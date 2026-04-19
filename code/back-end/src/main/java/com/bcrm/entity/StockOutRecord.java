package com.bcrm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 出库记录实体类
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Data
@TableName("stock_out_record")
public class StockOutRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 出库单号
     */
    private String stockOutNo;

    /**
     * 产品ID
     */
    private Long productId;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 产品编码
     */
    private String productCode;

    /**
     * 出库数量
     */
    private Integer quantity;

    /**
     * 单价
     */
    private BigDecimal unitPrice;

    /**
     * 总价
     */
    private BigDecimal totalPrice;

    /**
     * 出库类型：1-服务消耗 2-报废 3-其他 4-客户消费
     */
    private Integer stockOutType;

    /**
     * 客户ID（出库类型为客户消费时关联）
     */
    private Long customerId;

    /**
     * 客户名称（出库类型为客户消费时记录）
     */
    private String customerName;

    /**
     * 关联ID（如消费记录ID）
     */
    private Long relatedId;

    /**
     * 出库时间
     */
    private LocalDateTime stockOutTime;

    /**
     * 状态：0-作废 1-正常
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
    @TableField(fill = FieldFill.UPDATE)
    private Long updateBy;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    /**
     * 删除标记：0-未删除 1-已删除
     */
    @TableLogic
    private Integer deleted;

    /**
     * 备注
     */
    private String remark;
}
