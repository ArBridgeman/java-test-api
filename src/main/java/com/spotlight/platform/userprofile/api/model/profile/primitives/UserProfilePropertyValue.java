package com.spotlight.platform.userprofile.api.model.profile.primitives;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.*;

public class UserProfilePropertyValue {

    private final Object value;

    @JsonCreator
    private UserProfilePropertyValue(Object value) {
        this.value = value;
    }

    public static UserProfilePropertyValue valueOf(Object value) {
        return new UserProfilePropertyValue(value);
    }

    @JsonValue
    protected Object getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        return value.equals(((UserProfilePropertyValue) obj).getValue());
    }

    public UserProfilePropertyValue increment(UserProfilePropertyValue incrementValue) {
        // Only allows for type integer in increment; may be that we support doubles, etc.
        // or replace the existing value with the new integer
        return UserProfilePropertyValue.valueOf(
                (Integer) incrementValue.getValue() + (Integer) value);
    }

    public UserProfilePropertyValue collect(UserProfilePropertyValue collectValue) {
        // Only allows for type List; may be that we want to put a string in a list
        // or replace the existing value
        ArrayList<Object> combinedList = new ArrayList<>();
        combinedList.addAll((Collection<?>) value);
        combinedList.addAll((Collection<?>) collectValue.getValue());
        return UserProfilePropertyValue.valueOf(combinedList);
    }
}
