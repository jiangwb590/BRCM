package com.bcrm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bcrm.common.PageRequest;
import com.bcrm.dto.ConsumeRecordDTO;
import com.bcrm.entity.ConsumeRecord;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 消费记录服务接口
 *
 * @author BCRM
 * @since 2026-03-14
 */
public interface ConsumeRecordService extends IService<ConsumeRecord> {

    /**
     * 分页查询消费记录
     *
     * @param pageRequest 分页参数
     * @param query 查询条件
     * @return 分页结果
     */
    Page<ConsumeRecord> pageRecords(PageRequest pageRequest, ConsumeRecord query);

    /**
     * 创建消费记录
     *
     * @param dto 消费信息
     */
    void createRecord(ConsumeRecordDTO dto);

    /**
     * 获取客户消费记录
     *
     * @param customerId 客户ID
     * @return 消费记录列表
     */
    List<ConsumeRecord> getByCustomerId(Long customerId);

    /**
     * 获取营收统计
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计结果
     */
    Map<String, Object> getRevenueStatistics(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 获取今日营收
     *
     * @return 今日营收金额
     */
    BigDecimal getTodayRevenue();
}
