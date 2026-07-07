package com.abntbuilder.formatter.rendering.singlepage;

public record SinglePageRenderableArea(
        int physicalLineCapacity,
        int boundarySafetyLineCount,
        int safeLineCapacity,
        int physicalHeightTwips,
        int boundarySafetyHeightTwips,
        int safeHeightTwips
) {

    public SinglePageRenderableArea(
            int physicalLineCapacity,
            int boundarySafetyLineCount,
            int safeLineCapacity
    ) {
        this(
                physicalLineCapacity,
                boundarySafetyLineCount,
                safeLineCapacity,
                physicalLineCapacity,
                boundarySafetyLineCount,
                safeLineCapacity
        );
    }

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

        if (physicalHeightTwips < 0) {
            throw new IllegalArgumentException("physicalHeightTwips must not be negative.");
        }

        if (boundarySafetyHeightTwips < 0) {
            throw new IllegalArgumentException("boundarySafetyHeightTwips must not be negative.");
        }

        if (safeHeightTwips < 0) {
            throw new IllegalArgumentException("safeHeightTwips must not be negative.");
        }

        if (safeHeightTwips > physicalHeightTwips) {
            throw new IllegalArgumentException("safeHeightTwips must not exceed physicalHeightTwips.");
        }
    }
}
