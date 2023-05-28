package com.spotlight.platform.userprofile.api.model.profile;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

import com.spotlight.platform.userprofile.api.model.profile.primitives.UserProfileUpdateFixture;

import org.junit.jupiter.api.Test;

class UserProfileUpdateTest {

    @Test
    void serialization_worksAsExpected() {
        assertThatJson(UserProfileUpdateFixture.USER_PROFILE_CHANGE)
                .isEqualTo(UserProfileUpdateFixture.SERIALIZED_USER_PROFILE_CHANGE);
    }
}
