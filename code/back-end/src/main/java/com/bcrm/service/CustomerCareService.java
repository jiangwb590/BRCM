package com.bcrm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bcrm.common.PageRequest;
import com.bcrm.entity.CustomerCare;

import java.time.LocalDate;
import java.util.List;

/**
 * 客户关怀服务接口
 *
 * @author BCRM
 * @since 2026-03-14
 */
public interface CustomerCareService extends IService<CustomerCare> {

    /**
     * 分页查询关怀记录
     *
     * @param pageRequest 分页参数
     * @param query 查询条件
     * @return 分页结果
     */
    Page<CustomerCare> pageCares(PageRequest pageRequest, CustomerCare query);

    /**
     * 获取待执行关怀
     *
     * @param date 日期
     * @return 关怀列表
     */
    List<CustomerCare> getPendingCares(LocalDate date);

    /**
     * 创建关怀计划
     *
     * @param care 关怀信息
     */
    void createCare(CustomerCare care);

    /**
     * 执行关怀
     *
     * @param id 关怀ID
     * @param remark 备注
     */
    void executeCare(Long id, String remark, Long executeBy, String executeByName);

    /**
     * 取消关怀
     *
     * @param id 关怀ID
     */
    void cancelCare(Long id);

    /**
     * 获取今日待办
     *
     * @return 待办列表
     */
    List<CustomerCare> getTodayTasks();
}
