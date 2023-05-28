package com.spotlight.platform.userprofile.api.model.profile.primitives;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.spotlight.platform.userprofile.api.core.exceptions.EntityNotFoundException;

public enum UserUpdateType {
    REPLACE("replace"),
    INCREMENT("increment");

    private final String value;

    UserUpdateType(String value) {
        this.value = value;
    }

    @JsonCreator
    public static UserUpdateType fromJson(final String jsonValue) {
        for (UserUpdateType type : values()) {
            if (type.value.equals(jsonValue)) {
                return type;
            }
        }
        // could throw another exception
        throw new EntityNotFoundException();
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }
}
