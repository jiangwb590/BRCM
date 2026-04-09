package com.bcrm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 充值记录实体
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Data
@TableName("recharge_record")
public class RechargeRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String rechargeNo;

    private Long customerId;

    private String customerName;

    private Long cardId;

    private BigDecimal rechargeAmount;

    private BigDecimal giftAmount;

    private BigDecimal totalAmount;

    private String payMethod;

    private BigDecimal balanceBefore;

    private BigDecimal balanceAfter;

    private Integer status;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
