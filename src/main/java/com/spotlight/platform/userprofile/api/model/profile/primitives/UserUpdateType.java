package com.spotlight.platform.userprofile.api.model.profile.primitives;

import com.fasterxml.jackson.annotation.JsonValue;

public enum UserUpdateType {
    REPLACE("replace"),
    INCREMENT("increment"),
    COLLECT("collect");

    private final String value;

    UserUpdateType(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }
}
