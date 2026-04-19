package com.bcrm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 客户DTO
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Data
public class CustomerDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 客户ID（修改时必填）
     */
    private Long id;

    /**
     * 客户姓名
     */
    @NotBlank(message = "客户姓名不能为空")
    private String name;

    /**
     * 性别：0-未知 1-男 2-女
     */
    private Integer gender;

    /**
     * 手机号
     */
//    @NotBlank(message = "手机号不能为空")
    private String phone;

    /**
     * 微信号
     */
    private String wechat;

    /**
     * 生日（仅月日，格式：MM-dd）
     */
    private String birthday;

    /**
     * 客户来源
     */
    private String source;

    /**
     * 会员等级ID
     */
    private Long memberLevelId;

    /**
     * 顾问ID（专属美容师）
     */
    private Long advisorId;

    /**
     * 标签（多个标签用逗号分隔）
     */
    private String tags;

    /**
     * 备注
     */
    private String remark;
}
