package hyperface.cms.util.ruleengine.rules

import groovy.util.logging.Slf4j
import hyperface.cms.util.ruleengine.SequentialRule
import org.springframework.stereotype.Component

/**
 * Sample sequential rule to demonstrate how the sequential rule should run.
 * To build the rule, please have your own method defined which takes custom params
 * and accordingly put your code in the {@code execute} method for it to get executed.
 *
 * Please remove this once we have at least one concrete SequentialRule implementation.
 */

@Component
@Slf4j
class HelloRuleSeq extends SequentialRule {

    @Override
    Boolean execute() {
        log.info("Hello, executed sequentially!")
        return Boolean.TRUE
    }

    @Override
    String name() {
        return "HELLO_RULE_SEQUENTIAL"
    }
}
