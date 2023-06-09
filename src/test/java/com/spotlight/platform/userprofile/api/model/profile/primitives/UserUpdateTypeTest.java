package com.spotlight.platform.userprofile.api.model.profile.primitives;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

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

    private static Stream<Arguments> provideParameters() {
        return Stream.of(
                Arguments.of(UserUpdateType.REPLACE, "replace"),
                Arguments.of(UserUpdateType.INCREMENT, "increment"),
                Arguments.of(UserUpdateType.COLLECT, "collect"));
    }
}
