package com.bcrm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bcrm.entity.SysOperLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志Mapper
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Mapper
public interface SysOperLogMapper extends BaseMapper<SysOperLog> {
}
