package com.bcrm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bcrm.common.PageRequest;
import com.bcrm.entity.SysOperLog;
import com.bcrm.mapper.SysOperLogMapper;
import com.bcrm.service.SysOperLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 操作日志服务实现
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Service
@RequiredArgsConstructor
public class SysOperLogServiceImpl extends ServiceImpl<SysOperLogMapper, SysOperLog> implements SysOperLogService {

    @Override
    public Page<SysOperLog> pageLogs(PageRequest pageRequest, SysOperLog query, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<SysOperLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getTitle())) {
            wrapper.like(SysOperLog::getTitle, query.getTitle());
        }
        if (StringUtils.hasText(query.getOperName())) {
            wrapper.like(SysOperLog::getOperName, query.getOperName());
        }
        if (query.getBusinessType() != null) {
            wrapper.eq(SysOperLog::getBusinessType, query.getBusinessType());
        }
        if (query.getStatus() != null) {
            wrapper.eq(SysOperLog::getStatus, query.getStatus());
        }
        if (startTime != null) {
            wrapper.ge(SysOperLog::getOperTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(SysOperLog::getOperTime, endTime);
        }
        wrapper.orderByDesc(SysOperLog::getOperTime);
        return page(pageRequest.toPage(), wrapper);
    }

    @Override
    public void recordLog(SysOperLog log) {
        save(log);
    }
}
