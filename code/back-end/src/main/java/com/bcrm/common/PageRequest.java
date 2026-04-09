package com.bcrm.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 分页请求参数
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Data
public class PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码，默认第1页
     */
    private Integer current = 1;

    /**
     * 每页条数，默认10条
     */
    private Integer size = 10;

    /**
     * 排序字段
     */
    private String orderBy;

    /**
     * 是否升序，默认降序
     */
    private Boolean isAsc = false;

    /**
     * 获取当前页码（兼容方法）
     */
    public Integer getPageNum() {
        return this.current;
    }

    /**
     * 获取每页条数（兼容方法）
     */
    public Integer getPageSize() {
        return this.size;
    }

    /**
     * 获取MyBatis Plus的Page对象
     */
    public <T> com.baomidou.mybatisplus.extension.plugins.pagination.Page<T> toPage() {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<T> page = 
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(current, size);
        if (orderBy != null && !orderBy.isEmpty()) {
            if (isAsc) {
                page.addOrder(com.baomidou.mybatisplus.core.metadata.OrderItem.asc(orderBy));
            } else {
                page.addOrder(com.baomidou.mybatisplus.core.metadata.OrderItem.desc(orderBy));
            }
        }
        return page;
    }
}
