package com.kiwi.concurrency.factory;

import com.kiwi.concurrency.KiwiThreadPoolExecutor;

import java.util.Map;

public record ConcurrencyContainer(
        Map<String, KiwiThreadPoolExecutor> executors
) {
}
