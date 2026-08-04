package com.abntbuilder.formatter.input.profile;

import com.abntbuilder.formatter.engine.model.profile.DocumentProfile;
import com.abntbuilder.formatter.shared.exception.MissingProfileException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.List;

@Component
@ConditionalOnProperty(name = "profile.service.url")
public class HttpProfileProvider implements ProfileProvider {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String profileServiceUrl;

    public HttpProfileProvider(
            ObjectMapper objectMapper,
            @Value("${profile.service.url}") String profileServiceUrl,
            @Value("${profile.service.connect-timeout-ms:3000}") int connectTimeoutMs,
            @Value("${profile.service.read-timeout-ms:10000}") int readTimeoutMs
    ) {
        this.objectMapper = objectMapper;
        this.profileServiceUrl = profileServiceUrl;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);
        this.restTemplate = new RestTemplate(factory);
    }

    @Override
    public DocumentProfile findById(String profileId) {
        String url = profileServiceUrl + "/profiles/" + profileId;
        String json;
        try {
            json = restTemplate.getForObject(url, String.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new MissingProfileException(profileId);
        }

        try {
            ProfileDefinition def = objectMapper.readValue(json, ProfileDefinition.class);
            return def.toDomain();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse profile " + profileId + ": " + e.getMessage(), e);
        }
    }

    @Override
    public Collection<DocumentProfile> allProfiles() {
        return List.of();
    }
}
