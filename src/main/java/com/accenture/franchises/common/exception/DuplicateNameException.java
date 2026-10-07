package com.accenture.franchises.common.exception;

public class DuplicateNameException extends RuntimeException {

    public DuplicateNameException(String resourceType, String name) {
        super("%s named '%s' already exists".formatted(resourceType, name));
    }
}
