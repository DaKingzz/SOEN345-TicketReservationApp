package com.soen345.ticketreservation.performance;

public final class PerformanceTestConfig {

    private PerformanceTestConfig() {}

    // Replace with a dedicated Firebase test user account
    public static final String TEST_EMAIL = "x@gmail.com";
    public static final String TEST_PASSWORD = "xxxxxx";

    // Timeouts
    public static final long AUTH_TIMEOUT_SECONDS = 20;
    public static final long FIRESTORE_TIMEOUT_SECONDS = 30;

    // Dataset sizes
    public static final int SMALL_EVENT_DATASET = 10;
    public static final int MEDIUM_EVENT_DATASET = 100;
    public static final int LARGE_EVENT_DATASET = 500;

    // Performance thresholds (adjust if needed for your environment)
    public static final long MAX_EVENT_LOAD_MS_100 = 4000;
    public static final long MAX_SINGLE_RESERVATION_MS = 5000;
    public static final long MAX_CONCURRENT_TEST_TOTAL_MS = 15000;
}