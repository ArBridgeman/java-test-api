package com.spotlight.platform.userprofile.api.model.profile.primitives;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.spotlight.platform.userprofile.api.core.exceptions.EntityNotFoundException;

public enum UserChangeType {
    REPLACE("replace");

    private final String value;

    UserChangeType(String value) {
        this.value = value;
    }

    @JsonCreator
    public static UserChangeType fromJson(final String jsonValue) {
        for (UserChangeType type : values()) {
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
