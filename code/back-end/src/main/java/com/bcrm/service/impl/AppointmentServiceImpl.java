package com.bcrm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bcrm.common.PageRequest;
import com.bcrm.dto.AppointmentDTO;
import com.bcrm.entity.Appointment;
import com.bcrm.entity.Customer;
import com.bcrm.entity.ServiceProject;
import com.bcrm.entity.SysUser;
import com.bcrm.exception.BusinessException;
import com.bcrm.mapper.AppointmentMapper;
import com.bcrm.service.AppointmentService;
import com.bcrm.service.CustomerService;
import com.bcrm.service.SysDictService;
import com.bcrm.service.SysUserService;
import com.bcrm.service.ServiceProjectService;
import com.bcrm.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

/**
 * 预约服务实现
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl extends ServiceImpl<AppointmentMapper, Appointment> implements AppointmentService {

    private final CustomerService customerService;
    private final ServiceProjectService serviceProjectService;
    private final SysUserService sysUserService;
    private final SysDictService sysDictService;
    private final SmsService smsService;

    @Override
    public Page<Appointment> pageAppointments(PageRequest pageRequest, Appointment query) {
        Page<Appointment> page = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());
        
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getCustomerName()), Appointment::getCustomerName, query.getCustomerName());
        wrapper.like(StringUtils.hasText(query.getCustomerPhone()), Appointment::getCustomerPhone, query.getCustomerPhone());
        wrapper.eq(query.getAppointmentDate() != null, Appointment::getAppointmentDate, query.getAppointmentDate());
        wrapper.eq(StringUtils.hasText(query.getStatus()), Appointment::getStatus, query.getStatus());
        wrapper.eq(query.getBeauticianId() != null, Appointment::getBeauticianId, query.getBeauticianId());
        wrapper.orderByDesc(Appointment::getCreateTime);
        
        return this.page(page, wrapper);
    }

    @Override
    public List<Appointment> getByDate(LocalDate date) {
        return this.list(new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getAppointmentDate, date)
                .ne(Appointment::getStatus, "已取消")
                .orderByAsc(Appointment::getStartTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addAppointment(AppointmentDTO dto) {
        // 检查当天预约数量是否已达上限
        checkDailyAppointmentLimit(dto.getAppointmentDate());
        
        // 查询客户信息
        Customer customer = customerService.getById(dto.getCustomerId());
        if (customer == null) {
            throw new BusinessException("客户不存在");
        }
        
        // 查询项目信息
        ServiceProject project = serviceProjectService.getById(dto.getProjectId());
        if (project == null) {
            throw new BusinessException("项目不存在");
        }
        
        Appointment appointment = new Appointment();
        appointment.setAppointmentNo(generateAppointmentNo());
        appointment.setCustomerId(dto.getCustomerId());
        appointment.setCustomerName(customer.getName());
        appointment.setCustomerPhone(customer.getPhone());
        appointment.setProjectId(dto.getProjectId());
        appointment.setProjectName(project.getName());
        appointment.setAppointmentDate(dto.getAppointmentDate());
        appointment.setStartTime(dto.getStartTime());
        appointment.setEndTime(dto.getEndTime());
        appointment.setSource(dto.getSource());
        appointment.setRemark(dto.getRemark());
        appointment.setStatus("待确认");
        
        // 设置美容师
        if (dto.getBeauticianId() != null) {
            SysUser beautician = sysUserService.getById(dto.getBeauticianId());
            if (beautician != null) {
                appointment.setBeauticianId(dto.getBeauticianId());
                appointment.setBeauticianName(beautician.getRealName());
            }
        }
        
        this.save(appointment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAppointment(AppointmentDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("预约ID不能为空");
        }
        
        Appointment existAppointment = this.getById(dto.getId());
        if (existAppointment == null) {
            throw new BusinessException("预约不存在");
        }
        
        if ("已完成".equals(existAppointment.getStatus()) || "已取消".equals(existAppointment.getStatus())) {
            throw new BusinessException("该预约已完成或已取消，无法修改");
        }
        
        Appointment appointment = new Appointment();
        appointment.setId(dto.getId());
        appointment.setAppointmentDate(dto.getAppointmentDate());
        appointment.setStartTime(dto.getStartTime());
        appointment.setEndTime(dto.getEndTime());
        appointment.setRemark(dto.getRemark());
        
        if (dto.getBeauticianId() != null) {
            SysUser beautician = sysUserService.getById(dto.getBeauticianId());
            if (beautician != null) {
                appointment.setBeauticianId(dto.getBeauticianId());
                appointment.setBeauticianName(beautician.getRealName());
            }
        }
        
        this.updateById(appointment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelAppointment(Long id, String reason) {
        Appointment appointment = this.getById(id);
        if (appointment == null) {
            throw new BusinessException("预约不存在");
        }
        
        if ("已完成".equals(appointment.getStatus())) {
            throw new BusinessException("该预约已完成，无法取消");
        }
        
        Appointment updateAppointment = new Appointment();
        updateAppointment.setId(id);
        updateAppointment.setStatus("已取消");
        updateAppointment.setCancelReason(reason);
        updateAppointment.setCancelTime(LocalDateTime.now());
        
        this.updateById(updateAppointment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmAppointment(Long id) {
        Appointment appointment = this.getById(id);
        if (appointment == null) {
            throw new BusinessException("预约不存在");
        }
        
        Appointment updateAppointment = new Appointment();
        updateAppointment.setId(id);
        updateAppointment.setStatus("已确认");
        
        this.updateById(updateAppointment);
        
        // 发送预约成功提醒短信
        if (appointment.getCustomerPhone() != null && !appointment.getCustomerPhone().isEmpty()) {
            try {
                // 格式化预约时间
                String appointmentTime = String.format("%s %s", 
                        appointment.getAppointmentDate().format(DateTimeFormatter.ofPattern("MM月dd日")),
                        appointment.getStartTime());
                
                boolean smsSuccess = smsService.sendAppointmentSms(
                        appointment.getCustomerPhone(),
                        appointment.getCustomerName(),
                        appointmentTime,
                        appointment.getProjectName()
                );
                
                if (smsSuccess) {
                    log.info("预约确认短信发送成功，客户：{}，手机号：{}", 
                            appointment.getCustomerName(), appointment.getCustomerPhone());
                } else {
                    log.warn("预约确认短信发送失败，客户：{}，手机号：{}", 
                            appointment.getCustomerName(), appointment.getCustomerPhone());
                }
            } catch (Exception e) {
                log.error("发送预约确认短信异常，客户：{}，错误：{}", 
                        appointment.getCustomerName(), e.getMessage());
            }
        } else {
            log.warn("客户 {} 手机号为空，跳过发送预约确认短信", appointment.getCustomerName());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startService(Long id) {
        Appointment appointment = this.getById(id);
        if (appointment == null) {
            throw new BusinessException("预约不存在");
        }
        
        Appointment updateAppointment = new Appointment();
        updateAppointment.setId(id);
        updateAppointment.setStatus("服务中");
        
        this.updateById(updateAppointment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeService(Long id) {
        Appointment appointment = this.getById(id);
        if (appointment == null) {
            throw new BusinessException("预约不存在");
        }
        
        Appointment updateAppointment = new Appointment();
        updateAppointment.setId(id);
        updateAppointment.setStatus("已完成");
        
        this.updateById(updateAppointment);
    }

    @Override
    public Object getTodayStatistics() {
        LocalDate today = LocalDate.now();
        long total = this.count(new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getAppointmentDate, today));
        long pending = this.count(new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getAppointmentDate, today)
                .eq(Appointment::getStatus, "待确认"));
        long confirmed = this.count(new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getAppointmentDate, today)
                .eq(Appointment::getStatus, "已确认"));
        long completed = this.count(new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getAppointmentDate, today)
                .eq(Appointment::getStatus, "已完成"));
        
        return new Object() {
            public final long totalCount = total;
            public final long pendingCount = pending;
            public final long confirmedCount = confirmed;
            public final long completedCount = completed;
        };
    }

    /**
     * 生成预约编号
     */
    private String generateAppointmentNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomStr = String.format("%04d", new Random().nextInt(10000));
        return "AP" + dateStr + randomStr;
    }

    /**
     * 检查当天预约数量是否已达上限
     */
    private void checkDailyAppointmentLimit(LocalDate appointmentDate) {
        // 从字典中获取每日预约上限阈值
        String limitValue = sysDictService.getDictValue("daily_appointment_limit");
        int limit = 50; // 默认值
        if (StringUtils.hasText(limitValue)) {
            try {
                limit = Integer.parseInt(limitValue);
            } catch (NumberFormatException e) {
                log.warn("每日预约上限配置值无效: {}", limitValue);
            }
        }

        // 统计当天已有预约数量（不含已取消的）
        long currentCount = this.count(new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getAppointmentDate, appointmentDate)
                .ne(Appointment::getStatus, "已取消"));

        if (currentCount >= limit) {
            throw new BusinessException("当天预约客户数已满，请选择其他的时间");
        }
    }
}
