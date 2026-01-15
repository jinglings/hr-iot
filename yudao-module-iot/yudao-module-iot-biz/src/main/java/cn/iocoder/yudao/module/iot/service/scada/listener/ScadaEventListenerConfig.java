package cn.iocoder.yudao.module.iot.service.scada.listener;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * SCADA 事件监听器配置
 *
 * 配置异步事件处理的线程池
 *
 * Part of SCADA-015: Implement Device Property Change Listener
 *
 * @author HR-IoT Team
 */
@Configuration
@ConditionalOnProperty(prefix = "scada.mqtt", name = "enabled", havingValue = "true", matchIfMissing = false)
public class ScadaEventListenerConfig {

    /**
     * SCADA 事件处理线程池
     *
     * 用于异步处理设备属性变化事件
     */
    @Bean("scadaEventExecutor")
    public Executor scadaEventExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数
        executor.setCorePoolSize(4);
        // 最大线程数
        executor.setMaxPoolSize(8);
        // 队列容量
        executor.setQueueCapacity(1000);
        // 线程名前缀
        executor.setThreadNamePrefix("scada-event-");
        // 线程空闲时间 (秒)
        executor.setKeepAliveSeconds(60);
        // 允许核心线程超时
        executor.setAllowCoreThreadTimeOut(true);
        // 拒绝策略：由调用线程处理
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待任务完成再关闭
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 等待时间 (秒)
        executor.setAwaitTerminationSeconds(30);

        executor.initialize();
        return executor;
    }

}
