package com.bcrm.vo;

import cn.hutool.core.annotation.Alias;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 客户导出数据VO
 */
@Data
public class CustomerExportVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Alias("客户姓名")
    private String name;

    @Alias("性别")
    private String gender;

    @Alias("手机号")
    private String phone;

    @Alias("生日")
    private String birthday;

    @Alias("客户类型")
    private String category;

    @Alias("客户来源")
    private String source;

    @Alias("累计消费")
    private BigDecimal totalAmount;

    @Alias("消费次数")
    private Integer consumeCount;

    @Alias("最近消费时间")
    private LocalDateTime lastConsumeTime;

    @Alias("储值余额")
    private BigDecimal balance;

    @Alias("备注")
    private String remark;

    @Alias("创建时间")
    private LocalDateTime createTime;
}