package com.kiwi.config.registry;

public record ConfigKey(
       String name,
       String envName,
       String defaultValue,
       ValueParser valueParser
) {
}
