package com.spotlight.platform.userprofile.api.model.profile;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

import com.spotlight.platform.userprofile.api.model.profile.primitives.UserProfileFixtures;

import org.junit.jupiter.api.Test;

class UserProfileTest {

    @Test
    void serialization_WorksAsExpected() {
        assertThatJson(UserProfileFixtures.USER_PROFILE)
                .isEqualTo(UserProfileFixtures.SERIALIZED_USER_PROFILE);
    }
}
