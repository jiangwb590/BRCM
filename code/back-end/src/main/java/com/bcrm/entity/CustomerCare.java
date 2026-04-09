package com.bcrm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 客户关怀实体
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Data
@TableName("customer_care")
public class CustomerCare implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关怀ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 客户ID
     */
    private Long customerId;

    /**
     * 客户姓名（冗余）
     */
    private String customerName;

    /**
     * 关联消费记录ID
     */
    private Long consumeRecordId;

    /**
     * 关联项目ID
     */
    private Long projectId;

    /**
     * 项目/套餐名称
     */
    private String projectName;

    /**
     * 项目/套餐描述（用于短信内容）
     */
    private String projectDescription;

    /**
     * 客户手机号
     */
    private String customerPhone;

    /**
     * 关怀类型：生日关怀/消费关怀/沉睡唤醒/回访提醒
     */
    private String careType;

    /**
     * 关怀内容
     */
    private String content;

    /**
     * 计划日期
     */
    private LocalDate planDate;

    /**
     * 执行状态：待执行/已执行/已取消
     */
    private String status;

    /**
     * 执行时间
     */
    private LocalDateTime executeTime;

    /**
     * 执行人
     */
    private Long executeBy;

    /**
     * 执行人姓名
     */
    private String executeByName;

    /**
     * 备注
     */
    private String remark;

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
