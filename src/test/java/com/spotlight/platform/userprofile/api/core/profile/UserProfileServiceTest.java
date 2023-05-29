package com.spotlight.platform.userprofile.api.core.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.spotlight.platform.userprofile.api.core.exceptions.EntityNotFoundException;
import com.spotlight.platform.userprofile.api.core.profile.persistence.UserProfileDao;
import com.spotlight.platform.userprofile.api.model.profile.UserProfile;
import com.spotlight.platform.userprofile.api.model.profile.UserProfileUpdate;
import com.spotlight.platform.userprofile.api.model.profile.primitives.*;

import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
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
                        UserProfileUpdateFixture.REPLACE_PROFILE_PROPERTY);

        @Test
        void replaceExistingProperty_updatesValue() {
            when(userProfileDaoMock.get(any(UserId.class)))
                    .thenReturn(Optional.of(UserProfileFixtures.USER_PROFILE));

            fixInstantNow(
                    () ->
                            userProfileService.replace(
                                    UserProfileUpdateFixture.REPLACE_USER_PROFILE_UPDATE));

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
                    UserProfileUpdateFixture.getUserProfileProperty("property2", "property2Value");
            UserProfileUpdate userProfileUpdate =
                    new UserProfileUpdate(
                            UserProfileFixtures.USER_ID, UserUpdateType.REPLACE, replaceProperty2);

            fixInstantNow(() -> userProfileService.replace(userProfileUpdate));

            Map<UserProfilePropertyName, UserProfilePropertyValue> allProperties = new HashMap<>();
            allProperties.putAll(UserProfileFixtures.USER_PROFILE.userProfileProperties());
            allProperties.putAll(replaceProperty2);

            compareMockPutToExpectedUserProfile(
                    new UserProfile(
                            UserProfileFixtures.USER_ID,
                            UserProfileFixtures.LATEST_UPDATE_TIMESTAMP,
                            allProperties));
        }

        @Test
        void replaceForNonExistingUser_savesNewProfile() {
            when(userProfileDaoMock.get(any(UserId.class))).thenReturn(Optional.empty());

            fixInstantNow(
                    () ->
                            userProfileService.replace(
                                    UserProfileUpdateFixture.REPLACE_USER_PROFILE_UPDATE));

            compareMockPutToExpectedUserProfile(UPDATED_USER_PROFILE);
        }
    }

    @Nested
    @DisplayName("increment")
    class Increment {
        @Test
        void incrementExistingIntegerProperty_updatesValue() {
            UserProfile userProfile =
                    new UserProfile(
                            UserProfileFixtures.USER_ID,
                            UserProfileFixtures.LAST_UPDATE_TIMESTAMP,
                            UserProfileUpdateFixture.getUserProfileProperty("property2", -10));
            when(userProfileDaoMock.get(any(UserId.class))).thenReturn(Optional.of(userProfile));

            fixInstantNow(
                    () ->
                            userProfileService.increment(
                                    UserProfileUpdateFixture.INCREMENT_USER_PROFILE_UPDATE));

            compareMockPutToExpectedUserProfile(
                    new UserProfile(
                            UserProfileFixtures.USER_ID,
                            UserProfileFixtures.LATEST_UPDATE_TIMESTAMP,
                            UserProfileUpdateFixture.getUserProfileProperty("property2", -8)));
        }

        @Test
        void incrementNewProperty_addsValue() {
            when(userProfileDaoMock.get(any(UserId.class)))
                    .thenReturn(Optional.of(UserProfileFixtures.USER_PROFILE));

            fixInstantNow(
                    () ->
                            userProfileService.increment(
                                    new UserProfileUpdate(
                                            UserProfileFixtures.USER_ID,
                                            UserUpdateType.INCREMENT,
                                            UserProfileUpdateFixture.INCREMENT_PROFILE_PROPERTY)));

            Map<UserProfilePropertyName, UserProfilePropertyValue> allProperties = new HashMap<>();
            allProperties.putAll(UserProfileFixtures.USER_PROFILE.userProfileProperties());
            allProperties.putAll(UserProfileUpdateFixture.INCREMENT_PROFILE_PROPERTY);

            compareMockPutToExpectedUserProfile(
                    new UserProfile(
                            UserProfileFixtures.USER_ID,
                            UserProfileFixtures.LATEST_UPDATE_TIMESTAMP,
                            allProperties));
        }

        // Worthwhile to test for other types?
        // Or rather mock with thrown exception due to UserProfilePropertyValueTest?
        @Test
        void incrementCannotCastToInt_throwException() {
            when(userProfileDaoMock.get(any(UserId.class)))
                    .thenReturn(Optional.of(UserProfileFixtures.USER_PROFILE));

            UserProfileUpdate UserProfileUpdate =
                    new UserProfileUpdate(
                            UserProfileFixtures.USER_ID,
                            UserUpdateType.INCREMENT,
                            UserProfileUpdateFixture.getUserProfileProperty(
                                    "property2", "not_an_integer"));

            fixInstantNow(
                    () ->
                            assertThatThrownBy(
                                            () -> userProfileService.increment(UserProfileUpdate))
                                    .isExactlyInstanceOf(ClassCastException.class));
        }

        @Test
        void incrementForNonExistingUser_savesNewProfile() {
            when(userProfileDaoMock.get(any(UserId.class))).thenReturn(Optional.empty());

            fixInstantNow(
                    () ->
                            userProfileService.increment(
                                    UserProfileUpdateFixture.INCREMENT_USER_PROFILE_UPDATE));

            compareMockPutToExpectedUserProfile(
                    new UserProfile(
                            UserProfileFixtures.USER_ID,
                            UserProfileFixtures.LATEST_UPDATE_TIMESTAMP,
                            UserProfileUpdateFixture.INCREMENT_PROFILE_PROPERTY));
        }
    }

    @Nested
    @DisplayName("collect")
    class Collect {
        @Test
        void collectExistingProperty_updatesValue() {
            UserProfile userProfile =
                    new UserProfile(
                            UserProfileFixtures.USER_ID,
                            UserProfileFixtures.LAST_UPDATE_TIMESTAMP,
                            UserProfileUpdateFixture.getUserProfileProperty(
                                    "property3", List.of("shield")));

            when(userProfileDaoMock.get(any(UserId.class))).thenReturn(Optional.of(userProfile));

            fixInstantNow(
                    () ->
                            userProfileService.collect(
                                    UserProfileUpdateFixture.COLLECT_USER_PROFILE_UPDATE));

            compareMockPutToExpectedUserProfile(
                    new UserProfile(
                            UserProfileFixtures.USER_ID,
                            UserProfileFixtures.LATEST_UPDATE_TIMESTAMP,
                            UserProfileUpdateFixture.getUserProfileProperty(
                                    "property3", List.of("shield", "sword"))));
        }

        @Test
        void collectNewProperty_addsValue() {
            when(userProfileDaoMock.get(any(UserId.class)))
                    .thenReturn(Optional.of(UserProfileFixtures.USER_PROFILE));

            fixInstantNow(
                    () ->
                            userProfileService.collect(
                                    new UserProfileUpdate(
                                            UserProfileFixtures.USER_ID,
                                            UserUpdateType.INCREMENT,
                                            UserProfileUpdateFixture.COLLECT_PROFILE_PROPERTY)));

            Map<UserProfilePropertyName, UserProfilePropertyValue> allProperties = new HashMap<>();
            allProperties.putAll(UserProfileFixtures.USER_PROFILE.userProfileProperties());
            allProperties.putAll(UserProfileUpdateFixture.COLLECT_PROFILE_PROPERTY);

            compareMockPutToExpectedUserProfile(
                    new UserProfile(
                            UserProfileFixtures.USER_ID,
                            UserProfileFixtures.LATEST_UPDATE_TIMESTAMP,
                            allProperties));
        }

        // Worthwhile to test for other types?
        // Or rather mock with thrown exception due to UserProfilePropertyValueTest?
        @Test
        void collectCannotCastToList_throwException() {
            when(userProfileDaoMock.get(any(UserId.class)))
                    .thenReturn(Optional.of(UserProfileFixtures.USER_PROFILE));

            UserProfileUpdate UserProfileUpdate =
                    new UserProfileUpdate(
                            UserProfileFixtures.USER_ID,
                            UserUpdateType.COLLECT,
                            UserProfileUpdateFixture.getUserProfileProperty(
                                    "property2", "not_a_list"));

            fixInstantNow(
                    () ->
                            assertThatThrownBy(() -> userProfileService.collect(UserProfileUpdate))
                                    .isExactlyInstanceOf(ClassCastException.class));
        }

        @Test
        void collectForNonExistingUser_savesNewProfile() {
            when(userProfileDaoMock.get(any(UserId.class))).thenReturn(Optional.empty());

            fixInstantNow(
                    () ->
                            userProfileService.collect(
                                    UserProfileUpdateFixture.COLLECT_USER_PROFILE_UPDATE));

            compareMockPutToExpectedUserProfile(
                    new UserProfile(
                            UserProfileFixtures.USER_ID,
                            UserProfileFixtures.LATEST_UPDATE_TIMESTAMP,
                            UserProfileUpdateFixture.COLLECT_PROFILE_PROPERTY));
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
            doCallRealMethod().when(userProfileServiceMock).update(any(UserProfileUpdate.class));

            userProfileServiceMock.update(UserProfileUpdateFixture.REPLACE_USER_PROFILE_UPDATE);

            verify(userProfileServiceMock)
                    .replace(UserProfileUpdateFixture.REPLACE_USER_PROFILE_UPDATE);
        }

        @Test
        void updateWithIncrement_worksAsExpected() {
            when(userProfileDaoMock.get(any(UserId.class)))
                    .thenReturn(Optional.of(UserProfileFixtures.USER_PROFILE));
            doCallRealMethod().when(userProfileServiceMock).update(any(UserProfileUpdate.class));

            userProfileServiceMock.update(UserProfileUpdateFixture.INCREMENT_USER_PROFILE_UPDATE);

            verify(userProfileServiceMock)
                    .increment(UserProfileUpdateFixture.INCREMENT_USER_PROFILE_UPDATE);
        }

        @Test
        void updateWithCollect_worksAsExpected() {
            when(userProfileDaoMock.get(any(UserId.class)))
                    .thenReturn(Optional.of(UserProfileFixtures.USER_PROFILE));
            doCallRealMethod().when(userProfileServiceMock).update(any(UserProfileUpdate.class));

            userProfileServiceMock.update(UserProfileUpdateFixture.COLLECT_USER_PROFILE_UPDATE);

            verify(userProfileServiceMock)
                    .collect(UserProfileUpdateFixture.COLLECT_USER_PROFILE_UPDATE);
        }
    }

    private void compareMockPutToExpectedUserProfile(UserProfile expectedUserProfile) {
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
