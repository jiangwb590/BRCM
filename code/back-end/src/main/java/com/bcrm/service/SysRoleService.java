package com.bcrm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bcrm.common.PageRequest;
import com.bcrm.dto.SysRoleDTO;
import com.bcrm.entity.SysRole;

import java.util.List;

/**
 * 角色服务接口
 *
 * @author BCRM
 * @since 2026-03-14
 */
public interface SysRoleService extends IService<SysRole> {

    /**
     * 分页查询角色
     */
    Page<SysRole> pageRoles(PageRequest pageRequest, SysRole query);

    /**
     * 查询所有角色
     */
    List<SysRole> listAllRoles();

    /**
     * 新增角色
     */
    void addRole(SysRoleDTO dto);

    /**
     * 修改角色
     */
    void updateRole(SysRoleDTO dto);

    /**
     * 删除角色
     */
    void deleteRole(Long id);

    /**
     * 分配菜单权限
     */
    void assignMenus(Long roleId, List<Long> menuIds);

    /**
     * 获取角色菜单ID列表
     */
    List<Long> getRoleMenuIds(Long roleId);
}
