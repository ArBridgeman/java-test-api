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

import ru.vyarus.dropwizard.guice.test.ClientSupport;
import ru.vyarus.dropwizard.guice.test.jupiter.ext.TestDropwizardAppExtension;

import java.util.List;
import java.util.Optional;

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

        @Test
        void userWithValidUserUpdateType_returns204(
                ClientSupport client, UserProfileDao userProfileDao) {
            when(userProfileDao.get(any(UserId.class)))
                    .thenReturn(Optional.of(UserProfileFixtures.USER_PROFILE));

            var response =
                    client.targetRest()
                            .path(URL)
                            .resolveTemplate(USER_ID_PATH_PARAM, UserProfileFixtures.USER_ID)
                            .resolveTemplate(USER_UPDATE_TYPE, UserUpdateType.REPLACE.toString())
                            .request()
                            .post(
                                    Entity.entity(
                                            UserProfileUpdateFixture.REPLACED_PROFILE_PROPERTY,
                                            MediaType.APPLICATION_JSON_TYPE));
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
                                            UserProfileUpdateFixture.REPLACED_PROFILE_PROPERTY,
                                            MediaType.APPLICATION_JSON_TYPE));
            assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST_400);
        }

        @Test
        void nonExistingUserWithValidUserUpdateType_returns204(
                ClientSupport client, UserProfileDao userProfileDao) {
            when(userProfileDao.get(any(UserId.class))).thenReturn(Optional.empty());

            var response =
                    client.targetRest()
                            .path(URL)
                            .resolveTemplate(USER_ID_PATH_PARAM, UserProfileFixtures.USER_ID)
                            .resolveTemplate(USER_UPDATE_TYPE, UserUpdateType.REPLACE.toString())
                            .request()
                            .post(
                                    Entity.entity(
                                            UserProfileUpdateFixture.REPLACED_PROFILE_PROPERTY,
                                            MediaType.APPLICATION_JSON_TYPE));
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
                                            UserProfileUpdateFixture.REPLACED_PROFILE_PROPERTY,
                                            MediaType.APPLICATION_JSON_TYPE));

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
                            .resolveTemplate(USER_UPDATE_TYPE, UserUpdateType.REPLACE.toString())
                            .request()
                            .post(
                                    Entity.entity(
                                            UserProfileUpdateFixture.REPLACED_PROFILE_PROPERTY,
                                            MediaType.APPLICATION_JSON_TYPE));

            assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
        }
    }

    @Nested
    @DisplayName("updateUserProfiles")
    class UpdateUserProfiles {
        private static final String URL = "/users/update/";
        private static final List<UserProfileUpdate> USER_PROFILE_UPDATE_WITH_REPLACES =
                List.of(
                        UserProfileUpdateFixture.USER_PROFILE_CHANGE,
                        new UserProfileUpdate(
                                UserProfileFixtures.NON_EXISTING_USER_ID,
                                UserUpdateType.REPLACE,
                                UserProfileUpdateFixture.REPLACED_PROFILE_PROPERTY));

        @Test
        void usersWithValidUserUpdateTypes_areSuccessful(
                ClientSupport client, UserProfileDao userProfileDao) {
            when(userProfileDao.get(UserProfileFixtures.USER_ID))
                    .thenReturn(Optional.of(UserProfileFixtures.USER_PROFILE));
            when(userProfileDao.get(UserProfileFixtures.NON_EXISTING_USER_ID))
                    .thenReturn(Optional.empty());

            var response =
                    client.targetRest()
                            .path(URL)
                            .request()
                            .post(
                                    Entity.entity(
                                            USER_PROFILE_UPDATE_WITH_REPLACES,
                                            MediaType.APPLICATION_JSON_TYPE));
            assertThat(response.getStatus()).isEqualTo(HttpStatus.NO_CONTENT_204);
        }
    }
}
