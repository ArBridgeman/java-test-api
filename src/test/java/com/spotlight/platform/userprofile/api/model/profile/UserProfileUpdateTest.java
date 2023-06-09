package com.spotlight.platform.userprofile.api.model.profile;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

import com.spotlight.platform.userprofile.api.model.profile.primitives.UserProfileUpdateFixture;

import org.junit.jupiter.api.Test;

class UserProfileUpdateTest {

    // Could do for each type of profile update but same underlying mechanism
    @Test
    void serialization_worksAsExpected() {
        assertThatJson(UserProfileUpdateFixture.REPLACE_USER_PROFILE_UPDATE)
                .isEqualTo(UserProfileUpdateFixture.SERIALIZED_USER_PROFILE_UPDATE);
    }
}
