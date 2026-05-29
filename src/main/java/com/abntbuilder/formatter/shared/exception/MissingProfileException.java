package com.abntbuilder.formatter.shared.exception;

public class MissingProfileException extends RuntimeException {

    public MissingProfileException(String profileId) {
        super("Profile not found for id: " + profileId);
    }
}
