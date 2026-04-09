package com.bcrm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 服务项目DTO
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Data
public class ServiceProjectDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 项目ID（修改时必填）
     */
    private Long id;

    /**
     * 项目编号
     */
    private String projectCode;

    /**
     * 项目名称
     */
    @NotBlank(message = "项目名称不能为空")
    private String name;

    /**
     * 项目分类ID
     */
    private String categoryName;

    /**
     * 项目图片
     */
    private String image;

    /**
     * 项目描述
     */
    private String description;

    /**
     * 服务时长（分钟）
     */
    @NotNull(message = "服务时长不能为空")
    private Integer duration;

    /**
     * 项目价格
     */
    @NotNull(message = "项目价格不能为空")
    private BigDecimal price;

    /**
     * 会员价
     */
    private BigDecimal memberPrice;

    /**
     * 成本价
     */
    private BigDecimal costPrice;

    /**
     * 是否套餐：0-否 1-是
     */
    private Integer isPackage;

    /**
     * 是否次卡项目：0-否 1-是
     */
    private Integer isCardProject;

    /**
     * 状态：0-下架 1-上架
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sort;
}
