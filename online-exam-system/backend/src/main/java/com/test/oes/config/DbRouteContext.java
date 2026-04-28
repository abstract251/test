package com.test.oes.config;

public final class DbRouteContext {

    private static final ThreadLocal<String> ROUTE_OVERRIDE = new ThreadLocal<>();

    private DbRouteContext() {
    }

    public static void forcePrimary() {
        ROUTE_OVERRIDE.set(ReadWriteRoutingDataSource.PRIMARY);
    }

    public static String currentOverride() {
        return ROUTE_OVERRIDE.get();
    }

    public static void clear() {
        ROUTE_OVERRIDE.remove();
    }
}
