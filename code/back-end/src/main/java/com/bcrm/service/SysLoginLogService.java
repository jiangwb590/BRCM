package com.bcrm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bcrm.entity.SysLoginLog;

/**
 * 登录日志服务接口
 *
 * @author BCRM
 * @since 2026-03-14
 */
public interface SysLoginLogService extends IService<SysLoginLog> {

    /**
     * 记录登录日志
     */
    void recordLog(SysLoginLog log);
}
