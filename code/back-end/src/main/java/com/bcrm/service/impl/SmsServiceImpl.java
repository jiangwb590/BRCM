package com.bcrm.service.impl;

import com.bcrm.config.SmsConfig;
import com.bcrm.service.SmsService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * 互亿无线短信服务实现（验证码通知短信接口）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsServiceImpl implements SmsService {
    
    private final SmsConfig smsConfig;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 发送短信（单条）
     */
    @Override
    public boolean sendSms(String phoneNumber, String content) {
        if (!smsConfig.getEnabled()) {
            log.warn("短信功能未启用，跳过发送。手机号: {}", phoneNumber);
            return false;
        }
        
        try {
            // 构建短信内容（添加签名）
            String fullContent = String.format("【%s】%s", smsConfig.getSignName(), content);
            
            // 构建表单参数
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("account", smsConfig.getApiId());
            params.add("password", smsConfig.getApiKey());
            params.add("mobile", phoneNumber);
            params.add("content", content);
            params.add("format", smsConfig.getFormat());
            
            // 发送请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
            
            log.info("发送短信请求: phone={}, content={}", phoneNumber, fullContent);
            
            ResponseEntity<String> response = restTemplate.exchange(
                    smsConfig.getApiUrl(),
                    HttpMethod.POST,
                    entity,
                    String.class);
            
            // 解析响应
            String responseBody = response.getBody();
            log.info("短信响应: {}", responseBody);
            
            // 解析JSON响应
            JsonNode result = objectMapper.readTree(responseBody);
            int code = result.get("code").asInt();
            if (code == 2) {
                log.info("短信发送成功, smsid: {}", result.get("smsid").asText());
                return true;
            } else {
                log.error("短信发送失败: code={}, msg={}", code, result.get("msg").asText());
                return false;
            }
        } catch (Exception e) {
            log.error("发送短信异常", e);
            return false;
        }
    }
    
    /**
     * 批量发送短信（逐条发送）
     */
    @Override
    public int sendBatchSms(List<String> phoneNumbers, String content) {
        if (!smsConfig.getEnabled()) {
            log.warn("短信功能未启用，跳过批量发送。手机号数量: {}", phoneNumbers.size());
            return 0;
        }
        
        // 单条发送接口，逐条发送
        int successCount = 0;
        for (String phone : phoneNumbers) {
            if (sendSms(phone, content)) {
                successCount++;
            }
        }
        return successCount;
    }
    
    /**
     * 发送生日祝福短信
     */
    @Override
    public boolean sendBirthdaySms(String phoneNumber, String customerName) {
        String content = String.format("亲爱的%s，今天是您的生日，祝您生日快乐，愿您美丽永驻，幸福常伴！回T退订", customerName);
        return sendSms(phoneNumber, content);
    }
    
    /**
     * 发送预约提醒短信
     */
    @Override
    public boolean sendAppointmentSms(String phoneNumber, String customerName, String appointmentTime, String serviceName) {
        String content = String.format("尊敬的%s，您已成功预约%s的护肤美容项目，服务时间是%s，请准时到店体验，期待为您带来美丽改变！",
                customerName, serviceName,appointmentTime);
        return sendSms(phoneNumber, content);
    }
    
    /**
     * 发送消费关怀短信
     */
    @Override
    public boolean sendConsumeSms(String phoneNumber, String customerName, String amount, String balance) {
        String content = String.format("尊敬的%s，您本次消费%s元，账户余额%s元，感谢您的支持！回T退订", 
                customerName, amount, balance);
        return sendSms(phoneNumber, content);
    }
    
    /**
     * 发送沉睡唤醒短信
     */
    @Override
    public boolean sendWakeupSms(String phoneNumber, String customerName) {
        String content = String.format("亲爱的%s，好久不见，我们想您了！回店消费享专属优惠，期待您的光临！回T退订", customerName);
        return sendSms(phoneNumber, content);
    }
    
    /**
     * 发送消费回访短信
     */
    @Override
    public boolean sendConsumeCareSms(String phoneNumber, String customerName, String projectName, String projectDescription) {
        String content;
        if (projectDescription != null && !projectDescription.isEmpty()) {
            content = String.format("尊敬的%s，感谢您体验%s，%s，期待再次为您服务！回T退订", 
                    customerName, projectName, projectDescription);
        } else {
            content = String.format("尊敬的%s，感谢您体验%s，如有任何问题欢迎随时联系我们，期待再次为您服务！回T退订", 
                    customerName, projectName);
        }
        return sendSms(phoneNumber, content);
    }
    
    /**
     * 发送新客户回访短信（登记满7天）
     */
    @Override
    public boolean sendNewCustomerFollowUpSms(String phoneNumber, String customerName, Integer gender) {
        String title = "亲爱的" + customerName;
        if (gender != null) {
            title += (gender == 2 ? "女士" : "先生");
        }
        String content = String.format("%s，感谢您成为我们的新朋友！希望我们的服务让您感到满意。如有任何问题或建议，欢迎随时联系我们，期待为您带来更多美丽体验！",
                title);
        return sendSms(phoneNumber, content);
    }
    
    /**
     * 发送会员消费满3次提醒短信
     */
    @Override
    public boolean sendMemberConsumeRewardSms(String phoneNumber, String customerName) {
        String content = String.format("尊贵的%s会员您好!您本月已到店进行了三次项目护理，第四次给您免费做该项目升一级的护理，请您安排好时间，准确来护理哦！",
                customerName);
        return sendSms(phoneNumber, content);
    }
}