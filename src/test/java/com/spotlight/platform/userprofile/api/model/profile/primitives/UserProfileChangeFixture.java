package com.spotlight.platform.userprofile.api.model.profile.primitives;

import com.spotlight.platform.helpers.FixtureHelpers;
import com.spotlight.platform.userprofile.api.model.profile.UserProfileChange;

import java.util.Map;

public class UserProfileChangeFixture {

    public static Map<UserProfilePropertyName, UserProfilePropertyValue> getUserProfileProperty(
            String propertyName, Object propertyValue) {
        return Map.of(
                UserProfilePropertyName.valueOf(propertyName),
                UserProfilePropertyValue.valueOf(propertyValue));
    }

    public static final Map<UserProfilePropertyName, UserProfilePropertyValue>
            REPLACED_PROFILE_PROPERTY = getUserProfileProperty("property1", "newValue");

    public static final UserProfileChange USER_PROFILE_CHANGE =
            new UserProfileChange(
                    UserProfileFixtures.USER_ID, UserChangeType.REPLACE, REPLACED_PROFILE_PROPERTY);

    public static final String SERIALIZED_USER_PROFILE_CHANGE =
            FixtureHelpers.fixture("/fixtures/model/profile/userProfileChange.json");
}
