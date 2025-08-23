package com.kiwi.dto;

import com.kiwi.persistent.dto.Key;
import com.kiwi.persistent.dto.Value;

public record DataRequest(
    Key key,
    Value data
) {
}
