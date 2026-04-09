package com.bcrm.service;

import com.bcrm.dto.LoginRequest;
import com.bcrm.entity.SysUser;
import com.bcrm.exception.BusinessException;
import com.bcrm.mapper.SysUserMapper;
import com.bcrm.security.LoginUser;
import com.bcrm.service.impl.AuthServiceImpl;
import com.bcrm.utils.JwtUtils;
import com.bcrm.vo.LoginVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 认证服务测试
 *
 * @author BCRM
 * @since 2026-03-14
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private SysUserMapper sysUserMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    private SysUser testUser;
    private LoginUser loginUser;

    @BeforeEach
    void setUp() {
        testUser = new SysUser();
        testUser.setId(1L);
        testUser.setUsername("admin");
        testUser.setRealName("管理员");
        testUser.setPassword("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH");
        testUser.setStatus(1);
        testUser.setCreateTime(LocalDateTime.now());

        loginUser = new LoginUser(testUser);
    }

    @Test
    @DisplayName("登录成功测试")
    void testLoginSuccess() {
        // 准备测试数据
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("123456");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(loginUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtils.generateToken(1L, "admin")).thenReturn("test-token");
        when(jwtUtils.getExpiration()).thenReturn(86400L);
        when(sysUserMapper.update(any(), any())).thenReturn(1);

        // 执行测试
        LoginVO result = authService.login(request);

        // 验证结果
        assertNotNull(result);
        assertEquals("test-token", result.getToken());
        assertEquals("admin", result.getUsername());
        assertEquals("管理员", result.getRealName());
        assertEquals("Bearer", result.getTokenType());
    }

    @Test
    @DisplayName("登录失败-密码错误测试")
    void testLoginFailed() {
        // 准备测试数据
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // 执行测试
        assertThrows(BusinessException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("登录失败-账号被禁用测试")
    void testLoginAccountDisabled() {
        // 准备测试数据
        testUser.setStatus(0);
        LoginUser disabledUser = new LoginUser(testUser);
        
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("123456");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(disabledUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        // 执行测试
        assertThrows(BusinessException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("获取用户信息测试")
    void testGetUserInfo() {
        // 准备测试数据
        loginUser.setToken("test-token");
        when(jwtUtils.getExpiration()).thenReturn(86400L);

        // 执行测试
        LoginVO result = authService.getUserInfo(loginUser);

        // 验证结果
        assertNotNull(result);
        assertEquals("test-token", result.getToken());
        assertEquals("admin", result.getUsername());
        assertEquals("管理员", result.getRealName());
    }

    @Test
    @DisplayName("刷新Token测试")
    void testRefreshToken() {
        // 准备测试数据
        when(jwtUtils.generateToken(1L, "admin")).thenReturn("new-token");
        when(jwtUtils.getExpiration()).thenReturn(86400L);

        // 执行测试
        LoginVO result = authService.refreshToken(loginUser);

        // 验证结果
        assertNotNull(result);
        assertEquals("new-token", result.getToken());
    }

    @Test
    @DisplayName("登出测试")
    void testLogout() {
        // 执行测试，不应该抛出异常
        assertDoesNotThrow(() -> authService.logout(loginUser));
    }
}