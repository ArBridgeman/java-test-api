package com.spotlight.platform.userprofile.api.model.profile.primitives;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.spotlight.platform.userprofile.api.core.exceptions.EntityNotFoundException;

import org.junit.jupiter.api.Test;

class UserChangeTypeTest {

    @Test
    void serialization_worksAsExpected() {
        assertThatJson(UserChangeType.REPLACE).isEqualTo("replace");
    }

    @Test
    void fromJson_worksAsExpected() {
        assertThat(UserChangeType.fromJson("replace")).isEqualTo(UserChangeType.REPLACE);
    }

    @Test
    void fromJsonForUndefinedValue_throwsException() {
        assertThatThrownBy(() -> UserChangeType.fromJson("do_something_not_defined"))
                .isExactlyInstanceOf(EntityNotFoundException.class);
    }
}
