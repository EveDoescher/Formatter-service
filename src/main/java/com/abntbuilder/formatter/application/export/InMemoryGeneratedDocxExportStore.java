package com.abntbuilder.formatter.application.export;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class InMemoryGeneratedDocxExportStore implements GeneratedDocxExportStore {

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
}