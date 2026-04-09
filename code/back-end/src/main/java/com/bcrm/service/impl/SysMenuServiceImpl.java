package com.bcrm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bcrm.common.PageRequest;
import com.bcrm.entity.SysMenu;
import com.bcrm.exception.BusinessException;
import com.bcrm.mapper.SysMenuMapper;
import com.bcrm.service.SysMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜单服务实现
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    @Override
    public Page<SysMenu> pageMenus(PageRequest pageRequest, SysMenu query) {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getMenuName())) {
            wrapper.like(SysMenu::getMenuName, query.getMenuName());
        }
        if (query.getStatus() != null) {
            wrapper.eq(SysMenu::getStatus, query.getStatus());
        }
        wrapper.orderByAsc(SysMenu::getSort);
        return page(pageRequest.toPage(), wrapper);
    }

    @Override
    public List<SysMenu> listMenuTree() {
        // 查询所有启用的菜单（包括按钮）
        List<SysMenu> allMenus = list(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getStatus, 1)
                .orderByAsc(SysMenu::getSort));
        // 构建完整树形结构
        return buildMenuTree(allMenus, 0L);
    }

    /**
     * 递归构建菜单树
     */
    private List<SysMenu> buildMenuTree(List<SysMenu> menus, Long parentId) {
        List<SysMenu> result = new ArrayList<>();
        for (SysMenu menu : menus) {
            if (parentId.equals(menu.getParentId())) {
                // 递归查找子菜单
                menu.setChildren(buildMenuTree(menus, menu.getId()));
                result.add(menu);
            }
        }
        return result;
    }

    @Override
    public List<SysMenu> getMenusByUserId(Long userId) {
        return baseMapper.selectMenusByUserId(userId);
    }

    @Override
    public List<String> getPermsByUserId(Long userId) {
        return baseMapper.selectPermsByUserId(userId);
    }

    @Override
    public List<String> getPermsByRoleId(Long roleId) {
        return baseMapper.selectPermsByRoleId(roleId);
    }

    @Override
    public void addMenu(SysMenu menu) {
        save(menu);
    }

    @Override
    public void updateMenu(SysMenu menu) {
        SysMenu entity = getById(menu.getId());
        if (entity == null) {
            throw new BusinessException("菜单不存在");
        }
        updateById(menu);
    }

    @Override
    public void deleteMenu(Long id) {
        // 检查是否有子菜单
        long count = count(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getParentId, id));
        if (count > 0) {
            throw new BusinessException("存在子菜单，不能删除");
        }
        removeById(id);
    }
}
