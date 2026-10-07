package com.accenture.franchises.common.exception;

import java.util.UUID;

public class ResourceNotFoundException extends RuntimeException {

    private final UUID resourceId;

    public ResourceNotFoundException(String message) {
        super(message);
        this.resourceId = null;
    }

    public ResourceNotFoundException(String resourceType, UUID resourceId) {
        super("%s %s not found".formatted(resourceType, resourceId));
        this.resourceId = resourceId;
    }

    public ResourceNotFoundException(String resourceType, String resourceId) {
        super("%s %s not found".formatted(resourceType, resourceId));
        this.resourceId = null;
    }

    public UUID getResourceId() {
        return resourceId;
    }
}
