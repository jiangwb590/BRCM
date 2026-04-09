package com.bcrm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bcrm.common.PageRequest;
import com.bcrm.dto.SysUserDTO;
import com.bcrm.entity.SysUser;

import java.util.List;

/**
 * 用户服务接口
 *
 * @author BCRM
 * @since 2026-03-14
 */
public interface SysUserService extends IService<SysUser> {

    /**
     * 分页查询用户
     *
     * @param pageRequest 分页参数
     * @param query 查询条件
     * @return 分页结果
     */
    Page<SysUser> pageUsers(PageRequest pageRequest, SysUser query);

    /**
     * 新增用户
     *
     * @param dto 用户信息
     */
    void addUser(SysUserDTO dto);

    /**
     * 修改用户
     *
     * @param dto 用户信息
     */
    void updateUser(SysUserDTO dto);

    /**
     * 删除用户
     *
     * @param id 用户ID
     */
    void deleteUser(Long id);

    /**
     * 批量删除用户
     *
     * @param ids 用户ID数组
     */
    void batchDeleteUser(Long[] ids);

    /**
     * 重置密码
     *
     * @param id 用户ID
     */
    void resetPassword(Long id);

    /**
     * 修改用户状态
     *
     * @param id 用户ID
     * @param status 状态
     */
    void updateStatus(Long id, Integer status);

    /**
     * 获取美容师列表
     *
     * @return 美容师列表
     */
    List<SysUser> getBeauticians();
}
