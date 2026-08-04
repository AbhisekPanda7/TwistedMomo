package com.twistedmomos.backend.shared.config;

import org.springframework.boot.task.ThreadPoolTaskExecutorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.support.ContextPropagatingTaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Carries the trace context onto async threads.
 *
 * <p>Without the decorator an {@code @ApplicationModuleListener} logs with an empty trace —
 * so a failure while writing a report cannot be tied back to the order that caused it.
 * The context lives in a ThreadLocal, which does not follow the task across the handoff.
 */
@Configuration
public class AsyncConfig {

    @Bean
    ThreadPoolTaskExecutor applicationTaskExecutor(ThreadPoolTaskExecutorBuilder builder) {
        return builder.taskDecorator(new ContextPropagatingTaskDecorator()).build();
    }
}
