package com.abntbuilder.formatter.input.profile;

import com.abntbuilder.formatter.input.profile.ProfileDefinition;
import com.abntbuilder.formatter.engine.model.profile.DocumentProfile;
import com.abntbuilder.formatter.input.profile.ProfileProvider;
import com.abntbuilder.formatter.shared.exception.MissingProfileException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class ClasspathJsonProfileProvider implements ProfileProvider {

    private static final String DEFAULT_PROFILE_RESOURCE_PATTERN = "classpath*:/profiles/*.json";

    private final Map<String, DocumentProfile> profiles;

    public ClasspathJsonProfileProvider() {
        this(new ObjectMapper(), DEFAULT_PROFILE_RESOURCE_PATTERN);
    }

    ClasspathJsonProfileProvider(ObjectMapper objectMapper, String resourcePattern) {
        Objects.requireNonNull(objectMapper, "objectMapper must not be null");
        requireNonBlank(resourcePattern, "resourcePattern");
        this.profiles = loadProfiles(objectMapper, resourcePattern);
    }

    @Override
    public DocumentProfile findById(String profileId) {
        DocumentProfile profile = profiles.get(profileId);

        if (profile == null) {
            throw new MissingProfileException(profileId);
        }

        return profile;
    }

    private static Map<String, DocumentProfile> loadProfiles(ObjectMapper objectMapper, String resourcePattern) {
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver().getResources(resourcePattern);
            Map<String, DocumentProfile> loadedProfiles = new LinkedHashMap<>();

            if (resources.length == 0) {
                throw new IllegalStateException("No profile JSON resources found for pattern: " + resourcePattern);
            }

            for (Resource resource : resources) {
                DocumentProfile profile = loadProfile(objectMapper, resource);

                if (loadedProfiles.putIfAbsent(profile.id(), profile) != null) {
                    throw new IllegalStateException("Duplicate profile id in JSON resources: " + profile.id());
                }
            }

            return Map.copyOf(loadedProfiles);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load profile JSON resources.", exception);
        }
    }

    private static DocumentProfile loadProfile(ObjectMapper objectMapper, Resource resource) throws IOException {
        try (InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readValue(inputStream, ProfileDefinition.class).toDomain();
        }
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }
}
