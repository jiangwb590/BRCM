package com.bcrm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bcrm.common.PageRequest;
import com.bcrm.dto.RechargeDTO;
import com.bcrm.entity.RechargeRecord;

/**
 * 充值服务接口
 */
public interface RechargeRecordService extends IService<RechargeRecord> {

    /**
     * 分页查询充值记录
     */
    Page<RechargeRecord> pageRecharges(PageRequest pageRequest, Long customerId);

    /**
     * 充值
     */
    void recharge(RechargeDTO dto);
}
