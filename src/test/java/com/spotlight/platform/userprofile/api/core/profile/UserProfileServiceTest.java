package com.spotlight.platform.userprofile.api.core.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
// TODO figure out way to not change to import star
import static org.mockito.Mockito.*;

import com.spotlight.platform.userprofile.api.core.exceptions.EntityNotFoundException;
import com.spotlight.platform.userprofile.api.core.profile.persistence.UserProfileDao;
import com.spotlight.platform.userprofile.api.model.profile.UserProfile;
import com.spotlight.platform.userprofile.api.model.profile.UserProfileChange;
import com.spotlight.platform.userprofile.api.model.profile.primitives.*;

import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class UserProfileServiceTest {
    private final UserProfileDao userProfileDaoMock = mock(UserProfileDao.class);
    private final UserProfileService userProfileService =
            new UserProfileService(userProfileDaoMock);

    @Nested
    @DisplayName("get")
    class Get {
        @Test
        void getForExistingUser_returnsUser() {
            when(userProfileDaoMock.get(any(UserId.class)))
                    .thenReturn(Optional.of(UserProfileFixtures.USER_PROFILE));

            assertThat(userProfileService.get(UserProfileFixtures.USER_ID))
                    .usingRecursiveComparison()
                    .isEqualTo(UserProfileFixtures.USER_PROFILE);
        }

        @Test
        void getForNonExistingUser_throwsException() {
            when(userProfileDaoMock.get(any(UserId.class))).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userProfileService.get(UserProfileFixtures.USER_ID))
                    .isExactlyInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("replace")
    class Replace {
        private static final UserProfile UPDATED_USER_PROFILE =
                new UserProfile(
                        UserProfileFixtures.USER_ID,
                        UserProfileFixtures.LATEST_UPDATE_TIMESTAMP,
                        UserProfileChangeFixture.REPLACED_PROFILE_PROPERTY);

        @Test
        void replaceExistingProperty_updatesValue() {
            when(userProfileDaoMock.get(any(UserId.class)))
                    .thenReturn(Optional.of(UserProfileFixtures.USER_PROFILE));

            fixInstantNow(
                    () -> userProfileService.replace(UserProfileChangeFixture.USER_PROFILE_CHANGE));

            ArgumentCaptor<UserProfile> myCaptor = ArgumentCaptor.forClass(UserProfile.class);
            verify(userProfileDaoMock).put(myCaptor.capture());

            assertThat(myCaptor.getValue())
                    .usingRecursiveComparison()
                    .isEqualTo(UPDATED_USER_PROFILE);
        }

        @Test
        void replaceNewProperty_addsValue() {
            when(userProfileDaoMock.get(any(UserId.class)))
                    .thenReturn(Optional.of(UserProfileFixtures.USER_PROFILE));

            Map<UserProfilePropertyName, UserProfilePropertyValue> replaceProperty2 =
                    UserProfileChangeFixture.getUserProfileProperty("property2", "property2Value");
            UserProfileChange userProfileChange =
                    new UserProfileChange(
                            UserProfileFixtures.USER_ID, UserChangeType.REPLACE, replaceProperty2);

            fixInstantNow(() -> userProfileService.replace(userProfileChange));

            Map<UserProfilePropertyName, UserProfilePropertyValue> allProperties = new HashMap<>();
            allProperties.putAll(UserProfileFixtures.USER_PROFILE.userProfileProperties());
            allProperties.putAll(replaceProperty2);

            compareStubToExpectedUserProfile(
                    new UserProfile(
                            UserProfileFixtures.USER_ID,
                            UserProfileFixtures.LATEST_UPDATE_TIMESTAMP,
                            allProperties));
        }

        @Test
        void replaceForNonExistingUser_savesNewProfile() {
            when(userProfileDaoMock.get(any(UserId.class))).thenReturn(Optional.empty());

            fixInstantNow(
                    () -> userProfileService.replace(UserProfileChangeFixture.USER_PROFILE_CHANGE));

            compareStubToExpectedUserProfile(UPDATED_USER_PROFILE);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        private static final UserProfileService userProfileServiceMock =
                mock(UserProfileService.class);

        @Test
        void updateWithReplace_worksAsExpected() {
            when(userProfileDaoMock.get(any(UserId.class)))
                    .thenReturn(Optional.of(UserProfileFixtures.USER_PROFILE));
            doCallRealMethod().when(userProfileServiceMock).update(any(UserProfileChange.class));

            userProfileServiceMock.update(UserProfileChangeFixture.USER_PROFILE_CHANGE);

            verify(userProfileServiceMock).replace(UserProfileChangeFixture.USER_PROFILE_CHANGE);
        }
    }

    private void compareStubToExpectedUserProfile(UserProfile expectedUserProfile) {
        ArgumentCaptor<UserProfile> captor = ArgumentCaptor.forClass(UserProfile.class);
        verify(userProfileDaoMock).put(captor.capture());

        assertThat(captor.getValue()).usingRecursiveComparison().isEqualTo(expectedUserProfile);
    }

    private void fixInstantNow(Runnable expressionToRun) {
        try (MockedStatic<Instant> mockedStatic = mockStatic(Instant.class)) {
            mockedStatic.when(Instant::now).thenReturn(UserProfileFixtures.LATEST_UPDATE_TIMESTAMP);
            expressionToRun.run();
        }
    }
}
