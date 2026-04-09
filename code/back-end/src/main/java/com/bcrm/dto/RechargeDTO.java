package com.bcrm.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 充值DTO
 */
@Data
public class RechargeDTO {

    @NotNull(message = "客户ID不能为空")
    private Long customerId;

    @NotNull(message = "充值金额不能为空")
    @DecimalMin(value = "0.01", message = "充值金额必须大于0")
    private BigDecimal rechargeAmount;

    private BigDecimal giftAmount;

    private String payMethod;

    private String remark;
}
