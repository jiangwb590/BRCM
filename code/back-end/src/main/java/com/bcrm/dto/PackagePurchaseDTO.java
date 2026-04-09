package com.bcrm.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 购买套餐DTO
 */
@Data
public class PackagePurchaseDTO {

    @NotNull(message = "客户ID不能为空")
    private Long customerId;

    @NotNull(message = "套餐ID不能为空")
    private Long packageId;

    @Min(value = 1, message = "购买数量必须大于0")
    private Integer quantity = 1;

    private String payMethod;

    private Long cardId;

    private String remark;
}
