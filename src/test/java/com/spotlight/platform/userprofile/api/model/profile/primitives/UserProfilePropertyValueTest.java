package com.spotlight.platform.userprofile.api.model.profile.primitives;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

class UserProfilePropertyValueTest {
    private static final String STRING_VALUE = "someString";
    private static final int INTEGER_VALUE = 5;
    private static final double DOUBLE_VALUE = 15.2;
    private static final List<String> LIST_VALUE = List.of("one", "two");

    @Test
    void equals_returnsTrueForEqualValues() {
        assertThat(UserProfilePropertyValue.valueOf(STRING_VALUE))
                .isEqualTo(UserProfilePropertyValue.valueOf(STRING_VALUE));
        assertThat(UserProfilePropertyValue.valueOf(INTEGER_VALUE))
                .isEqualTo(UserProfilePropertyValue.valueOf(INTEGER_VALUE));
        assertThat(UserProfilePropertyValue.valueOf(DOUBLE_VALUE))
                .isEqualTo(UserProfilePropertyValue.valueOf(DOUBLE_VALUE));
        assertThat(UserProfilePropertyValue.valueOf(LIST_VALUE))
                .isEqualTo(UserProfilePropertyValue.valueOf(LIST_VALUE));
    }

    @Test
    void serialization_worksCorrectly() {
        assertThatJson(UserProfilePropertyValue.valueOf(STRING_VALUE)).isEqualTo("someString");
        assertThatJson(UserProfilePropertyValue.valueOf(INTEGER_VALUE)).isEqualTo("5");
        assertThatJson(UserProfilePropertyValue.valueOf(DOUBLE_VALUE)).isEqualTo("15.2");
        assertThatJson(UserProfilePropertyValue.valueOf(LIST_VALUE)).isEqualTo("[\"one\",\"two\"]");
    }

    @Nested
    @DisplayName("implement")
    class Implement {
        private static final UserProfilePropertyValue currentValue =
                UserProfilePropertyValue.valueOf(INTEGER_VALUE);

        @Test
        void implement_worksCorrectly() {
            assertThat(currentValue.increment(UserProfilePropertyValue.valueOf(INTEGER_VALUE)))
                    .isEqualTo(UserProfilePropertyValue.valueOf(10));
        }

        @Test
        void implement_throwsException() {
            assertThatThrownBy(
                            () ->
                                    currentValue.increment(
                                            UserProfilePropertyValue.valueOf(STRING_VALUE)))
                    .isExactlyInstanceOf(ClassCastException.class);
            assertThatThrownBy(
                            () ->
                                    currentValue.increment(
                                            UserProfilePropertyValue.valueOf(DOUBLE_VALUE)))
                    .isExactlyInstanceOf(ClassCastException.class);
            assertThatThrownBy(
                            () ->
                                    currentValue.increment(
                                            UserProfilePropertyValue.valueOf(LIST_VALUE)))
                    .isExactlyInstanceOf(ClassCastException.class);
        }
    }
}
