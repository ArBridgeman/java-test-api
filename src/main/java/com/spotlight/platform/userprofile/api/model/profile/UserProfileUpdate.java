package com.spotlight.platform.userprofile.api.model.profile;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.spotlight.platform.userprofile.api.model.profile.primitives.UserId;
import com.spotlight.platform.userprofile.api.model.profile.primitives.UserProfilePropertyName;
import com.spotlight.platform.userprofile.api.model.profile.primitives.UserProfilePropertyValue;
import com.spotlight.platform.userprofile.api.model.profile.primitives.UserUpdateType;

import java.util.Map;

import javax.validation.Valid;

public record UserProfileUpdate(
        // If Valid not provided here and UserProfileUpdate is nested, i.e. a list, then these types
        // are not enforced.
        @Valid @JsonProperty UserId userId,
        @Valid @JsonProperty UserUpdateType userUpdateType,
        @Valid @JsonProperty
                Map<UserProfilePropertyName, UserProfilePropertyValue> userProfileProperties) {}
