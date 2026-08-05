package com.abntbuilder.formatter.application.export;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class InMemoryGeneratedDocxExportStore implements GeneratedDocxExportStore {

    private static final Duration TTL = Duration.ofMinutes(30);

    private final ConcurrentMap<String, GeneratedDocxExport> exportsById = new ConcurrentHashMap<>();

    @Override
    public GeneratedDocxExport save(String fileName, byte[] bytes) {
        GeneratedDocxExport generatedExport = new GeneratedDocxExport(
                UUID.randomUUID().toString(),
                fileName,
                bytes,
                Instant.now()
        );

        exportsById.put(generatedExport.id(), generatedExport);

        return generatedExport;
    }

    @Override
    public Optional<GeneratedDocxExport> findById(String id) {
        return Optional.ofNullable(exportsById.get(id));
    }

    @Scheduled(fixedDelay = 10 * 60 * 1000)
    void evictExpired() {
        Instant cutoff = Instant.now().minus(TTL);
        exportsById.values().removeIf(export -> export.createdAt().isBefore(cutoff));
    }
}