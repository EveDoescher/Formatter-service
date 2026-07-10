package com.abntbuilder.formatter.shared.exception;

import java.util.List;

public class InvalidFontChoiceException extends RuntimeException {

    public InvalidFontChoiceException(String roleName, String chosen, List<String> allowed) {
        super("Invalid font choice for role \"" + roleName + "\": \"" + chosen
                + "\" is not in allowed values " + allowed + ".");
    }
}
