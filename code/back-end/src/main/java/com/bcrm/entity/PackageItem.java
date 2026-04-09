package com.bcrm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 套餐项目关联实体类
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Data
@TableName("package_item")
public class PackageItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 套餐ID
     */
    private Long packageId;

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

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
