package com.bcrm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bcrm.common.PageRequest;
import com.bcrm.common.PageResult;
import com.bcrm.common.Result;
import com.bcrm.dto.AppointmentDTO;
import com.bcrm.entity.Appointment;
import com.bcrm.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 预约管理控制器
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Tag(name = "预约管理")
@RestController
@RequestMapping("/appointment")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    /**
     * 分页查询预约列表
     */
    @Operation(summary = "分页查询预约列表")
    @GetMapping("/page")
    public Result<PageResult<Appointment>> page(PageRequest pageRequest, Appointment query) {
        Page<Appointment> page = appointmentService.pageAppointments(pageRequest, query);
        return Result.success(PageResult.of(page));
    }

    /**
     * 查询指定日期的预约列表
     */
    @Operation(summary = "查询指定日期的预约列表")
    @GetMapping("/date/{date}")
    public Result<List<Appointment>> getByDate(@PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        List<Appointment> list = appointmentService.getByDate(date);
        return Result.success(list);
    }

    /**
     * 根据ID查询预约
     */
    @Operation(summary = "根据ID查询预约")
    @GetMapping("/{id}")
    public Result<Appointment> getById(@PathVariable Long id) {
        Appointment appointment = appointmentService.getById(id);
        return Result.success(appointment);
    }

    /**
     * 新增预约
     */
    @Operation(summary = "新增预约")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody AppointmentDTO dto) {
        appointmentService.addAppointment(dto);
        return Result.success();
    }

    /**
     * 修改预约
     */
    @Operation(summary = "修改预约")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody AppointmentDTO dto) {
        appointmentService.updateAppointment(dto);
        return Result.success();
    }

    /**
     * 取消预约
     */
    @Operation(summary = "取消预约")
    @PostMapping("/cancel/{id}")
    public Result<Void> cancel(@PathVariable Long id, @RequestParam(required = false) String reason) {
        appointmentService.cancelAppointment(id, reason);
        return Result.success();
    }

    /**
     * 确认预约
     */
    @Operation(summary = "确认预约")
    @PostMapping("/confirm/{id}")
    public Result<Void> confirm(@PathVariable Long id) {
        appointmentService.confirmAppointment(id);
        return Result.success();
    }

    /**
     * 开始服务
     */
    @Operation(summary = "开始服务")
    @PostMapping("/start/{id}")
    public Result<Void> startService(@PathVariable Long id) {
        appointmentService.startService(id);
        return Result.success();
    }

    /**
     * 完成服务
     */
    @Operation(summary = "完成服务")
    @PostMapping("/complete/{id}")
    public Result<Void> completeService(@PathVariable Long id) {
        appointmentService.completeService(id);
        return Result.success();
    }

    /**
     * 获取今日预约统计
     */
    @Operation(summary = "获取今日预约统计")
    @GetMapping("/statistics/today")
    public Result<?> getTodayStatistics() {
        return Result.success(appointmentService.getTodayStatistics());
    }
}
