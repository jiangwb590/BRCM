package com.bcrm.utils;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JWT工具类测试
 *
 * @author BCRM
 * @since 2026-03-14
 */
class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "secret", "bcrm-test-secret-key-for-unit-testing-purpose-only-2024-bcrm");
        ReflectionTestUtils.setField(jwtUtils, "expiration", 86400L);
    }

    @Test
    @DisplayName("生成Token测试")
    void testGenerateToken() {
        // 执行测试
        String token = jwtUtils.generateToken(1L, "admin");

        // 验证结果
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    @DisplayName("解析Token测试")
    void testParseToken() {
        // 准备测试数据
        String token = jwtUtils.generateToken(1L, "admin");

        // 执行测试
        Claims claims = jwtUtils.parseToken(token);

        // 验证结果
        assertNotNull(claims);
        assertEquals("admin", claims.getSubject());
        assertEquals(1L, claims.get("userId", Long.class));
    }

    @Test
    @DisplayName("从Token获取用户ID测试")
    void testGetUserId() {
        // 准备测试数据
        String token = jwtUtils.generateToken(1L, "admin");

        // 执行测试
        Long userId = jwtUtils.getUserId(token);

        // 验证结果
        assertEquals(1L, userId);
    }

    @Test
    @DisplayName("从Token获取用户名测试")
    void testGetUsername() {
        // 准备测试数据
        String token = jwtUtils.generateToken(1L, "admin");

        // 执行测试
        String username = jwtUtils.getUsername(token);

        // 验证结果
        assertEquals("admin", username);
    }

    @Test
    @DisplayName("验证Token有效性测试")
    void testValidateToken() {
        // 准备测试数据
        String token = jwtUtils.generateToken(1L, "admin");

        // 执行测试
        boolean isValid = jwtUtils.validateToken(token);

        // 验证结果
        assertTrue(isValid);
    }

    @Test
    @DisplayName("验证无效Token测试")
    void testValidateInvalidToken() {
        // 执行测试
        boolean isValid = jwtUtils.validateToken("invalid-token");

        // 验证结果
        assertFalse(isValid);
    }

    @Test
    @DisplayName("刷新Token测试")
    void testRefreshToken() throws InterruptedException {
        // 准备测试数据
        String token = jwtUtils.generateToken(1L, "admin");
        
        // 等待一小段时间确保时间戳不同
        Thread.sleep(100);

        // 执行测试
        String newToken = jwtUtils.refreshToken(token);

        // 验证结果
        assertNotNull(newToken);
        
        // 验证新Token有效且包含相同的用户信息
        Long userId = jwtUtils.getUserId(newToken);
        String username = jwtUtils.getUsername(newToken);
        assertEquals(1L, userId);
        assertEquals("admin", username);
    }

    @Test
    @DisplayName("获取过期时间测试")
    void testGetExpiration() {
        // 执行测试
        Long expiration = jwtUtils.getExpiration();

        // 验证结果
        assertEquals(86400L, expiration);
    }
}
