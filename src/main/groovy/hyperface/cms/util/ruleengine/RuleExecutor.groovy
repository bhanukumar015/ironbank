package hyperface.cms.util.ruleengine

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.util.CollectionUtils

import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.TimeUnit

/**
 * This class executes the submitted collection of Sequential
 * or Parallel rules and return an overall result, TRUE or FALSE.
 *
 */

@Component
@Slf4j
final class RuleExecutor {

    private static final long PARALLEL_RULES_TIME_OUT = 10l

    @Autowired
    @Qualifier("threadpoolexecutor")
    private ExecutorService taskExecutor

    /**
     * Takes a {@code List<SequentialRule>} and runs them sequentially.
     *
     * @param sequentialRules - {@link java.util.List} of {@link hyperface.cms.util.ruleengine.SequentialRule}
     * @return {@code TRUE} if all the sequential rules passed, {@code FALSE}
     * if any one of the rule fails and {@code NULL} in case of empty list passed or
     * an exception has occurred.
     */
    Boolean executeSequentialRules(final List<SequentialRule> sequentialRules) {
        try {
            if (CollectionUtils.isEmpty(sequentialRules)) {
                log.error("Empty sequential rule list passed.")
                return null
            }
            for (SequentialRule rule : sequentialRules) {
                Boolean result = rule.execute()
                if (Boolean.FALSE == result) {
                    log.error("Sequential Rule: [{}] failed.", rule.name())
                    return result
                }
            }
            log.info("All sequential rules passed.")
            return Boolean.TRUE
        } catch (Exception e) {
            log.error("Execution of sequential rule failed. Exception: {}", e.getLocalizedMessage())
            return null
        }
    }

    /**
     * Takes a {@code List<ParallelRule>} and runs them sequentially.
     *
     * @param parallelRules - {@link java.util.List} of {@link hyperface.cms.util.ruleengine.ParallelRule}
     * @return {@code TRUE} if all the parallel rules passed, {@code FALSE}
     * if any one of the rule fails and {@code NULL} in case of empty list passed or
     * an exception has occurred.
     */
    Boolean executeParallelRules(final List<ParallelRule> parallelRules) {
        if (CollectionUtils.isEmpty(parallelRules)) {
            log.error("Empty parallel rule list passed.")
            return null
        }
        try {
            List<Future<Boolean>> results = taskExecutor
                    .invokeAll(parallelRules, PARALLEL_RULES_TIME_OUT, TimeUnit.SECONDS)
            int failedResultsCount = results
                    .stream()
                    .filter(res -> !res.isDone())
                    .count()
            if (failedResultsCount > 0) {
                log.error("[{}] parallel rules failed.", failedResultsCount)
                return Boolean.FALSE
            }
            log.info("All parallel rules passed.")
            return Boolean.TRUE
        } catch (InterruptedException ie) {
            log.error("Executor service interrupted. Aborting rules execution. Exception: {}", ie.getLocalizedMessage())
            return null
        } catch (RejectedExecutionException re) {
            log.error("Executor rejected rule. Aborting rules execution. Exception: {}", re.getLocalizedMessage())
            return null
        }
    }

}
