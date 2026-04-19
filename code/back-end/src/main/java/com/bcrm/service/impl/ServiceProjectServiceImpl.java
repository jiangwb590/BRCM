package com.bcrm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bcrm.common.PageRequest;
import com.bcrm.dto.ServiceProjectDTO;
import com.bcrm.entity.ServiceProject;
import com.bcrm.exception.BusinessException;
import com.bcrm.mapper.ServiceProjectMapper;
import com.bcrm.service.ServiceProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 服务项目服务实现
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceProjectServiceImpl extends ServiceImpl<ServiceProjectMapper, ServiceProject> implements ServiceProjectService {

    @Override
    public Page<ServiceProject> pageProjects(PageRequest pageRequest, ServiceProject query) {
        log.info("分页参数: current={}, size={}", pageRequest.getCurrent(), pageRequest.getSize());
        Page<ServiceProject> page = pageRequest.toPage();
        
        LambdaQueryWrapper<ServiceProject> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getName()), ServiceProject::getName, query.getName());
        wrapper.eq(query.getCategoryId() != null, ServiceProject::getCategoryId, query.getCategoryId());
        wrapper.eq(query.getStatus() != null, ServiceProject::getStatus, query.getStatus());
        wrapper.orderByAsc(ServiceProject::getSort);
        wrapper.orderByDesc(ServiceProject::getCreateTime);
        
        return this.page(page, wrapper);
    }

    @Override
    public List<ServiceProject> listActiveProjects() {
        return this.list(new LambdaQueryWrapper<ServiceProject>()
                .eq(ServiceProject::getStatus, 1)
                .orderByAsc(ServiceProject::getSort));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addProject(ServiceProjectDTO dto) {
        ServiceProject project = new ServiceProject();
        project.setProjectCode(dto.getProjectCode());
        project.setName(dto.getName());
        project.setCategoryName(dto.getCategoryName());
        project.setImage(dto.getImage());
        project.setDescription(dto.getDescription());
        project.setDuration(dto.getDuration());
        project.setPrice(dto.getPrice());
        project.setMemberPrice(dto.getMemberPrice());
        project.setCostPrice(dto.getCostPrice());
        project.setIsPackage(dto.getIsPackage() != null ? dto.getIsPackage() : 0);
        project.setIsCardProject(dto.getIsCardProject() != null ? dto.getIsCardProject() : 0);
        project.setStatus(dto.getStatus()==null?1:dto.getStatus());
        project.setSort(dto.getSort() != null ? dto.getSort() : 0);
        
        this.save(project);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProject(ServiceProjectDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("项目ID不能为空");
        }
        
        ServiceProject existProject = this.getById(dto.getId());
        if (existProject == null) {
            throw new BusinessException("项目不存在");
        }
        
        ServiceProject project = new ServiceProject();
        project.setId(dto.getId());
        project.setProjectCode(dto.getProjectCode());
        project.setName(dto.getName());
        project.setCategoryName(dto.getCategoryName());
        project.setImage(dto.getImage());
        project.setDescription(dto.getDescription());
        project.setDuration(dto.getDuration());
        project.setPrice(dto.getPrice());
        project.setMemberPrice(dto.getMemberPrice());
        project.setCostPrice(dto.getCostPrice());
        project.setIsPackage(dto.getIsPackage());
        project.setIsCardProject(dto.getIsCardProject());
        project.setStatus(dto.getStatus());
        project.setSort(dto.getSort());
        
        this.updateById(project);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProject(Long id) {
        this.removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        ServiceProject project = this.getById(id);
        if (project == null) {
            throw new BusinessException("项目不存在");
        }
        
        ServiceProject updateProject = new ServiceProject();
        updateProject.setId(id);
        updateProject.setStatus(status);
        this.updateById(updateProject);
    }
}
