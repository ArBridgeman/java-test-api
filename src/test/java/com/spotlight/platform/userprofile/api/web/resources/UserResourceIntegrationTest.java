package com.spotlight.platform.userprofile.api.web.resources;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.spotlight.platform.userprofile.api.core.profile.persistence.UserProfileDao;
import com.spotlight.platform.userprofile.api.model.profile.UserProfile;
import com.spotlight.platform.userprofile.api.model.profile.UserProfileUpdate;
import com.spotlight.platform.userprofile.api.model.profile.primitives.*;
import com.spotlight.platform.userprofile.api.web.UserProfileApiApplication;

import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ru.vyarus.dropwizard.guice.test.ClientSupport;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.TestDropwizardAppExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

@Execution(ExecutionMode.SAME_THREAD)
class UserResourceIntegrationTest {
    @RegisterExtension
    static TestDropwizardAppExtension APP =
            TestDropwizardAppExtension.forApp(UserProfileApiApplication.class)
                    .randomPorts()
                    .hooks(
                            builder ->
                                    builder.modulesOverride(
                                            new AbstractModule() {
                                                @Provides
                                                @Singleton
                                                public UserProfileDao getUserProfileDao() {
                                                    return mock(UserProfileDao.class);
                                                }
                                            }))
                    .randomPorts()
                    .create();

    @BeforeEach
    void beforeEach(UserProfileDao userProfileDao) {
        reset(userProfileDao);
    }

    @Nested
    @DisplayName("getUserProfile")
    class GetUserProfile {
        private static final String USER_ID_PATH_PARAM = "userId";
        private static final String URL = "/users/{%s}/profile".formatted(USER_ID_PATH_PARAM);

        @Test
        void existingUser_correctObjectIsReturned(
                ClientSupport client, UserProfileDao userProfileDao) {
            when(userProfileDao.get(any(UserId.class)))
                    .thenReturn(Optional.of(UserProfileFixtures.USER_PROFILE));

            var response =
                    client.targetRest()
                            .path(URL)
                            .resolveTemplate(USER_ID_PATH_PARAM, UserProfileFixtures.USER_ID)
                            .request()
                            .get();

            assertThat(response.getStatus()).isEqualTo(HttpStatus.OK_200);
            assertThatJson(response.readEntity(UserProfile.class))
                    .isEqualTo(UserProfileFixtures.SERIALIZED_USER_PROFILE);
        }

        @Test
        void nonExistingUser_returns404(ClientSupport client, UserProfileDao userProfileDao) {
            when(userProfileDao.get(any(UserId.class))).thenReturn(Optional.empty());

            var response =
                    client.targetRest()
                            .path(URL)
                            .resolveTemplate(USER_ID_PATH_PARAM, UserProfileFixtures.USER_ID)
                            .request()
                            .get();

            assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND_404);
        }

        @Test
        void validationFailed_returns400(ClientSupport client) {
            var response =
                    client.targetRest()
                            .path(URL)
                            .resolveTemplate(
                                    USER_ID_PATH_PARAM, UserProfileFixtures.INVALID_USER_ID)
                            .request()
                            .get();

            assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST_400);
        }

        @Test
        void unhandledExceptionOccurred_returns500(
                ClientSupport client, UserProfileDao userProfileDao) {
            when(userProfileDao.get(any(UserId.class)))
                    .thenThrow(new RuntimeException("Some unhandled exception"));

            var response =
                    client.targetRest()
                            .path(URL)
                            .resolveTemplate(USER_ID_PATH_PARAM, UserProfileFixtures.USER_ID)
                            .request()
                            .get();

            assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
        }
    }

    @Nested
    @DisplayName("updateUserProfile")
    class UpdateUserProfile {
        private static final String USER_ID_PATH_PARAM = "userId";
        private static final String USER_UPDATE_TYPE = "userUpdateType";
        private static final String URL =
                "/users/{%s}/update/{%s}".formatted(USER_ID_PATH_PARAM, USER_UPDATE_TYPE);

        private static Stream<Arguments> provideUpdateProperties() {
            return Stream.of(
                    Arguments.of(
                            UserProfileUpdateFixture.REPLACE_PROFILE_PROPERTY,
                            UserUpdateType.REPLACE),
                    Arguments.of(
                            UserProfileUpdateFixture.INCREMENT_PROFILE_PROPERTY,
                            UserUpdateType.INCREMENT),
                    Arguments.of(
                            UserProfileUpdateFixture.COLLECT_PROFILE_PROPERTY,
                            UserUpdateType.COLLECT));
        }

        @ParameterizedTest
        @MethodSource("provideUpdateProperties")
        void userWithValidUserUpdateType_returns204(
                Map<UserProfilePropertyName, UserProfilePropertyValue> updateUserProperties,
                UserUpdateType userUpdateType,
                ClientSupport client,
                UserProfileDao userProfileDao) {
            when(userProfileDao.get(any(UserId.class)))
                    .thenReturn(Optional.of(UserProfileFixtures.USER_PROFILE));

            var response =
                    client.targetRest()
                            .path(URL)
                            .resolveTemplate(USER_ID_PATH_PARAM, UserProfileFixtures.USER_ID)
                            .resolveTemplate(USER_UPDATE_TYPE, userUpdateType.toString())
                            .request()
                            .post(
                                    Entity.entity(
                                            updateUserProperties, MediaType.APPLICATION_JSON_TYPE));
            assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT_204);
        }

        @Test
        void userWithInvalidUserUpdateType_returns400(
                ClientSupport client, UserProfileDao userProfileDao) {
            when(userProfileDao.get(any(UserId.class)))
                    .thenReturn(Optional.of(UserProfileFixtures.USER_PROFILE));

            var response =
                    client.targetRest()
                            .path(URL)
                            .resolveTemplate(USER_ID_PATH_PARAM, UserProfileFixtures.USER_ID)
                            .resolveTemplate(USER_UPDATE_TYPE, "do_something_undefined")
                            .request()
                            .post(
                                    Entity.entity(
                                            UserProfileUpdateFixture.REPLACE_PROFILE_PROPERTY,
                                            MediaType.APPLICATION_JSON_TYPE));
            assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST_400);
        }

        @ParameterizedTest
        @MethodSource("provideUpdateProperties")
        void nonExistingUserWithValidUserUpdateType_returns204(
                Map<UserProfilePropertyName, UserProfilePropertyValue> updateUserProperties,
                UserUpdateType userUpdateType,
                ClientSupport client,
                UserProfileDao userProfileDao) {
            when(userProfileDao.get(any(UserId.class))).thenReturn(Optional.empty());

            var response =
                    client.targetRest()
                            .path(URL)
                            .resolveTemplate(USER_ID_PATH_PARAM, UserProfileFixtures.USER_ID)
                            .resolveTemplate(USER_UPDATE_TYPE, userUpdateType.toString())
                            .request()
                            .post(
                                    Entity.entity(
                                            updateUserProperties, MediaType.APPLICATION_JSON_TYPE));
            assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT_204);
        }

        @Test
        void invalidUser_returns400(ClientSupport client) {
            var response =
                    client.targetRest()
                            .path(URL)
                            .resolveTemplate(
                                    USER_ID_PATH_PARAM, UserProfileFixtures.INVALID_USER_ID)
                            .resolveTemplate(USER_UPDATE_TYPE, UserUpdateType.REPLACE.toString())
                            .request()
                            .post(
                                    Entity.entity(
                                            UserProfileUpdateFixture.REPLACE_PROFILE_PROPERTY,
                                            MediaType.APPLICATION_JSON_TYPE));

            assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST_400);
        }

        // Mock and throw exception or cleaner way to do?
        @Test
        void userWithUnhandledExceptionOccurred_returns500(
                ClientSupport client, UserProfileDao userProfileDao) {
            when(userProfileDao.get(any(UserId.class)))
                    .thenReturn(Optional.of(UserProfileFixtures.USER_PROFILE));

            var response =
                    client.targetRest()
                            .path(URL)
                            .resolveTemplate(USER_ID_PATH_PARAM, UserProfileFixtures.USER_ID)
                            .resolveTemplate(USER_UPDATE_TYPE, UserUpdateType.INCREMENT.toString())
                            .request()
                            .post(
                                    Entity.entity(
                                            UserProfileUpdateFixture.REPLACE_PROFILE_PROPERTY,
                                            MediaType.APPLICATION_JSON_TYPE));

            assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
        }
    }

    @Nested
    @DisplayName("updateUserProfiles")
    class UpdateUserProfiles {
        private static final String URL = "/users/update/";

        private static Stream<Arguments> provideUserProfileUpdate() {
            return Stream.of(
                    Arguments.of(UserProfileUpdateFixture.REPLACE_USER_PROFILE_UPDATE),
                    Arguments.of(UserProfileUpdateFixture.INCREMENT_USER_PROFILE_UPDATE),
                    Arguments.of(UserProfileUpdateFixture.COLLECT_USER_PROFILE_UPDATE));
        }

        @ParameterizedTest
        @MethodSource("provideUserProfileUpdate")
        void userWithValidUserUpdateType_returns204(
                UserProfileUpdate userProfileUpdate,
                ClientSupport client,
                UserProfileDao userProfileDao) {

            when(userProfileDao.get(UserProfileFixtures.USER_ID))
                    .thenReturn(Optional.of(UserProfileFixtures.USER_PROFILE));

            var response =
                    client.targetRest()
                            .path(URL)
                            .request()
                            .post(
                                    Entity.entity(
                                            List.of(userProfileUpdate),
                                            MediaType.APPLICATION_JSON_TYPE));
            assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT_204);
        }

        @Test
        void userWithBulkValidUserUpdateTypes_returns204(
                ClientSupport client, UserProfileDao userProfileDao) {

            when(userProfileDao.get(UserProfileFixtures.USER_ID))
                    .thenReturn(Optional.of(UserProfileFixtures.USER_PROFILE));

            var response =
                    client.targetRest()
                            .path(URL)
                            .request()
                            .post(
                                    Entity.entity(
                                            List.of(
                                                    UserProfileUpdateFixture
                                                            .REPLACE_USER_PROFILE_UPDATE,
                                                    UserProfileUpdateFixture
                                                            .INCREMENT_USER_PROFILE_UPDATE,
                                                    UserProfileUpdateFixture
                                                            .COLLECT_USER_PROFILE_UPDATE),
                                            MediaType.APPLICATION_JSON_TYPE));
            assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT_204);
        }

        @Test
        void usersWithBulkValidUserUpdateTypes_returns204(
                ClientSupport client, UserProfileDao userProfileDao) {

            when(userProfileDao.get(UserProfileFixtures.USER_ID))
                    .thenReturn(Optional.of(UserProfileFixtures.USER_PROFILE));
            when(userProfileDao.get(UserProfileFixtures.NON_EXISTING_USER_ID))
                    .thenReturn(Optional.empty());

            UserProfileUpdate nonExistingUserProfileUpdate =
                    new UserProfileUpdate(
                            UserProfileFixtures.NON_EXISTING_USER_ID,
                            UserUpdateType.COLLECT,
                            UserProfileUpdateFixture.COLLECT_PROFILE_PROPERTY);

            var response =
                    client.targetRest()
                            .path(URL)
                            .request()
                            .post(
                                    Entity.entity(
                                            List.of(
                                                    nonExistingUserProfileUpdate,
                                                    UserProfileUpdateFixture
                                                            .REPLACE_USER_PROFILE_UPDATE),
                                            MediaType.APPLICATION_JSON_TYPE));
            assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT_204);
        }

        @ParameterizedTest
        @MethodSource("provideUserProfileUpdate")
        void nonExistingUserWithValidUserUpdateType_returns204(
                UserProfileUpdate userProfileUpdate,
                ClientSupport client,
                UserProfileDao userProfileDao) {

            when(userProfileDao.get(any(UserId.class))).thenReturn(Optional.empty());

            var response =
                    client.targetRest()
                            .path(URL)
                            .request()
                            .post(
                                    Entity.entity(
                                            List.of(userProfileUpdate),
                                            MediaType.APPLICATION_JSON_TYPE));
            assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT_204);
        }

        @Test
        void userWithInvalidUserUpdateType_returns400(
                ClientSupport client, UserProfileDao userProfileDao) {
            when(userProfileDao.get(any(UserId.class)))
                    .thenReturn(Optional.of(UserProfileFixtures.USER_PROFILE));

            // would be interesting to know why differs from invalidUser
            var response =
                    client.targetRest()
                            .path(URL)
                            .request()
                            .post(
                                    Entity.json(
                                            UserProfileUpdateFixture
                                                    .INVALID_SERIALIZED_USER_PROFILE_UPDATES));

            assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST_400);
        }

        // Mock and throw exception or cleaner way to do?
        @Test
        void userWithUnhandledExceptionOccurred_returns500(
                ClientSupport client, UserProfileDao userProfileDao) {
            when(userProfileDao.get(any(UserId.class)))
                    .thenReturn(Optional.of(UserProfileFixtures.USER_PROFILE));

            var response =
                    client.targetRest()
                            .path(URL)
                            .request()
                            .post(
                                    Entity.entity(
                                            List.of(
                                                    UserProfileUpdateFixture
                                                            .COLLECT_WITH_BAD_PROPERTY_USER_PROFILE_UPDATE),
                                            MediaType.APPLICATION_JSON_TYPE));

            assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
        }

        @Test
        void invalidUser_returns422(ClientSupport client) {
            UserProfileUpdate invalidUserProfileUpdate =
                    new UserProfileUpdate(
                            UserProfileFixtures.INVALID_USER_ID,
                            UserUpdateType.REPLACE,
                            UserProfileUpdateFixture.REPLACE_PROFILE_PROPERTY);

            var response =
                    client.targetRest()
                            .path(URL)
                            .request()
                            .post(
                                    Entity.entity(
                                            List.of(invalidUserProfileUpdate),
                                            MediaType.APPLICATION_JSON_TYPE));

            assertThat(response.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY_422);
        }
    }
}
