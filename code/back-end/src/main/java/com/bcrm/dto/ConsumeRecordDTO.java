package com.bcrm.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 消费记录DTO
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Data
public class ConsumeRecordDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 客户ID
     */
    @NotNull(message = "客户ID不能为空")
    private Long customerId;

    /**
     * 消费模式：project-单独项目 package-已购买套餐
     */
    @NotNull(message = "消费模式不能为空")
    private String consumeMode;

    /**
     * 项目ID（单独项目模式）
     */
    private Long projectId;

    /**
     * 套餐购买记录ID（已购买套餐模式）
     */
    private Long purchaseId;

    /**
     * 产品ID（购买产品模式）
     */
    private Long productId;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 购买数量（购买产品模式）
     */
    private Integer quantity;

    /**
     * 消费类型：cash-现金消费/times-次卡消费/stored-储值消费/product-购买产品
     */
    @NotNull(message = "消费类型不能为空")
    private String consumeType;

    /**
     * 消费金额
     */
    private BigDecimal amount;

    /**
     * 消费次数（次卡消费时使用）
     */
    private Integer consumeTimes;

    /**
     * 实际支付金额
     */
    private BigDecimal payAmount;

    /**
     * 美容师ID
     */
    private Long beauticianId;

    /**
     * 次卡ID（次卡消费时使用）
     */
    private Long cardId;

    /**
     * 关联预约ID
     */
    private Long appointmentId;

    /**
     * 支付方式：现金/微信/支付宝/银行卡
     */
    private String payMethod;

    /**
     * 产品消耗记录（JSON格式）
     * 格式：[{"productId":1,"productName":"眼霜","quantity":2,"type":"product"},{"productId":2,"productName":"手套","quantity":1,"type":"consumable"}]
     */
    private String productConsumptions;

    /**
     * 备注
     */
    private String remark;

    /**
     * 消费时间
     */
    private LocalDateTime consumeTime;
}
