package com.bcrm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bcrm.common.PageRequest;
import com.bcrm.dto.SysUserDTO;
import com.bcrm.entity.SysUser;
import com.bcrm.entity.SysUserRole;
import com.bcrm.exception.BusinessException;
import com.bcrm.mapper.SysUserMapper;
import com.bcrm.mapper.SysUserRoleMapper;
import com.bcrm.service.SysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * 用户服务实现
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final PasswordEncoder passwordEncoder;
    private final SysUserRoleMapper sysUserRoleMapper;

    @Override
    public Page<SysUser> pageUsers(PageRequest pageRequest, SysUser query) {
        Page<SysUser> page = pageRequest.toPage();
        return baseMapper.selectUserPage(page, query.getUsername(), query.getRealName(), query.getPhone(), query.getStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addUser(SysUserDTO dto) {
        // 检查用户名是否存在
        if (this.checkUsernameExists(dto.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        
        // 检查手机号是否存在
        if (StringUtils.hasText(dto.getPhone()) && this.checkPhoneExists(dto.getPhone())) {
            throw new BusinessException("手机号已存在");
        }
        
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword() != null ? dto.getPassword() : "123456"));
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setAvatar(dto.getAvatar());
        user.setGender(dto.getGender());
        user.setStatus(dto.getStatus());
        user.setDeptId(dto.getDeptId());
        user.setRemark(dto.getRemark());
        
        this.save(user);
        
        // 保存用户角色关联
        saveUserRoles(user.getId(), dto.getRoleIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(SysUserDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("用户ID不能为空");
        }
        
        SysUser existUser = this.getById(dto.getId());
        if (existUser == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 检查用户名是否被其他用户使用
        if (!existUser.getUsername().equals(dto.getUsername()) && this.checkUsernameExists(dto.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        
        // 检查手机号是否被其他用户使用
        if (StringUtils.hasText(dto.getPhone()) && !dto.getPhone().equals(existUser.getPhone()) 
                && this.checkPhoneExists(dto.getPhone())) {
            throw new BusinessException("手机号已存在");
        }
        
        SysUser user = new SysUser();
        user.setId(dto.getId());
        user.setUsername(dto.getUsername());
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setAvatar(dto.getAvatar());
        user.setGender(dto.getGender());
        user.setStatus(dto.getStatus());
        user.setDeptId(dto.getDeptId());
        user.setRemark(dto.getRemark());
        
        this.updateById(user);
        
        // 更新用户角色关联
        if (dto.getRoleIds() != null && dto.getRoleIds().length > 0) {
            sysUserRoleMapper.deleteByUserId(dto.getId());
            saveUserRoles(dto.getId(), dto.getRoleIds());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        this.removeById(id);
        // 删除用户角色关联
        sysUserRoleMapper.deleteByUserId(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteUser(Long[] ids) {
        this.removeByIds(Arrays.asList(ids));
        // 删除用户角色关联
        for (Long id : ids) {
            sysUserRoleMapper.deleteByUserId(id);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long id) {
        SysUser user = this.getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 重置为默认密码 123456
        SysUser updateUser = new SysUser();
        updateUser.setId(id);
        updateUser.setPassword(passwordEncoder.encode("123456"));
        this.updateById(updateUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        SysUser user = this.getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        SysUser updateUser = new SysUser();
        updateUser.setId(id);
        updateUser.setStatus(status);
        this.updateById(updateUser);
    }

    @Override
    public List<SysUser> getBeauticians() {
        return baseMapper.selectBeauticians();
    }

    /**
     * 保存用户角色关联
     */
    private void saveUserRoles(Long userId, Long[] roleIds) {
        if (roleIds != null && roleIds.length > 0) {
            for (Long roleId : roleIds) {
                SysUserRole userRole = new SysUserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                sysUserRoleMapper.insert(userRole);
            }
        }
    }

    /**
     * 检查用户名是否存在
     */
    private boolean checkUsernameExists(String username) {
        return this.count(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
                .eq(SysUser::getDeleted, 0)) > 0;
    }

    /**
     * 检查手机号是否存在
     */
    private boolean checkPhoneExists(String phone) {
        return this.count(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getPhone, phone)
                .eq(SysUser::getDeleted, 0)) > 0;
    }
}