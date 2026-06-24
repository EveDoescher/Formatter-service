package com.abntbuilder.formatter.document.component.listofsymbols;

public record SymbolEntry(String symbol, String meaning) {
    public SymbolEntry {
        requireNonBlank(symbol, "symbol");
        requireNonBlank(meaning, "meaning");
    }

    private static void requireNonBlank(String v, String f) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(f + " must not be blank.");
    }
}
