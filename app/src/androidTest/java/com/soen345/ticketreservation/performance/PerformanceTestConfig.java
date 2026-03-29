package com.soen345.ticketreservation.performance;

public final class PerformanceTestConfig {

    private PerformanceTestConfig() {}

    // Test Credentials
    public static final String TEST_EMAIL = "a@a.com";
    public static final String TEST_PASSWORD = "aaaaaa";

    // Timeouts
    public static final long AUTH_TIMEOUT_SECONDS = 20;
    public static final long FIRESTORE_TIMEOUT_SECONDS = 30;

    // Dataset sizes
    public static final int SMALL_EVENT_DATASET = 10;
    public static final int MEDIUM_EVENT_DATASET = 100;
    public static final int LARGE_EVENT_DATASET = 500;

    // Performance thresholds
    public static final long MAX_EVENT_LOAD_MS_100 = 4000;
    public static final long MAX_SINGLE_RESERVATION_MS = 5000;
    public static final long MAX_CONCURRENT_TEST_TOTAL_MS = 15000;
}