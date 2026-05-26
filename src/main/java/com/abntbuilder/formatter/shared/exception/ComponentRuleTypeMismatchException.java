package com.abntbuilder.formatter.shared.exception;

import com.abntbuilder.formatter.profile.model.component.ComponentRule;

public class ComponentRuleTypeMismatchException extends RuntimeException {

    public ComponentRuleTypeMismatchException(
            String componentId,
            Class<? extends ComponentRule> expectedType,
            ComponentRule actualRule
    ) {
        super("Component rule for id: " + componentId
                + " must be " + expectedType.getSimpleName()
                + " but was " + actualRule.getClass().getSimpleName() + ".");
    }
}