package com.abntbuilder.formatter.rendering.layout.singlepage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class SinglePageTextLineBreaker {

    public List<String> breakText(String text, int maxCharactersPerLine) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("text must not be blank.");
        }

        if (maxCharactersPerLine <= 0) {
            throw new IllegalArgumentException("maxCharactersPerLine must be greater than zero.");
        }

        List<String> result = new ArrayList<>();

        String[] explicitLines = text.strip().split("\\R");

        for (String explicitLine : explicitLines) {
            result.addAll(breakSingleLine(explicitLine, maxCharactersPerLine));
        }

        return List.copyOf(result);
    }

    private static List<String> breakSingleLine(String text, int maxCharactersPerLine) {
        Objects.requireNonNull(text, "text must not be null");

        String normalized = text.trim().replaceAll("\\s+", " ");

        if (normalized.isBlank()) {
            throw new IllegalArgumentException("text must not be blank.");
        }

        List<String> lines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();

        String[] words = normalized.split(" ");

        for (String word : words) {
            if (word.length() > maxCharactersPerLine) {
                throw new IllegalArgumentException("word length exceeds maxCharactersPerLine.");
            }

            if (currentLine.isEmpty()) {
                currentLine.append(word);
                continue;
            }

            int candidateLength = currentLine.length() + 1 + word.length();

            if (candidateLength <= maxCharactersPerLine) {
                currentLine.append(' ').append(word);
            } else {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            }
        }

        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
        }

        return List.copyOf(lines);
    }
}