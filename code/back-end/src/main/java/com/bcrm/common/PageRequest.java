package com.bcrm.common;

import lombok.Getter;

import java.io.Serializable;

/**
 * 分页请求参数
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Getter
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

    // === Setter 方法，兼容前端多种参数名 ===

    public void setCurrent(Integer current) {
        if (current != null) {
            this.current = current;
        }
    }

    public void setSize(Integer size) {
        if (size != null) {
            this.size = size;
        }
    }

    /**
     * 兼容前端 pageNum 参数
     */
    public void setPageNum(Integer pageNum) {
        if (pageNum != null) {
            this.current = pageNum;
        }
    }

    /**
     * 兼容前端 pageSize 参数
     */
    public void setPageSize(Integer pageSize) {
        if (pageSize != null) {
            this.size = pageSize;
        }
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public void setIsAsc(Boolean isAsc) {
        this.isAsc = isAsc;
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
