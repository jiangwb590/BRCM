package com.bcrm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 套餐购买记录实体
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Data
@TableName("package_purchase")
public class PackagePurchase implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String purchaseNo;

    private Long customerId;

    private String customerName;

    private Long packageId;

    private String packageName;

    private Integer times;

    /**
     * 剩余次数
     */
    private Integer remainTimes;

    private BigDecimal price;

    private BigDecimal totalAmount;

    private String payMethod;

    private Long cardId;

    private BigDecimal balanceBefore;

    private BigDecimal balanceAfter;

    private Long memberCardId;

    private LocalDate validStartDate;

    private LocalDate validEndDate;

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
