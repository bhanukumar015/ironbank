package hyperface.cms.util.ruleengine

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import java.util.concurrent.ExecutorService
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

@Configuration
class ExecutorConfig {
    private static final CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2
    private static final MAX_POOL_SIZE = CORE_POOL_SIZE * 2
    private static final long KEEP_ALIVE_TIME_IN_MINUTES = 5l

    @Bean("threadpoolexecutor")
    ExecutorService threadPoolExecutorService() {

        ExecutorService executorService = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_TIME_IN_MINUTES,
                TimeUnit.MINUTES,
                new LinkedBlockingQueue<Runnable>()
        )
        return executorService
    }

}
