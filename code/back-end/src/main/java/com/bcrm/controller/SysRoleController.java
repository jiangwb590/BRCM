package com.bcrm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bcrm.common.PageRequest;
import com.bcrm.common.PageResult;
import com.bcrm.common.Result;
import com.bcrm.dto.SysRoleDTO;
import com.bcrm.entity.SysRole;
import com.bcrm.service.SysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色控制器
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Tag(name = "角色管理")
@RestController
@RequestMapping("/system/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService sysRoleService;

    /**
     * 分页查询角色
     */
    @Operation(summary = "分页查询角色")
    @GetMapping("/page")
    public Result<PageResult<SysRole>> page(PageRequest pageRequest, SysRole query) {
        Page<SysRole> page = sysRoleService.pageRoles(pageRequest, query);
        return Result.success(PageResult.of(page));
    }

    /**
     * 查询所有角色
     */
    @Operation(summary = "查询所有角色")
    @GetMapping("/all")
    public Result<List<SysRole>> listAll() {
        List<SysRole> list = sysRoleService.listAllRoles();
        return Result.success(list);
    }

    /**
     * 根据ID查询角色
     */
    @Operation(summary = "根据ID查询角色")
    @GetMapping("/{id}")
    public Result<SysRole> getById(@PathVariable Long id) {
        SysRole role = sysRoleService.getById(id);
        return Result.success(role);
    }

    /**
     * 新增角色
     */
    @Operation(summary = "新增角色")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody SysRoleDTO dto) {
        sysRoleService.addRole(dto);
        return Result.success();
    }

    /**
     * 修改角色
     */
    @Operation(summary = "修改角色")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody SysRoleDTO dto) {
        sysRoleService.updateRole(dto);
        return Result.success();
    }

    /**
     * 删除角色
     */
    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sysRoleService.deleteRole(id);
        return Result.success();
    }

    /**
     * 分配菜单权限
     */
    @Operation(summary = "分配菜单权限")
    @PostMapping("/{id}/menus")
    public Result<Void> assignMenus(@PathVariable Long id, @RequestBody List<Long> menuIds) {
        sysRoleService.assignMenus(id, menuIds);
        return Result.success();
    }

    /**
     * 获取角色菜单ID列表
     */
    @Operation(summary = "获取角色菜单ID列表")
    @GetMapping("/{id}/menus")
    public Result<List<Long>> getRoleMenuIds(@PathVariable Long id) {
        List<Long> menuIds = sysRoleService.getRoleMenuIds(id);
        return Result.success(menuIds);
    }
}
