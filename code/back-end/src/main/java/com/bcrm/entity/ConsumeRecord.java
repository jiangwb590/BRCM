package com.bcrm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 消费记录实体
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Data
@TableName("consume_record")
public class ConsumeRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 消费单号
     */
    private String consumeNo;

    /**
     * 客户ID
     */
    private Long customerId;

    /**
     * 客户姓名（冗余）
     */
    private String customerName;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 项目名称（冗余）
     */
    private String projectName;

    /**
     * 产品ID（购买产品时使用）
     */
    private Long productId;

    /**
     * 产品名称（冗余）
     */
    private String productName;

    /**
     * 购买数量（购买产品时使用）
     */
    private Integer quantity;

    /**
     * 消费类型：cash-现金消费/times-次卡消费/stored-储值消费/product-购买产品
     */
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
     * 折扣金额
     */
    private BigDecimal discountAmount;

    /**
     * 美容师ID
     */
    private Long beauticianId;

    /**
     * 美容师姓名（冗余）
     */
    private String beauticianName;

    /**
     * 次卡ID（次卡消费时使用）
     */
    private Long cardId;

    /**
     * 套餐购买记录ID（次卡消费时使用）
     */
    private Long purchaseId;

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

    /**
     * 状态：0-已退款 1-正常
     */
    private Integer status;

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 删除标记：0-未删除 1-已删除
     */
    @TableLogic
    private Integer deleted;
}
