package com.spotlight.platform.userprofile.api.model.profile.primitives;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.spotlight.platform.userprofile.api.core.exceptions.EntityNotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class UserUpdateTypeTest {

    @ParameterizedTest
    @MethodSource("provideParameters")
    void serialization_worksAsExpected(UserUpdateType enumToSerialize, String expectedJsonValue) {
        assertThatJson(enumToSerialize).isEqualTo(expectedJsonValue);
    }

    @ParameterizedTest
    @MethodSource("provideParameters")
    void fromJson_worksAsExpected(UserUpdateType expectedEnum, String jsonToSerialize) {
        assertThat(UserUpdateType.fromJson(jsonToSerialize)).isEqualTo(expectedEnum);
    }

    @Test
    void fromJsonForUndefinedValue_throwsException() {
        assertThatThrownBy(() -> UserUpdateType.fromJson("do_something_not_defined"))
                .isExactlyInstanceOf(EntityNotFoundException.class);
    }

    private static Stream<Arguments> provideParameters() {
        return Stream.of(
                Arguments.of(UserUpdateType.REPLACE, "replace"),
                Arguments.of(UserUpdateType.INCREMENT, "increment"),
                Arguments.of(UserUpdateType.COLLECT, "collect"));
    }
}
