package com.bcrm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bcrm.common.PageRequest;
import com.bcrm.entity.Message;
import com.bcrm.mapper.MessageMapper;
import com.bcrm.service.impl.MessageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 消息服务测试
 *
 * @author BCRM
 * @since 2026-03-14
 */
@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageMapper messageMapper;

    @InjectMocks
    private MessageServiceImpl messageService;

    private Message testMessage;

    @BeforeEach
    void setUp() {
        testMessage = new Message();
        testMessage.setId(1L);
        testMessage.setTitle("测试消息");
        testMessage.setContent("这是一条测试消息");
        testMessage.setMessageType("系统消息");
        testMessage.setReceiverId(1L);
        testMessage.setReceiverName("admin");
        testMessage.setIsRead(0);
        testMessage.setDeleted(0);
        testMessage.setCreateTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("分页查询消息测试")
    void testPageMessages() {
        // 准备测试数据
        PageRequest pageRequest = new PageRequest();
        pageRequest.setCurrent(1);
        pageRequest.setSize(10);

        Page<Message> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(testMessage));
        page.setTotal(1);

        when(messageMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        // 执行测试
        Page<Message> result = messageService.pageMessages(pageRequest, new Message());

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
    }

    @Test
    @DisplayName("获取未读消息测试")
    void testGetUnreadMessages() {
        // 准备测试数据
        when(messageMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(testMessage));

        // 执行测试
        List<Message> result = messageService.getUnreadMessages(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(0, result.get(0).getIsRead());
    }

    @Test
    @DisplayName("获取未读消息数量测试")
    void testGetUnreadCount() {
        // 准备测试数据
        when(messageMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);

        // 执行测试
        long count = messageService.getUnreadCount(1L);

        // 验证结果
        assertEquals(5, count);
    }

    @Test
    @DisplayName("标记消息已读测试")
    void testMarkAsRead() {
        // 准备测试数据
        when(messageMapper.selectById(1L)).thenReturn(testMessage);
        when(messageMapper.updateById(any(Message.class))).thenReturn(1);

        // 执行测试
        assertDoesNotThrow(() -> messageService.markAsRead(1L));

        // 验证update被调用
        verify(messageMapper, times(1)).updateById(any(Message.class));
    }

    @Test
    @DisplayName("标记所有消息已读测试")
    void testMarkAllAsRead() {
        // 准备测试数据
        when(messageMapper.update(any(), any(LambdaUpdateWrapper.class))).thenReturn(3);

        // 执行测试
        assertDoesNotThrow(() -> messageService.markAllAsRead(1L));

        // 验证update被调用
        verify(messageMapper, times(1)).update(any(), any(LambdaUpdateWrapper.class));
    }

    @Test
    @DisplayName("发送消息测试")
    void testSendMessage() {
        // 准备测试数据
        Message newMessage = new Message();
        newMessage.setTitle("新消息");
        newMessage.setContent("消息内容");
        newMessage.setReceiverId(1L);
        when(messageMapper.insert(any(Message.class))).thenReturn(1);

        // 执行测试
        assertDoesNotThrow(() -> messageService.sendMessage(newMessage));

        // 验证insert被调用
        verify(messageMapper, times(1)).insert(any(Message.class));
    }
}
