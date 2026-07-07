package com.abntbuilder.formatter.input.profile;

import com.abntbuilder.formatter.engine.model.profile.DocumentProfile;
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
