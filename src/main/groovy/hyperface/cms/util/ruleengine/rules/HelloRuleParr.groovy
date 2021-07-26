package hyperface.cms.util.ruleengine.rules

import groovy.util.logging.Slf4j
import hyperface.cms.util.ruleengine.ParallelRule
import org.springframework.stereotype.Component

/**
 * Sample parallel rule to demonstrate how the parallel rule should run.
 * To build the rule, please have your own method defined which takes custom params
 * and accordingly put your code in the {@code call} method for it to get executed.
 *
 * Please remove this once we have at least one concrete ParallelRule implementation.
 */

@Component
@Slf4j
class HelloRuleParr extends ParallelRule {

    @Override
    Boolean call() throws Exception {
        log.info("Hello, executed in parallel.")
        return Boolean.TRUE
    }

    @Override
    String name() {
        return "HELLO_RULE_PARALLEL"
    }

}
