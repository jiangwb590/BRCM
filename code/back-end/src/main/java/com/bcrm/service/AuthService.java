package com.bcrm.service;

import com.bcrm.dto.LoginRequest;
import com.bcrm.security.LoginUser;
import com.bcrm.vo.LoginVO;

/**
 * 认证服务接口
 *
 * @author BCRM
 * @since 2026-03-14
 */
public interface AuthService {

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return 登录信息
     */
    LoginVO login(LoginRequest request);

    /**
     * 获取用户信息
     *
     * @param loginUser 登录用户
     * @return 用户信息
     */
    LoginVO getUserInfo(LoginUser loginUser);

    /**
     * 用户登出
     *
     * @param loginUser 登录用户
     */
    void logout(LoginUser loginUser);

    /**
     * 刷新Token
     *
     * @param loginUser 登录用户
     * @return 新的登录信息
     */
    LoginVO refreshToken(LoginUser loginUser);
}
