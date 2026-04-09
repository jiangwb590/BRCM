package com.bcrm;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * 应用上下文加载测试
 *
 * @author BCRM
 * @since 2026-03-14
 */
@SpringBootTest
@ActiveProfiles("test")
class BcrmApplicationTests {

    @Test
    void contextLoads() {
        // 测试Spring上下文是否能正常加载
    }
}
