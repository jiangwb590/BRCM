package com.bcrm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 预约信息实体
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Data
@TableName("appointment")
public class Appointment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 预约ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 预约编号
     */
    private String appointmentNo;

    /**
     * 客户ID
     */
    private Long customerId;

    /**
     * 客户姓名（冗余）
     */
    private String customerName;

    /**
     * 客户手机号（冗余）
     */
    private String customerPhone;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 项目名称（冗余）
     */
    private String projectName;

    /**
     * 预约日期
     */
    private LocalDate appointmentDate;

    /**
     * 预约开始时间
     */
    private LocalTime startTime;

    /**
     * 预约结束时间
     */
    private LocalTime endTime;

    /**
     * 美容师ID
     */
    private Long beauticianId;

    /**
     * 美容师姓名（冗余）
     */
    private String beauticianName;

    /**
     * 预约状态：待确认/已确认/服务中/已完成/已取消/爽约
     */
    private String status;

    /**
     * 预约来源：前台预约/电话预约/微信预约
     */
    private String source;

    /**
     * 备注
     */
    private String remark;

    /**
     * 取消原因
     */
    private String cancelReason;

    /**
     * 取消时间
     */
    private LocalDateTime cancelTime;

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
