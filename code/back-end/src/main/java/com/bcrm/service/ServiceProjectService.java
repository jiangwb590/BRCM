package com.bcrm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bcrm.common.PageRequest;
import com.bcrm.dto.ServiceProjectDTO;
import com.bcrm.entity.ServiceProject;

import java.util.List;

/**
 * 服务项目服务接口
 *
 * @author BCRM
 * @since 2026-03-14
 */
public interface ServiceProjectService extends IService<ServiceProject> {

    /**
     * 分页查询项目
     *
     * @param pageRequest 分页参数
     * @param query 查询条件
     * @return 分页结果
     */
    Page<ServiceProject> pageProjects(PageRequest pageRequest, ServiceProject query);

    /**
     * 查询所有上架项目
     *
     * @return 项目列表
     */
    List<ServiceProject> listActiveProjects();

    /**
     * 新增项目
     *
     * @param dto 项目信息
     */
    void addProject(ServiceProjectDTO dto);

    /**
     * 修改项目
     *
     * @param dto 项目信息
     */
    void updateProject(ServiceProjectDTO dto);

    /**
     * 删除项目
     *
     * @param id 项目ID
     */
    void deleteProject(Long id);

    /**
     * 修改项目状态
     *
     * @param id 项目ID
     * @param status 状态
     */
    void updateStatus(Long id, Integer status);
}
