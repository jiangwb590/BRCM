package com.bcrm.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 会员等级DTO
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Data
public class MemberLevelDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 等级ID
     */
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
     * 折扣
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
     * 备注
     */
    private String remark;
}
