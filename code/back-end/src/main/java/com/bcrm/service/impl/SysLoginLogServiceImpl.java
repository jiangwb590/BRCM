package com.bcrm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bcrm.entity.SysLoginLog;
import com.bcrm.mapper.SysLoginLogMapper;
import com.bcrm.service.SysLoginLogService;
import org.springframework.stereotype.Service;

/**
 * 登录日志服务实现
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Service
public class SysLoginLogServiceImpl extends ServiceImpl<SysLoginLogMapper, SysLoginLog> implements SysLoginLogService {

    @Override
    public void recordLog(SysLoginLog log) {
        save(log);
    }
}
