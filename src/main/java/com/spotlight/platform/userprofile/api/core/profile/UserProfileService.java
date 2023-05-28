package com.spotlight.platform.userprofile.api.core.profile;

import com.spotlight.platform.userprofile.api.core.exceptions.EntityNotFoundException;
import com.spotlight.platform.userprofile.api.core.profile.persistence.UserProfileDao;
import com.spotlight.platform.userprofile.api.model.profile.UserProfile;
import com.spotlight.platform.userprofile.api.model.profile.UserProfileUpdate;
import com.spotlight.platform.userprofile.api.model.profile.primitives.UserId;
import com.spotlight.platform.userprofile.api.model.profile.primitives.UserProfilePropertyName;
import com.spotlight.platform.userprofile.api.model.profile.primitives.UserProfilePropertyValue;
import com.spotlight.platform.userprofile.api.model.profile.primitives.UserUpdateType;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

public class UserProfileService {
    private final UserProfileDao userProfileDao;

    @Inject
    public UserProfileService(UserProfileDao userProfileDao) {
        this.userProfileDao = userProfileDao;
    }

    public UserProfile get(UserId userId) {
        return userProfileDao.get(userId).orElseThrow(EntityNotFoundException::new);
    }

    private Map<UserProfilePropertyName, UserProfilePropertyValue> getProfileProperties(
            UserId userId) {
        Optional<UserProfile> userProfile = userProfileDao.get(userId);
        if (userProfile.isEmpty()) {
            // Reduce downstream complexities by providing empty HashMap
            // in the event that a user profile did not previously exist.
            return new HashMap<>();
        }
        return new HashMap<>(userProfile.get().userProfileProperties());
    }

    public void replace(UserProfileUpdate userProfileUpdate) {
        UserId userId = userProfileUpdate.userId();
        Map<UserProfilePropertyName, UserProfilePropertyValue> profileProperties =
                getProfileProperties(userId);

        // Replace existing UserProfilePropertyName in profile and add new ones;
        // It will replace regardless of previous type.
        profileProperties.putAll(userProfileUpdate.userProfileProperties());

        userProfileDao.put(new UserProfile(userId, Instant.now(), profileProperties));
    }

    public void update(UserProfileUpdate userProfileUpdate) {
        if (userProfileUpdate.userUpdateType() == UserUpdateType.REPLACE) {
            replace(userProfileUpdate);
        }
    }
}
