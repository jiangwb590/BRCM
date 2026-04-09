package com.bcrm.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 套餐DTO
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Data
public class ServicePackageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 套餐ID
     */
    private Long id;

    /**
     * 套餐名称
     */
    private String packageName;

    /**
     * 套餐编码
     */
    private String packageCode;

    /**
     * 套餐价格
     */
    private BigDecimal price;

    /**
     * 原价
     */
    private BigDecimal originalPrice;

    /**
     * 卡扣价（储值卡支付价格）
     */
    private BigDecimal cardPrice;

    /**
     * 包含次数
     */
    private Integer times;

    /**
     * 描述
     */
    private String description;

    /**
     * 图片
     */
    private String image;

    /**
     * 有效天数
     */
    private Integer validDays;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态：0-下架 1-上架
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 套餐项目列表
     */
    private List<PackageItemDTO> items;

    @Data
    public static class PackageItemDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        /**
         * 项目ID
         */
        private Long serviceId;
        /**
         * 项目名称
         */
        private String serviceName;
        /**
         * 次数
         */
        private Integer times;
    }
}
