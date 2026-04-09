package com.bcrm.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bcrm.dto.LoginRequest;
import com.bcrm.entity.SysMenu;
import com.bcrm.entity.SysRole;
import com.bcrm.entity.SysUser;
import com.bcrm.exception.BusinessException;
import com.bcrm.mapper.SysMenuMapper;
import com.bcrm.mapper.SysUserMapper;
import com.bcrm.mapper.SysUserRoleMapper;
import com.bcrm.security.LoginUser;
import com.bcrm.service.AuthService;
import com.bcrm.service.SysMenuService;
import com.bcrm.service.SysRoleService;
import com.bcrm.utils.JwtUtils;
import com.bcrm.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 认证服务实现
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final SysUserMapper sysUserMapper;
    private final SysMenuService sysMenuService;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleService sysRoleService;
    private final SysMenuMapper sysMenuMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO login(LoginRequest request) {
        // 认证
        Authentication authentication;
        try {
            UsernamePasswordAuthenticationToken authToken = 
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());
            authentication = authenticationManager.authenticate(authToken);
        } catch (Exception e) {
            log.warn("用户登录失败: {} - {}", request.getUsername(), e.getMessage());
            throw new BusinessException("用户名或密码错误");
        }

        // 获取登录用户
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        
        // 检查用户状态
        if (loginUser.getUser().getStatus() != 1) {
            throw new BusinessException("账号已被禁用");
        }

        // 生成Token
        String token = jwtUtils.generateToken(loginUser.getUserId(), loginUser.getUsername());

        // 更新最后登录信息
        sysUserMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                .set(SysUser::getLastLoginTime, LocalDateTime.now())
                .eq(SysUser::getId, loginUser.getUser().getId())
        );

        // 构建返回信息
        return buildLoginVO(loginUser, token);
    }

    @Override
    public LoginVO getUserInfo(LoginUser loginUser) {
        return buildLoginVO(loginUser, loginUser.getToken());
    }

    @Override
    public void logout(LoginUser loginUser) {
        // 清除SecurityContext
        SecurityContextHolder.clearContext();
        log.info("用户登出: {}", loginUser.getUsername());
    }

    @Override
    public LoginVO refreshToken(LoginUser loginUser) {
        // 生成新Token
        String newToken = jwtUtils.generateToken(loginUser.getUserId(), loginUser.getUsername());
        loginUser.setToken(newToken);
        
        return buildLoginVO(loginUser, newToken);
    }

    /**
     * 构建登录响应
     */
    private LoginVO buildLoginVO(LoginUser loginUser, String token) {
        SysUser user = loginUser.getUser();
        
        // 查询用户角色
        List<Long> roleIds = sysUserRoleMapper.selectRoleIdsByUserId(user.getId());
        List<String> roles = new ArrayList<>();
        String roleName = "用户";
        
        if (!roleIds.isEmpty()) {
            List<SysRole> roleList = sysRoleService.listByIds(roleIds);
            roles = roleList.stream().map(SysRole::getRoleCode).collect(Collectors.toList());
            roleName = roleList.stream().map(SysRole::getRoleName).findFirst().orElse("用户");
        }
        
        // 查询用户权限
        List<String> permissions = new ArrayList<>();
        
        // 判断是否为超级管理员（角色ID=1）
        if (roleIds.contains(1L)) {
            // 超级管理员拥有所有权限
            permissions = Collections.singletonList("*:*:*");
        } else if (!roleIds.isEmpty()) {
            // 根据角色查询权限
            Set<String> permSet = new HashSet<>();
            for (Long roleId : roleIds) {
                List<String> perms = sysMenuMapper.selectPermsByRoleId(roleId);
                permSet.addAll(perms);
            }
            permissions = new ArrayList<>(permSet);
        }
        
        return LoginVO.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtUtils.getExpiration())
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .avatar(user.getAvatar())
                .roles(roles)
                .roleName(roleName)
                .permissions(permissions)
                .build();
    }
}
