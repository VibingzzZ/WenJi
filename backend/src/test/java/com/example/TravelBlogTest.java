package com.example;

import com.example.exception.BusinessException;
import com.example.po.pojo.TravelBlog;
import com.example.service.TravelBlogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TravelBlogTest {

    @Autowired
    private TravelBlogService blogService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

/**
 * 测试游记发布功能是否正常工作
 * 验证首次发布游记时系统能否正确处理并成功发布
 */
    @Test
    void testPublishBlogSuccessfully() {
        // 生成一个唯一的标题，避免重复
        String uniqueTitle = "西安城墙_" + UUID.randomUUID().toString().substring(0, 8);
        // 创建一个新的TravelBlog对象
        TravelBlog blog = new TravelBlog();
        // 设置博客的用户ID为1
        blog.setUserId(1);
        // 设置博客的标题为生成的唯一标题
        blog.setTitle(uniqueTitle);
        // 设置博客的内容为一段关于西安城墙的游记
        blog.setContent("今日登临古城墙，顿感历史之厚重...");
        // 设置博客的站点ID为"101"
        blog.setSiteId("101");

        // 验证发布博客操作不会抛出异常，首次发布应该成功
        assertDoesNotThrow(() -> blogService.publishBlog(blog),
            "首次发布游记应该成功");
        
        // 打印测试成功的消息
        System.out.println("测试结果：游记发布成功");
    }

/**
 * 测试使用分布式锁处理并发发布博客的场景
 * 验证在多线程环境下，分布式锁能否正确处理并发请求
 * @throws InterruptedException 如果线程被中断
 */
    @Test
    void testConcurrentPublishWithDistributedLock() throws InterruptedException {
        // 定义线程数量为5
        int threadCount = 5;
        // 创建固定大小的线程池
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        // 创建倒计时计数器，用于等待所有线程完成
        CountDownLatch latch = new CountDownLatch(threadCount);
        // 原子类计数器，用于统计成功次数
        AtomicInteger successCount = new AtomicInteger(0);
        // 原子类计数器，用于统计失败次数
        AtomicInteger failCount = new AtomicInteger(0);

        // 循环创建threadCount个任务
        for (int i = 0; i < threadCount; i++) {
            // 提交任务到线程池
            executor.submit(() -> {
                try {
                    // 创建新的TravelBlog对象
                    TravelBlog blog = new TravelBlog();
                    // 设置用户ID
                    blog.setUserId(999);
                    // 设置随机标题
                    blog.setTitle("并发测试_" + UUID.randomUUID().toString().substring(0, 8));
                    // 设置内容
                    blog.setContent("测试内容");
                    // 设置站点ID
                    blog.setSiteId("101");
                    
                    // 调用发布博客方法
                    blogService.publishBlog(blog);
                    // 成功计数器加1
                    successCount.incrementAndGet();
                } catch (BusinessException e) {
                    // 如果异常码为429（表示请求被限流）
                    if (e.getCode() == 429) {
                        // 失败计数器加1
                        failCount.incrementAndGet();
                    }
                } finally {
                    // 无论成功或失败，都减少倒计时计数器的值
                    latch.countDown();
                }
            });
        }

        // 等待所有线程完成
        latch.await();
        // 关闭线程池
        executor.shutdown();

        // 打印成功和失败的请求数量
        System.out.println("成功数: " + successCount.get() + ", 失败数: " + failCount.get());
        
        // 验证至少有一个请求成功
        assertTrue(successCount.get() >= 1, "至少有一个请求成功");
        // 验证至少有一个请求被锁拦截
        assertTrue(failCount.get() >= 1, "至少有一个请求被锁拦截");
        // 验证总请求数等于线程数
        assertEquals(threadCount, successCount.get() + failCount.get(), "总请求数应等于线程数");
        
        // 打印测试结果
        System.out.println("测试结果：分布式锁成功拦截并发重复请求");
    }
}
