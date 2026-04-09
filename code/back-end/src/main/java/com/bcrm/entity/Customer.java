package com.bcrm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 客户信息实体
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Data
@TableName("customer")
public class Customer implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 客户ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 客户姓名
     */
    private String name;

    /**
     * 性别：0-未知 1-男 2-女
     */
    private Integer gender;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 微信号
     */
    private String wechat;

    /**
     * 生日（仅月日，格式：MM-dd，如"03-20"）
     */
    private String birthday;

    /**
     * 客户来源
     */
    private String source;

    /**
     * 客户分类：潜在客户/新客户/老客户/VIP客户/沉睡客户
     */
    private String category;

    /**
     * 会员等级ID
     */
    private Long memberLevelId;

    /**
     * 顾问ID（专属美容师）
     */
    private Long advisorId;

    /**
     * 累计消费金额
     */
    private java.math.BigDecimal totalAmount;

    /**
     * 消费次数
     */
    private Integer consumeCount;

    /**
     * 最近消费时间
     */
    private LocalDateTime lastConsumeTime;

    /**
     * 储值余额
     */
    private java.math.BigDecimal balance;

    /**
     * 积分
     */
    private Integer points;

    /**
     * 标签（多个标签用逗号分隔）
     */
    private String tags;

    /**
     * 备注
     */
    private String remark;

    /**
     * 状态：0-禁用 1-启用
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

    /**
     * 搜索关键字（非数据库字段，用于查询条件）
     */
    @TableField(exist = false)
    private String keyword;
}
