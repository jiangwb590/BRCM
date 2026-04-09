package com.bcrm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bcrm.entity.StockOutRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 出库记录Mapper
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Mapper
public interface StockOutRecordMapper extends BaseMapper<StockOutRecord> {
}
