package com.spotlight.platform.userprofile.api.model.profile;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.spotlight.platform.userprofile.api.model.profile.primitives.UserChangeType;
import com.spotlight.platform.userprofile.api.model.profile.primitives.UserId;
import com.spotlight.platform.userprofile.api.model.profile.primitives.UserProfilePropertyName;
import com.spotlight.platform.userprofile.api.model.profile.primitives.UserProfilePropertyValue;

import java.util.Map;

public record UserProfileChange(
        @JsonProperty UserId userId,
        @JsonProperty UserChangeType userChangeType,
        @JsonProperty
                Map<UserProfilePropertyName, UserProfilePropertyValue> userProfileProperties) {}
