package com.englishassistant.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * Unified API response wrapper.
 * Matches the exact JSON shape the Vue frontend expects.
 * When there's an error: { "error": "message" }
 * When success without data: direct object (handled by controller returning Map)
 * When success with wrapper: { "data": ..., "total": ..., "page": ... }
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {
    private T data;
    private String error;
    private Long total;
    private Integer page;

    private Result() {}

    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.setData(data);
        return r;
    }

    public static <T> Result<T> error(String message) {
        Result<T> r = new Result<>();
        r.setError(message);
        return r;
    }
}
