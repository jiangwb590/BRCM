package com.bcrm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bcrm.common.PageRequest;
import com.bcrm.dto.MemberCardDTO;
import com.bcrm.entity.MemberCard;

import java.util.List;

/**
 * 会员卡服务接口
 *
 * @author BCRM
 * @since 2026-03-14
 */
public interface MemberCardService extends IService<MemberCard> {

    /**
     * 分页查询会员卡
     *
     * @param pageRequest 分页参数
     * @param query 查询条件
     * @return 分页结果
     */
    Page<MemberCard> pageCards(PageRequest pageRequest, MemberCard query);

    /**
     * 查询客户会员卡
     *
     * @param customerId 客户ID
     * @return 会员卡列表
     */
    List<MemberCard> getByCustomerId(Long customerId);

    /**
     * 开卡
     *
     * @param dto 会员卡信息
     */
    void createCard(MemberCardDTO dto);

    /**
     * 充值
     *
     * @param cardId 会员卡ID
     * @param amount 充值金额
     */
    void recharge(Long cardId, java.math.BigDecimal amount);

    /**
     * 作废会员卡
     *
     * @param cardId 会员卡ID
     */
    void disableCard(Long cardId);
}
