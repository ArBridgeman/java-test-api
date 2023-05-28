package com.spotlight.platform.userprofile.api.web.resources;

import com.spotlight.platform.userprofile.api.core.profile.UserProfileService;
import com.spotlight.platform.userprofile.api.model.profile.UserProfile;
import com.spotlight.platform.userprofile.api.model.profile.UserProfileUpdate;
import com.spotlight.platform.userprofile.api.model.profile.primitives.UserId;
import com.spotlight.platform.userprofile.api.model.profile.primitives.UserProfilePropertyName;
import com.spotlight.platform.userprofile.api.model.profile.primitives.UserProfilePropertyValue;
import com.spotlight.platform.userprofile.api.model.profile.primitives.UserUpdateType;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

    private final UserProfileService userProfileService;

    @Inject
    public UserResource(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @Path("{userId}/profile")
    @GET
    public UserProfile getUserProfile(@Valid @PathParam("userId") UserId userId) {
        return userProfileService.get(userId);
    }

    @Path("{userId}/update/{userUpdateType}")
    @POST
    public void updateUserProfile(
            @Valid @PathParam("userId") UserId userId,
            @PathParam("userUpdateType") UserUpdateType userUpdateType,
            Map<UserProfilePropertyName, UserProfilePropertyValue> userProfileProperties) {
        UserProfileUpdate userProfileUpdate =
                new UserProfileUpdate(userId, userUpdateType, userProfileProperties);
        userProfileService.update(userProfileUpdate);
    }

    // Might prefer to separate out bulk vs individual operations into different classes
    @Path("update")
    @POST
    public void updateUserProfiles(@Valid List<UserProfileUpdate> userProfileUpdates) {
        userProfileUpdates.forEach(userProfileService::update);
    }
}
