package com.bcrm.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bcrm.entity.SysUser;
import com.bcrm.mapper.SysMenuMapper;
import com.bcrm.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 用户详情服务实现
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper sysUserMapper;
    private final SysMenuMapper sysMenuMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 查询用户
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username)
                        .eq(SysUser::getDeleted, 0)
        );
        
        if (user == null) {
            log.warn("用户不存在: {}", username);
            throw new UsernameNotFoundException("用户不存在");
        }
        
        // 构建登录用户
        LoginUser loginUser = new LoginUser(user);
        
        // 查询用户权限
        List<String> perms = sysMenuMapper.selectPermsByUserId(user.getId());
        Set<String> permissions = new HashSet<>(perms);
        
        // 如果是超级管理员角色（role_id=1），给予所有权限
        Long roleId = sysUserMapper.selectRoleIdByUserId(user.getId());
        if (roleId != null && roleId == 1) {
            // 超级管理员拥有超级权限
            permissions.add("*:*:*");
        }
        
        loginUser.setPermissions(permissions);
        
        // 查询用户角色（暂时只设置一个角色）
        Set<String> roles = new HashSet<>();
        if (roleId != null) {
            roles.add(String.valueOf(roleId));
        }
        loginUser.setRoles(roles);
        
        log.info("用户 {} 登录，权限数量: {}", username, permissions.size());
        
        return loginUser;
    }
}
