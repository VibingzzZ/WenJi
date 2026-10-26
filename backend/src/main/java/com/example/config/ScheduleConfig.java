package com.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 定时任务配置
 * 用于支持：
 * 1. 聊天消息异步归档
 * 2. 向量嵌入批量生成
 * 3. 用户画像分析
 * 4. 个性化推荐数据更新
 */
@Configuration
@EnableScheduling
public class ScheduleConfig implements SchedulingConfigurer {

    /**
     * 配置定时任务的线程池
     * 使用独立线程池避免影响主业务
     */
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskScheduler());
    }

    /**
     * 创建定时任务线程池
     * @return 线程池对象
     */
    public Executor taskScheduler() {
        // 创建包含 5 个线程的线程池
        return Executors.newScheduledThreadPool(5, runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("ai-scheduler-" + thread.getName());
            thread.setDaemon(true); // 设置为守护线程
            return thread;
        });
    }
}
