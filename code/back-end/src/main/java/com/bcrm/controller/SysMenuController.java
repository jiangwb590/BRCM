package com.bcrm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bcrm.common.PageRequest;
import com.bcrm.common.PageResult;
import com.bcrm.common.Result;
import com.bcrm.entity.SysMenu;
import com.bcrm.security.LoginUser;
import com.bcrm.service.SysMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单控制器
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Tag(name = "菜单管理")
@RestController
@RequestMapping("/system/menu")
@RequiredArgsConstructor
public class SysMenuController {

    private final SysMenuService sysMenuService;

    /**
     * 分页查询菜单
     */
    @Operation(summary = "分页查询菜单")
    @GetMapping("/page")
    public Result<PageResult<SysMenu>> page(PageRequest pageRequest, SysMenu query) {
        Page<SysMenu> page = sysMenuService.pageMenus(pageRequest, query);
        return Result.success(PageResult.of(page));
    }

    /**
     * 查询菜单树
     */
    @Operation(summary = "查询菜单树")
    @GetMapping("/tree")
    public Result<List<SysMenu>> listMenuTree() {
        List<SysMenu> tree = sysMenuService.listMenuTree();
        return Result.success(tree);
    }

    /**
     * 获取用户菜单
     */
    @Operation(summary = "获取用户菜单")
    @GetMapping("/user")
    public Result<List<SysMenu>> getUserMenus(@AuthenticationPrincipal LoginUser loginUser) {
        List<SysMenu> menus = sysMenuService.getMenusByUserId(loginUser.getUser().getId());
        return Result.success(menus);
    }

    /**
     * 根据ID查询菜单
     */
    @Operation(summary = "根据ID查询菜单")
    @GetMapping("/{id}")
    public Result<SysMenu> getById(@PathVariable Long id) {
        SysMenu menu = sysMenuService.getById(id);
        return Result.success(menu);
    }

    /**
     * 新增菜单
     */
    @Operation(summary = "新增菜单")
    @PostMapping
    public Result<Void> add(@RequestBody SysMenu menu) {
        sysMenuService.addMenu(menu);
        return Result.success();
    }

    /**
     * 修改菜单
     */
    @Operation(summary = "修改菜单")
    @PutMapping
    public Result<Void> update(@RequestBody SysMenu menu) {
        sysMenuService.updateMenu(menu);
        return Result.success();
    }

    /**
     * 删除菜单
     */
    @Operation(summary = "删除菜单")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sysMenuService.deleteMenu(id);
        return Result.success();
    }
}
