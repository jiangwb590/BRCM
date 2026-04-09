package com.bcrm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bcrm.common.PageRequest;
import com.bcrm.entity.StockOutRecord;

import java.util.Map;

/**
 * 出库记录服务接口
 *
 * @author BCRM
 * @since 2026-03-14
 */
public interface StockOutRecordService extends IService<StockOutRecord> {

    /**
     * 分页查询出库记录
     */
    Page<StockOutRecord> pageRecords(PageRequest pageRequest, StockOutRecord query);

    /**
     * 新增出库记录
     */
    void addRecord(StockOutRecord record);

    /**
     * 作废出库记录
     */
    void cancelRecord(Long id);

    /**
     * 获取出库统计
     */
    Map<String, Object> getStatistics();
}
