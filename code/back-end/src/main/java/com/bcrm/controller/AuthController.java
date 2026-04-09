package com.bcrm.controller;

import com.bcrm.common.Result;
import com.bcrm.dto.LoginRequest;
import com.bcrm.security.LoginUser;
import com.bcrm.service.AuthService;
import com.bcrm.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Tag(name = "认证管理")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 登录
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        LoginVO loginVO = authService.login(request);
        return Result.success(loginVO);
    }

    /**
     * 获取当前用户信息
     */
    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    public Result<LoginVO> getUserInfo(@AuthenticationPrincipal LoginUser loginUser) {
        LoginVO userInfo = authService.getUserInfo(loginUser);
        return Result.success(userInfo);
    }

    /**
     * 登出
     */
    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout(@AuthenticationPrincipal LoginUser loginUser) {
        authService.logout(loginUser);
        return Result.success();
    }

    /**
     * 刷新Token
     */
    @Operation(summary = "刷新Token")
    @PostMapping("/refresh")
    public Result<LoginVO> refreshToken(@AuthenticationPrincipal LoginUser loginUser) {
        LoginVO loginVO = authService.refreshToken(loginUser);
        return Result.success(loginVO);
    }
}
