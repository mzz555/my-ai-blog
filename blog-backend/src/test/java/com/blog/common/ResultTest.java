package com.blog.common;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ResultTest {
    @Test
    void success_shouldReturnCode200AndData() {
        Result<String> result = Result.success("hello");
        assertEquals(200, result.getCode());
        assertEquals("hello", result.getData());
        assertNull(result.getMessage());
    }

    @Test
    void error_shouldReturnCodeAndMessage() {
        Result<Void> result = Result.error(400, "参数错误");
        assertEquals(400, result.getCode());
        assertEquals("参数错误", result.getMessage());
        assertNull(result.getData());
    }
}
