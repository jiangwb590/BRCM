package com.bcrm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bcrm.common.PageRequest;
import com.bcrm.entity.SysOperLog;

import java.time.LocalDateTime;

/**
 * 操作日志服务接口
 *
 * @author BCRM
 * @since 2026-03-14
 */
public interface SysOperLogService extends IService<SysOperLog> {

    /**
     * 分页查询操作日志
     */
    Page<SysOperLog> pageLogs(PageRequest pageRequest, SysOperLog query, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 记录操作日志
     */
    void recordLog(SysOperLog log);
}
