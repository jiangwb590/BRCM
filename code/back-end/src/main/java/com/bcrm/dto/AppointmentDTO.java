package com.bcrm.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 预约DTO
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Data
public class AppointmentDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 预约ID（修改时必填）
     */
    private Long id;

    /**
     * 客户ID
     */
    @NotNull(message = "客户ID不能为空")
    private Long customerId;

    /**
     * 项目ID
     */
    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    /**
     * 预约日期
     */
    @NotNull(message = "预约日期不能为空")
    private LocalDate appointmentDate;

    /**
     * 预约开始时间
     */
    @NotNull(message = "预约开始时间不能为空")
    private LocalTime startTime;

    /**
     * 预约结束时间
     */
    @NotNull(message = "预约结束时间不能为空")
    private LocalTime endTime;

    /**
     * 美容师ID
     */
    private Long beauticianId;

    /**
     * 预约来源
     */
    private String source;

    /**
     * 备注
     */
    private String remark;
}
