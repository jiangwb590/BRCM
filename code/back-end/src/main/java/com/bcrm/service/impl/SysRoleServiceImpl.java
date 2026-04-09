package com.bcrm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bcrm.common.PageRequest;
import com.bcrm.dto.SysRoleDTO;
import com.bcrm.entity.SysRole;
import com.bcrm.exception.BusinessException;
import com.bcrm.mapper.SysRoleMapper;
import com.bcrm.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 角色服务实现
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Page<SysRole> pageRoles(PageRequest pageRequest, SysRole query) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getRoleName())) {
            wrapper.like(SysRole::getRoleName, query.getRoleName());
        }
        if (query.getStatus() != null) {
            wrapper.eq(SysRole::getStatus, query.getStatus());
        }
        wrapper.orderByAsc(SysRole::getSort);
        return page(pageRequest.toPage(), wrapper);
    }

    @Override
    public List<SysRole> listAllRoles() {
        return list(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getStatus, 1)
                .orderByAsc(SysRole::getSort));
    }

    @Override
    public void addRole(SysRoleDTO dto) {
        SysRole entity = new SysRole();
        BeanUtils.copyProperties(dto, entity);
        save(entity);
    }

    @Override
    public void updateRole(SysRoleDTO dto) {
        SysRole entity = getById(dto.getId());
        if (entity == null) {
            throw new BusinessException("角色不存在");
        }
        BeanUtils.copyProperties(dto, entity);
        updateById(entity);
    }

    @Override
    public void deleteRole(Long id) {
        // 检查是否有关联用户
        Long userCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys_user_role WHERE role_id = ?", Long.class, id);
        if (userCount != null && userCount > 0) {
            throw new BusinessException("角色已分配给用户，不能删除");
        }
        removeById(id);
        // 删除角色菜单关联
        jdbcTemplate.update("DELETE FROM sys_role_menu WHERE role_id = ?", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignMenus(Long roleId, List<Long> menuIds) {
        // 删除原有关联
        jdbcTemplate.update("DELETE FROM sys_role_menu WHERE role_id = ?", roleId);
        
        // 插入新关联
        if (menuIds != null && !menuIds.isEmpty()) {
            for (Long menuId : menuIds) {
                jdbcTemplate.update(
                        "INSERT INTO sys_role_menu (role_id, menu_id) VALUES (?, ?)", 
                        roleId, menuId);
            }
        }
    }

    @Override
    public List<Long> getRoleMenuIds(Long roleId) {
        return jdbcTemplate.queryForList(
                "SELECT menu_id FROM sys_role_menu WHERE role_id = ?", 
                Long.class, roleId);
    }
}
