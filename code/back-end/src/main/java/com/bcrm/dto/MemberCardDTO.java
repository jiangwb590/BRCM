package com.bcrm.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 会员卡DTO
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Data
public class MemberCardDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 客户ID
     */
    @NotNull(message = "客户ID不能为空")
    private Long customerId;

    /**
     * 卡类型：次卡/储值卡
     */
    @NotNull(message = "卡类型不能为空")
    private String cardType;

    /**
     * 关联项目ID（次卡）
     */
    private Long projectId;

    /**
     * 总次数（次卡）
     */
    private Integer totalCount;

    /**
     * 储值金额（储值卡）
     */
    private BigDecimal totalAmount;

    /**
     * 购卡金额
     */
    @NotNull(message = "购卡金额不能为空")
    private BigDecimal purchaseAmount;

    /**
     * 有效期开始
     */
    private LocalDate validStartDate;

    /**
     * 有效期结束
     */
    private LocalDate validEndDate;
}
