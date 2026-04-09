package com.bcrm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会员等级实体类
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Data
@TableName("member_level")
public class MemberLevel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 等级ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 等级名称
     */
    private String levelName;

    /**
     * 等级编码
     */
    private String levelCode;

    /**
     * 最低消费金额
     */
    private BigDecimal minAmount;

    /**
     * 最高消费金额
     */
    private BigDecimal maxAmount;

    /**
     * 折扣（如0.95表示95折）
     */
    private BigDecimal discount;

    /**
     * 升级所需积分
     */
    private Integer upgradePoints;

    /**
     * 等级图标
     */
    private String levelIcon;

    /**
     * 等级颜色
     */
    private String levelColor;

    /**
     * 会员权益
     */
    private String benefits;

    /**
     * 排序
     */
    private Integer sort;

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
