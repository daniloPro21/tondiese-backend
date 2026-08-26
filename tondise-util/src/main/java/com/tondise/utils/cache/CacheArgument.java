package com.tondise.utils.cache;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CacheArgument(Object value, LocalDateTime expireAt) {
}
