package com.bcrm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bcrm.common.PageRequest;
import com.bcrm.entity.Customer;
import com.bcrm.entity.CustomerCare;
import com.bcrm.exception.BusinessException;
import com.bcrm.mapper.CustomerCareMapper;
import com.bcrm.mapper.CustomerMapper;
import com.bcrm.service.CustomerCareService;
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

/**
 * 客户关怀服务实现
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerCareServiceImpl extends ServiceImpl<CustomerCareMapper, CustomerCare> implements CustomerCareService {

    private final SmsService smsService;
    private final CustomerMapper customerMapper;

    @Override
    public Page<CustomerCare> pageCares(PageRequest pageRequest, CustomerCare query) {
        Page<CustomerCare> page = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());
        
        LambdaQueryWrapper<CustomerCare> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getCustomerName()), CustomerCare::getCustomerName, query.getCustomerName());
        wrapper.eq(query.getCustomerId() != null, CustomerCare::getCustomerId, query.getCustomerId());
        wrapper.eq(StringUtils.hasText(query.getCareType()), CustomerCare::getCareType, query.getCareType());
        wrapper.eq(StringUtils.hasText(query.getStatus()), CustomerCare::getStatus, query.getStatus());
        wrapper.orderByAsc(CustomerCare::getPlanDate);
        wrapper.orderByDesc(CustomerCare::getCreateTime);
        
        return this.page(page, wrapper);
    }

    @Override
    public List<CustomerCare> getPendingCares(LocalDate date) {
        return this.list(new LambdaQueryWrapper<CustomerCare>()
                .eq(CustomerCare::getPlanDate, date)
                .eq(CustomerCare::getStatus, "待执行")
                .orderByAsc(CustomerCare::getPlanDate));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createCare(CustomerCare care) {
        care.setStatus("待执行");
        this.save(care);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executeCare(Long id, String remark, Long executeBy, String executeByName) {
        CustomerCare care = this.getById(id);
        if (care == null) {
            throw new BusinessException("关怀记录不存在");
        }
        
        if (!"待执行".equals(care.getStatus())) {
            throw new BusinessException("该关怀已执行或已取消");
        }
        
        // 发送短信
        boolean smsResult = false;
        try {
            Customer customer = customerMapper.selectById(care.getCustomerId());
            if (customer != null && StringUtils.hasText(customer.getPhone())) {
                smsResult = sendCareSms(care.getCareType(), customer.getPhone(), customer.getName(), care);
            }
        } catch (Exception e) {
            log.error("发送关怀短信失败", e);
        }
        
        // 更新关怀记录
        CustomerCare updateCare = new CustomerCare();
        updateCare.setId(id);
        updateCare.setStatus("已执行");
        updateCare.setExecuteTime(LocalDateTime.now());
        updateCare.setExecuteBy(executeBy);
        updateCare.setExecuteByName(executeByName);
        String finalRemark = remark;
        if (smsResult) {
            finalRemark = (remark != null ? remark : "") + " [已发送短信通知]";
        }
        updateCare.setRemark(finalRemark);
        this.updateById(updateCare);
    }
    
    /**
     * 根据关怀类型发送对应短信
     */
    private boolean sendCareSms(String careType, String phone, String customerName, CustomerCare care) {
        switch (careType) {
            case "生日祝福":
                return smsService.sendBirthdaySms(phone, customerName);
            case "预约提醒":
                // 假设关怀内容包含预约信息
                return smsService.sendAppointmentSms(phone, customerName, care.getContent(), "服务项目");
            case "消费关怀":
                return smsService.sendConsumeSms(phone, customerName, "0", "0");
            case "沉睡唤醒":
                return smsService.sendWakeupSms(phone, customerName);
            default:
                log.warn("未知的关怀类型: {}", careType);
                return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelCare(Long id) {
        CustomerCare care = this.getById(id);
        if (care == null) {
            throw new BusinessException("关怀记录不存在");
        }
        
        CustomerCare updateCare = new CustomerCare();
        updateCare.setId(id);
        updateCare.setStatus("已取消");
        this.updateById(updateCare);
    }

    @Override
    public List<CustomerCare> getTodayTasks() {
        return this.list(new LambdaQueryWrapper<CustomerCare>()
                .eq(CustomerCare::getPlanDate, LocalDate.now())
                .eq(CustomerCare::getStatus, "待执行")
                .orderByAsc(CustomerCare::getPlanDate));
    }
}
