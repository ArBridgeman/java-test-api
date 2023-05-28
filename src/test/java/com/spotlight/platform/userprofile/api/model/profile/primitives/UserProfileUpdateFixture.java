package com.spotlight.platform.userprofile.api.model.profile.primitives;

import com.spotlight.platform.helpers.FixtureHelpers;
import com.spotlight.platform.userprofile.api.model.profile.UserProfileUpdate;

import java.util.Map;

public class UserProfileUpdateFixture {

    public static Map<UserProfilePropertyName, UserProfilePropertyValue> getUserProfileProperty(
            String propertyName, Object propertyValue) {
        return Map.of(
                UserProfilePropertyName.valueOf(propertyName),
                UserProfilePropertyValue.valueOf(propertyValue));
    }

    public static final Map<UserProfilePropertyName, UserProfilePropertyValue>
            REPLACED_PROFILE_PROPERTY = getUserProfileProperty("property1", "newValue");

    public static final UserProfileUpdate USER_PROFILE_CHANGE =
            new UserProfileUpdate(
                    UserProfileFixtures.USER_ID, UserUpdateType.REPLACE, REPLACED_PROFILE_PROPERTY);

    public static final String SERIALIZED_USER_PROFILE_UPDATE =
            FixtureHelpers.fixture("/fixtures/model/profile/userProfileUpdate.json");
}
