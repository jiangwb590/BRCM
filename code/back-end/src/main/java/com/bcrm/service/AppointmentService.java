package com.bcrm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bcrm.common.PageRequest;
import com.bcrm.dto.AppointmentDTO;
import com.bcrm.entity.Appointment;

import java.time.LocalDate;
import java.util.List;

/**
 * 预约服务接口
 *
 * @author BCRM
 * @since 2026-03-14
 */
public interface AppointmentService extends IService<Appointment> {

    /**
     * 分页查询预约
     *
     * @param pageRequest 分页参数
     * @param query 查询条件
     * @return 分页结果
     */
    Page<Appointment> pageAppointments(PageRequest pageRequest, Appointment query);

    /**
     * 查询指定日期的预约列表
     *
     * @param date 日期
     * @return 预约列表
     */
    List<Appointment> getByDate(LocalDate date);

    /**
     * 新增预约
     *
     * @param dto 预约信息
     */
    void addAppointment(AppointmentDTO dto);

    /**
     * 修改预约
     *
     * @param dto 预约信息
     */
    void updateAppointment(AppointmentDTO dto);

    /**
     * 取消预约
     *
     * @param id 预约ID
     * @param reason 取消原因
     */
    void cancelAppointment(Long id, String reason);

    /**
     * 确认预约
     *
     * @param id 预约ID
     */
    void confirmAppointment(Long id);

    /**
     * 开始服务
     *
     * @param id 预约ID
     */
    void startService(Long id);

    /**
     * 完成服务
     *
     * @param id 预约ID
     */
    void completeService(Long id);

    /**
     * 获取今日预约统计
     *
     * @return 统计结果
     */
    Object getTodayStatistics();
}
