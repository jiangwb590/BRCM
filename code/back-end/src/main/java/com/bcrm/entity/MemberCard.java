package com.bcrm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 会员卡实体
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Data
@TableName("member_card")
public class MemberCard implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 卡ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 卡号
     */
    private String cardNo;

    /**
     * 客户ID
     */
    private Long customerId;

    /**
     * 客户姓名（冗余）
     */
    private String customerName;

    /**
     * 卡类型：次卡/储值卡
     */
    private String cardType;

    /**
     * 关联项目ID（次卡）
     */
    private Long projectId;

    /**
     * 项目名称（冗余）
     */
    private String projectName;

    /**
     * 总次数（次卡）
     */
    private Integer totalCount;

    /**
     * 剩余次数（次卡）
     */
    private Integer remainCount;

    /**
     * 储值金额（储值卡）
     */
    private BigDecimal totalAmount;

    /**
     * 剩余金额（储值卡）
     */
    private BigDecimal remainAmount;

    /**
     * 购卡金额
     */
    private BigDecimal purchaseAmount;

    /**
     * 有效期开始
     */
    private LocalDate validStartDate;

    /**
     * 有效期结束
     */
    private LocalDate validEndDate;

    /**
     * 状态：0-禁用 1-启用 2-已过期
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
