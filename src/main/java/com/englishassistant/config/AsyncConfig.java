package com.englishassistant.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Async thread pool configuration.
 * - Core threads = CPU cores (for I/O-bound tasks like AI API calls)
 * - Queue = 100 (buffer overflow before rejecting)
 * - Rejection policy = CallerRunsPolicy (graceful degradation, don't lose tasks)
 *
 * Interview talking points:
 * - Why not default SimpleAsyncTaskExecutor? → Unbounded, OOM risk.
 * - Why CallerRunsPolicy? → Backpressure: when pool+queue full, caller thread runs task.
 * - Core vs Max threads trade-off for I/O bound tasks.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    private static final Logger log = LoggerFactory.getLogger(AsyncConfig.class);

    @Bean("aiTaskExecutor")
    public Executor aiTaskExecutor() {
        int cores = Runtime.getRuntime().availableProcessors();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(cores);
        executor.setMaxPoolSize(cores * 2);
        executor.setQueueCapacity(100);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("ai-async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();

        log.info("Async thread pool initialized: core={}, max={}, queue=100",
            cores, cores * 2);
        return executor;
    }
}
