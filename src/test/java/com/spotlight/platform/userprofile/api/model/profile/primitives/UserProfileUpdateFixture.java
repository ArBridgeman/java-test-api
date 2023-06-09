package com.spotlight.platform.userprofile.api.model.profile.primitives;

import com.spotlight.platform.helpers.FixtureHelpers;
import com.spotlight.platform.userprofile.api.model.profile.UserProfileUpdate;

import java.util.List;
import java.util.Map;

public class UserProfileUpdateFixture {

    public static Map<UserProfilePropertyName, UserProfilePropertyValue> getUserProfileProperty(
            String propertyName, Object propertyValue) {
        return Map.of(
                UserProfilePropertyName.valueOf(propertyName),
                UserProfilePropertyValue.valueOf(propertyValue));
    }

    // As we add more methods, we might want these broken up into separate fixture files.
    // Alternately, we might want some smarter way to do.
    public static final Map<UserProfilePropertyName, UserProfilePropertyValue>
            REPLACE_PROFILE_PROPERTY = getUserProfileProperty("property1", "newValue");

    public static final UserProfileUpdate REPLACE_USER_PROFILE_UPDATE =
            new UserProfileUpdate(
                    UserProfileFixtures.USER_ID, UserUpdateType.REPLACE, REPLACE_PROFILE_PROPERTY);

    public static final String SERIALIZED_USER_PROFILE_UPDATE =
            FixtureHelpers.fixture("/fixtures/model/profile/userProfileUpdate.json");

    public static final Map<UserProfilePropertyName, UserProfilePropertyValue>
            INCREMENT_PROFILE_PROPERTY = getUserProfileProperty("property2", 2);

    public static final UserProfileUpdate INCREMENT_USER_PROFILE_UPDATE =
            new UserProfileUpdate(
                    UserProfileFixtures.USER_ID,
                    UserUpdateType.INCREMENT,
                    INCREMENT_PROFILE_PROPERTY);

    public static final Map<UserProfilePropertyName, UserProfilePropertyValue>
            COLLECT_PROFILE_PROPERTY = getUserProfileProperty("property3", List.of("sword"));

    public static final UserProfileUpdate COLLECT_USER_PROFILE_UPDATE =
            new UserProfileUpdate(
                    UserProfileFixtures.USER_ID, UserUpdateType.COLLECT, COLLECT_PROFILE_PROPERTY);

    public static final UserProfileUpdate COLLECT_WITH_BAD_PROPERTY_USER_PROFILE_UPDATE =
            new UserProfileUpdate(
                    UserProfileFixtures.USER_ID,
                    UserUpdateType.COLLECT,
                    getUserProfileProperty("property2", "not-a-list"));

    public static final String INVALID_SERIALIZED_USER_PROFILE_UPDATES =
            FixtureHelpers.fixture("/fixtures/model/profile/invalidUserProfileUpdates.json");
}
