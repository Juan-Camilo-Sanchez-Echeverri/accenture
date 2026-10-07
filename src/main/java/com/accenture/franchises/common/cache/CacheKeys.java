package com.accenture.franchises.common.cache;

import java.util.UUID;

public final class CacheKeys {

    private CacheKeys() {
    }

    public static String topProducts(UUID franchiseId) {
        return "franchise:top-products:" + franchiseId;
    }
}