package com.bcrm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bcrm.common.PageRequest;
import com.bcrm.entity.Message;
import com.bcrm.mapper.MessageMapper;
import com.bcrm.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息服务实现
 *
 * @author BCRM
 * @since 2026-03-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    @Override
    public Page<Message> pageMessages(PageRequest pageRequest, Message query) {
        Page<Message> page = pageRequest.toPage();
        
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getTitle()), Message::getTitle, query.getTitle());
//        wrapper.eq(query.getReceiverId() != null, Message::getReceiverId, query.getReceiverId());
        wrapper.eq(StringUtils.hasText(query.getMessageType()), Message::getMessageType, query.getMessageType());
        wrapper.eq(query.getIsRead() != null, Message::getIsRead, query.getIsRead());
        wrapper.orderByDesc(Message::getCreateTime);
        
        return this.page(page, wrapper);
    }

    @Override
    public List<Message> getUnreadMessages(Long receiverId) {
        return this.list(new LambdaQueryWrapper<Message>()
//                .eq(Message::getReceiverId, receiverId)
                .eq(Message::getIsRead, 0)
                .orderByDesc(Message::getCreateTime));
    }

    @Override
    public long getUnreadCount(Long receiverId) {
        return this.count(new LambdaQueryWrapper<Message>()
//                .eq(Message::getReceiverId, receiverId)
                .eq(Message::getIsRead, 0));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long id) {
        Message message = this.getById(id);
        if (message != null && message.getIsRead() == 0) {
            Message updateMessage = new Message();
            updateMessage.setId(id);
            updateMessage.setIsRead(1);
            updateMessage.setReadTime(LocalDateTime.now());
            this.updateById(updateMessage);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead(Long receiverId) {
        this.update(new LambdaUpdateWrapper<Message>()
                .set(Message::getIsRead, 1)
                .set(Message::getReadTime, LocalDateTime.now())
//                .eq(Message::getReceiverId, receiverId)
                .eq(Message::getIsRead, 0));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendMessage(Message message) {
        message.setIsRead(0);
        this.save(message);
    }
}
