package com.bcrm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bcrm.common.PageRequest;
import com.bcrm.common.PageResult;
import com.bcrm.common.Result;
import com.bcrm.entity.CustomerCare;
import com.bcrm.security.LoginUser;
import com.bcrm.service.CustomerCareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 客户关怀控制器
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Tag(name = "客户关怀管理")
@RestController
@RequestMapping("/customer-care")
@RequiredArgsConstructor
public class CustomerCareController {

    private final CustomerCareService customerCareService;

    /**
     * 分页查询关怀记录
     */
    @Operation(summary = "分页查询关怀记录")
    @GetMapping("/page")
    public Result<PageResult<CustomerCare>> page(PageRequest pageRequest, CustomerCare query) {
        Page<CustomerCare> page = customerCareService.pageCares(pageRequest, query);
        return Result.success(PageResult.of(page));
    }

    /**
     * 获取待执行关怀
     */
    @Operation(summary = "获取待执行关怀")
    @GetMapping("/pending/{date}")
    public Result<List<CustomerCare>> getPendingCares(@PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        List<CustomerCare> list = customerCareService.getPendingCares(date);
        return Result.success(list);
    }

    /**
     * 获取今日待办
     */
    @Operation(summary = "获取今日待办")
    @GetMapping("/today")
    public Result<List<CustomerCare>> getTodayTasks() {
        List<CustomerCare> list = customerCareService.getTodayTasks();
        return Result.success(list);
    }

    /**
     * 根据ID查询关怀
     */
    @Operation(summary = "根据ID查询关怀")
    @GetMapping("/{id}")
    public Result<CustomerCare> getById(@PathVariable Long id) {
        CustomerCare care = customerCareService.getById(id);
        return Result.success(care);
    }

    /**
     * 创建关怀计划
     */
    @Operation(summary = "创建关怀计划")
    @PostMapping
    public Result<Void> create(@RequestBody CustomerCare care) {
        customerCareService.createCare(care);
        return Result.success();
    }

    /**
     * 执行关怀
     */
    @Operation(summary = "执行关怀")
    @PostMapping("/execute/{id}")
    public Result<Void> execute(@PathVariable Long id, @RequestParam(required = false) String remark,
                                @AuthenticationPrincipal LoginUser loginUser) {
        customerCareService.executeCare(id, remark, 
            loginUser.getUser().getId(), 
            loginUser.getUser().getRealName());
        return Result.success();
    }

    /**
     * 取消关怀
     */
    @Operation(summary = "取消关怀")
    @PostMapping("/cancel/{id}")
    public Result<Void> cancel(@PathVariable Long id) {
        customerCareService.cancelCare(id);
        return Result.success();
    }
}
