package com.spotlight.platform.userprofile.api.model.profile;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.spotlight.platform.userprofile.api.model.profile.primitives.UserId;
import com.spotlight.platform.userprofile.api.model.profile.primitives.UserProfilePropertyName;
import com.spotlight.platform.userprofile.api.model.profile.primitives.UserProfilePropertyValue;
import com.spotlight.platform.userprofile.api.model.profile.primitives.UserUpdateType;

import java.util.Map;

public record UserProfileUpdate(
        @JsonProperty UserId userId,
        @JsonProperty UserUpdateType userUpdateType,
        @JsonProperty
                Map<UserProfilePropertyName, UserProfilePropertyValue> userProfileProperties) {}
