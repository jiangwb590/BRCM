package com.bcrm.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 互亿无线短信配置（验证码通知短信接口）
 */
@Data
@Component
@ConfigurationProperties(prefix = "sms.ihuyi")
public class SmsConfig {
    
    /**
     * API ID (account)
     */
    private String apiId;
    
    /**
     * API Key (password)
     */
    private String apiKey;
    
    /**
     * 短信签名
     */
    private String signName;
    
    /**
     * 是否启用短信功能
     */
    private Boolean enabled = false;
    
    /**
     * API地址（短信单条发送）
     */
    private String apiUrl = "http://106.ihuyi.com/webservice/sms.php?method=Submit";
    
    /**
     * 返回格式
     */
    private String format = "json";
}