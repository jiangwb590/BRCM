package com.bcrm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bcrm.entity.StockInRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 入库记录Mapper
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Mapper
public interface StockInRecordMapper extends BaseMapper<StockInRecord> {
}
