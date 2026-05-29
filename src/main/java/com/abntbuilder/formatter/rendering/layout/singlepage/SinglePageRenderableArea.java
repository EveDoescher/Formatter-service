package com.abntbuilder.formatter.rendering.layout.singlepage;

public record SinglePageRenderableArea(
        int physicalLineCapacity,
        int boundarySafetyLineCount,
        int safeLineCapacity
) {

    public SinglePageRenderableArea {
        if (physicalLineCapacity < 0) {
            throw new IllegalArgumentException("physicalLineCapacity must not be negative.");
        }

        if (boundarySafetyLineCount < 0) {
            throw new IllegalArgumentException("boundarySafetyLineCount must not be negative.");
        }

        if (safeLineCapacity < 0) {
            throw new IllegalArgumentException("safeLineCapacity must not be negative.");
        }

        if (safeLineCapacity > physicalLineCapacity) {
            throw new IllegalArgumentException("safeLineCapacity must not exceed physicalLineCapacity.");
        }
    }
}
