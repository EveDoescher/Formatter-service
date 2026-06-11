package com.abntbuilder.formatter.rendering.component.bodycontent;

import com.abntbuilder.formatter.document.component.bodycontent.NumberedDisplayObject;
import com.abntbuilder.formatter.profile.model.component.bodycontent.DisplayObjectContinuationLabels;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

final class DisplayObjectRenderingState<T extends NumberedDisplayObject> {

    private final Map<String, Integer> numbersByGroupKey = new LinkedHashMap<>();
    private final Map<String, Integer> countsByGroupKey = new HashMap<>();
    private final Map<String, Integer> currentIndexByGroupKey = new HashMap<>();
    private final Map<String, String> sourceByGroupKey = new HashMap<>();

    DisplayObjectRenderingState(List<T> displayObjects) {
        int nextNumber = 1;

        for (T displayObject : displayObjects) {
            String groupKey = displayObject.displayGroupKey();
            countsByGroupKey.merge(groupKey, 1, Integer::sum);

            if (!numbersByGroupKey.containsKey(groupKey)) {
                numbersByGroupKey.put(groupKey, nextNumber);
                nextNumber++;
            }

            displayObject.source().ifPresent(source -> registerSource(groupKey, source));
        }
    }

    Optional<String> sourceFor(T displayObject) {
        return Optional.ofNullable(sourceByGroupKey.get(displayObject.displayGroupKey()));
    }

    DisplayObjectContinuationPart nextPart(T displayObject, DisplayObjectContinuationLabels labels) {
        String groupKey = displayObject.displayGroupKey();
        int index = currentIndexByGroupKey.merge(groupKey, 1, Integer::sum);
        int count = countsByGroupKey.get(groupKey);
        int number = numbersByGroupKey.get(groupKey);

        return new DisplayObjectContinuationPart(number, index, count, continuationLabel(index, count, labels));
    }

    private void registerSource(String groupKey, String source) {
        String previousSource = sourceByGroupKey.putIfAbsent(groupKey, source);

        if (previousSource != null && !previousSource.equals(source)) {
            throw new IllegalArgumentException(
                    "display object continuation group source must be consistent: " + groupKey
            );
        }
    }

    private Optional<String> continuationLabel(int index, int count, DisplayObjectContinuationLabels labels) {
        if (count == 1) {
            return Optional.empty();
        }

        if (index == 1) {
            return Optional.of(labels.first());
        }

        if (index == count) {
            return Optional.of(labels.last());
        }

        return Optional.of(labels.middle());
    }
}
