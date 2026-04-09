package com.bcrm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bcrm.annotation.HasPermission;
import com.bcrm.annotation.OperLog;
import com.bcrm.aspect.OperLogContextHolder;
import com.bcrm.common.PageRequest;
import com.bcrm.common.PageResult;
import com.bcrm.common.Result;
import com.bcrm.dto.ServiceProjectDTO;
import com.bcrm.entity.ServiceProject;
import com.bcrm.service.ServiceProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 服务项目控制器
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Tag(name = "项目管理")
@RestController
@RequestMapping("/project")
@RequiredArgsConstructor
public class ServiceProjectController {

    private final ServiceProjectService serviceProjectService;

    /**
     * 分页查询项目列表
     */
    @Operation(summary = "分页查询项目列表")
    @GetMapping("/page")
    public Result<PageResult<ServiceProject>> page(PageRequest pageRequest, ServiceProject query) {
        Page<ServiceProject> page = serviceProjectService.pageProjects(pageRequest, query);
        return Result.success(PageResult.of(page));
    }

    /**
     * 查询所有上架项目
     */
    @Operation(summary = "查询所有上架项目")
    @GetMapping("/active")
    public Result<List<ServiceProject>> listActive() {
        List<ServiceProject> list = serviceProjectService.listActiveProjects();
        return Result.success(list);
    }

    /**
     * 根据ID查询项目
     */
    @Operation(summary = "根据ID查询项目")
    @GetMapping("/{id}")
    public Result<ServiceProject> getById(@PathVariable Long id) {
        ServiceProject project = serviceProjectService.getById(id);
        return Result.success(project);
    }

    /**
     * 新增项目
     */
    @Operation(summary = "新增项目")
    @HasPermission("project:add")
    @OperLog(title = "项目管理", businessType = 1, targetType = "project")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody ServiceProjectDTO dto) {
        serviceProjectService.addProject(dto);
        return Result.success();
    }

    /**
     * 修改项目
     */
    @Operation(summary = "修改项目")
    @HasPermission("project:edit")
    @OperLog(title = "项目管理", businessType = 2, targetType = "project")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody ServiceProjectDTO dto) {
        serviceProjectService.updateProject(dto);
        return Result.success();
    }

    /**
     * 删除项目
     */
    @Operation(summary = "删除项目")
    @HasPermission("project:delete")
    @OperLog(title = "项目管理", businessType = 3, targetType = "project")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        // 获取项目名称用于日志记录
        ServiceProject project = serviceProjectService.getById(id);
        String projectName = project != null ? project.getName() : null;
        serviceProjectService.deleteProject(id);
        OperLogContextHolder.setTargetName(projectName);
        return Result.success();
    }

    /**
     * 修改项目状态
     */
    @Operation(summary = "修改项目状态")
    @HasPermission("project:edit")
    @OperLog(title = "项目管理", businessType = 2, targetType = "project")
    @PutMapping("/status/{id}")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        serviceProjectService.updateStatus(id, status);
        return Result.success();
    }
}
