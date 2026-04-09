package com.bcrm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 服务项目实体
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Data
@TableName("service_project")
public class ServiceProject implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 项目ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 项目编号
     */
    private String projectCode;

    /**
     * 项目名称
     */
    private String name;

    /**
     * 项目分类ID
     */
    private Long categoryId;

    /**
     * 分类名称（冗余）
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
    private Integer duration;

    /**
     * 项目价格
     */
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
