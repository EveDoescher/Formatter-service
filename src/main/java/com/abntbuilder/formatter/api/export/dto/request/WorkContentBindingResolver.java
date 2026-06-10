package com.abntbuilder.formatter.api.export.dto.request;

import com.abntbuilder.formatter.profile.model.component.ComponentContentBindings;

import java.util.List;
import java.util.Objects;

final class WorkContentBindingResolver {

    private final String componentId;
    private final AcademicWorkRequest work;
    private final ComponentContentBindings bindings;

    WorkContentBindingResolver(
            String componentId,
            AcademicWorkRequest work,
            ComponentContentBindings bindings
    ) {
        this.componentId = Objects.requireNonNull(componentId, "componentId must not be null");
        this.work = work;
        this.bindings = Objects.requireNonNull(bindings, "bindings must not be null");
    }

    String resolveString(String fieldName, String explicitValue) {
        return resolve(fieldName, explicitValue, String.class);
    }

    @SuppressWarnings("unchecked")
    List<String> resolveStringList(String fieldName, List<String> explicitValue) {
        return (List<String>) resolve(fieldName, explicitValue, List.class);
    }

    AcademicPersonRequest resolveAcademicPerson(String fieldName, AcademicPersonRequest explicitValue) {
        return resolve(fieldName, explicitValue, AcademicPersonRequest.class);
    }

    TitlePageNatureRequest resolveTitlePageNature(String fieldName, TitlePageNatureRequest explicitValue) {
        if (explicitValue != null) {
            return explicitValue;
        }

        AcademicWorkNatureRequest value = resolve(fieldName, null, AcademicWorkNatureRequest.class);

        if (value == null) {
            return null;
        }

        return value.toTitlePageNatureRequest();
    }

    ApprovalSheetNatureRequest resolveApprovalSheetNature(String fieldName, ApprovalSheetNatureRequest explicitValue) {
        if (explicitValue != null) {
            return explicitValue;
        }

        AcademicWorkNatureRequest value = resolve(fieldName, null, AcademicWorkNatureRequest.class);

        if (value == null) {
            return null;
        }

        return value.toApprovalSheetNatureRequest();
    }

    private <T> T resolve(String fieldName, T explicitValue, Class<T> expectedType) {
        if (explicitValue != null) {
            return explicitValue;
        }

        return bindings.sourceFor(fieldName)
                .map(source -> resolveBoundValue(fieldName, source, expectedType))
                .orElse(null);
    }

    @SuppressWarnings("unchecked")
    private <T> T resolveBoundValue(String fieldName, String source, Class<T> expectedType) {
        if (work == null) {
            return null;
        }

        Object value = work.valueFor(source);

        if (value == null) {
            return null;
        }

        if (!expectedType.isInstance(value)) {
            throw new IllegalArgumentException(
                    "Binding " + componentId + "." + fieldName + " expects "
                            + expectedType.getSimpleName()
                            + " but " + source + " provides "
                            + value.getClass().getSimpleName()
            );
        }

        return (T) value;
    }
}
