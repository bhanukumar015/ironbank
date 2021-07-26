package hyperface.cms.util.ruleengine

import java.util.concurrent.Callable

abstract class ParallelRule implements Rule, Callable<Boolean> {
}
