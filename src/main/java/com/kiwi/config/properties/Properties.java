package com.kiwi.config.properties;

//TODO all properties from this class will be moved to properties file in phase 5
public class Properties {
    public static final String PROTOCOL_VERSION = "1.0";
    public static final String INFO_SCHEMA_VERSION = "1.0";
    public static final int MAX_INFLIGHT_PER_CONNECTION = 32;
    public static final int RESPONSE_QUEUE_MAX_SIZE = 100000;

    public static final String SERVER_THREAD_POOL_NAME = "server-thread-pool";
    public static final String SERVER_THREAD_POOL_EXECUTOR_NAME = "server-executor";
    public static final String REJECTION_THREAD_POOL_NAME = "rejection-thread-pool";
    public static final String THREAD_NAME_PREFIX = "kiwi-thread";
    public static final int REJECTION_POOL_SIZE = 1;
    public static final int REJECTION_QUEUE_SIZE = 100;
    public static final int SERVER_THREAD_POOL_SIZE = 9;
    public static final int SERVER_THREAD_POOL_QUEUE_CAP = 1000;
}
