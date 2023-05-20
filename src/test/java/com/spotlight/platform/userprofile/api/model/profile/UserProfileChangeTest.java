package com.spotlight.platform.userprofile.api.model.profile;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

import com.spotlight.platform.userprofile.api.model.profile.primitives.UserProfileChangeFixture;

import org.junit.jupiter.api.Test;

class UserProfileChangeTest {

    @Test
    void serialization_worksAsExpected() {
        assertThatJson(UserProfileChangeFixture.USER_PROFILE_CHANGE)
                .isEqualTo(UserProfileChangeFixture.SERIALIZED_USER_PROFILE_CHANGE);
    }
}
