package com.bcrm.service;

import java.time.LocalDate;
import java.util.Map;

/**
 * 数据分析服务接口
 *
 * @author BCRM
 * @since 2026-03-14
 */
public interface DashboardService {

    /**
     * 获取概览统计
     *
     * @return 统计数据
     */
    Map<String, Object> getOverview();

    /**
     * 获取营收趋势
     *
     * @param days 天数
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 趋势数据
     */
    Map<String, Object> getRevenueTrend(Integer days, LocalDate startDate, LocalDate endDate);

    /**
     * 获取客户来源统计
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计数据
     */
    Map<String, Object> getCustomerSource(LocalDate startDate, LocalDate endDate);

    /**
     * 获取客户分类统计
     *
     * @return 统计数据
     */
    Map<String, Object> getCustomerCategory();

    /**
     * 获取员工业绩排名
     *
     * @param limit 排名数量
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 排名数据
     */
    Map<String, Object> getEmployeePerformance(Integer limit, LocalDate startDate, LocalDate endDate);

    /**
     * 获取项目销量排名
     *
     * @param limit 排名数量
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 排名数据
     */
    Map<String, Object> getProjectSales(Integer limit, LocalDate startDate, LocalDate endDate);

    /**
     * 获取产品/耗材消耗比例趋势
     * 计算逻辑：当天消耗数量 ÷ 当天到店人次
     *
     * @param days 天数
     * @return 趋势数据
     */
    Map<String, Object> getConsumptionRatioTrend(Integer days);

    /**
     * 获取客户来源趋势
     *
     * @param days 天数
     * @param channel 渠道（可选）
     * @return 趋势数据
     */
    Map<String, Object> getCustomerSourceTrend(Integer days, String channel);
}
