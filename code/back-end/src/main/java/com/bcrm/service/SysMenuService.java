package com.bcrm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bcrm.common.PageRequest;
import com.bcrm.entity.SysMenu;

import java.util.List;

/**
 * 菜单服务接口
 *
 * @author BCRM
 * @since 2026-03-14
 */
public interface SysMenuService extends IService<SysMenu> {

    /**
     * 分页查询菜单
     */
    Page<SysMenu> pageMenus(PageRequest pageRequest, SysMenu query);

    /**
     * 查询菜单树
     */
    List<SysMenu> listMenuTree();

    /**
     * 根据用户ID查询菜单
     */
    List<SysMenu> getMenusByUserId(Long userId);

    /**
     * 根据用户ID查询权限标识列表
     */
    List<String> getPermsByUserId(Long userId);

    /**
     * 根据角色ID查询权限标识列表
     */
    List<String> getPermsByRoleId(Long roleId);

    /**
     * 新增菜单
     */
    void addMenu(SysMenu menu);

    /**
     * 修改菜单
     */
    void updateMenu(SysMenu menu);

    /**
     * 删除菜单
     */
    void deleteMenu(Long id);
}