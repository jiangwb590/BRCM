package com.bcrm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bcrm.common.PageRequest;
import com.bcrm.entity.StockInRecord;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 入库记录服务接口
 *
 * @author BCRM
 * @since 2026-03-14
 */
public interface StockInRecordService extends IService<StockInRecord> {

    /**
     * 分页查询入库记录
     */
    Page<StockInRecord> pageRecords(PageRequest pageRequest, StockInRecord query);

    /**
     * 新增入库记录
     */
    void addRecord(StockInRecord record);

    /**
     * 作废入库记录
     */
    void cancelRecord(Long id);

    /**
     * 获取入库统计
     */
    Map<String, Object> getStatistics();
}
