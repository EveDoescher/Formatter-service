package com.abntbuilder.formatter.profile.resolution;

import com.abntbuilder.formatter.profile.model.DocumentProfile;
import com.abntbuilder.formatter.shared.exception.MissingProfileException;

import java.util.Map;

public final class InMemoryProfileProvider implements ProfileProvider {

    private final Map<String, DocumentProfile> profiles;

    public InMemoryProfileProvider(Map<String, DocumentProfile> profiles) {
        this.profiles = Map.copyOf(profiles);
    }

    @Override
    public DocumentProfile findById(String profileId) {
        DocumentProfile profile = profiles.get(profileId);

        if (profile == null) {
            throw new MissingProfileException(profileId);
        }

        return profile;
    }
}
