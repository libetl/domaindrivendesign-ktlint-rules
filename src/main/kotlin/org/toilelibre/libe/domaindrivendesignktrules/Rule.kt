package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.rule.engine.core.api.RuleId
import org.toilelibre.libe.domaindrivendesignktrules.DomainDrivenDesignRuleSetProvider.Companion.rulesetName

internal open class Rule(ruleName: String) : com.pinterest.ktlint.rule.engine.core.api.Rule(
    RuleId("$rulesetName:$ruleName"),
    About(
        maintainer = "libetl",
        repositoryUrl = "https://github.com/libetl/domaindrivendesign-ktlint-rules",
        issueTrackerUrl = "https://github.com/libetl/domaindrivendesign-ktlint-rules/issues",
    ),
)
