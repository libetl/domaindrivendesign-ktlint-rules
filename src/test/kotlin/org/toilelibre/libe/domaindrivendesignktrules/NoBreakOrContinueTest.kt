package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.rule.engine.api.Code
import com.pinterest.ktlint.rule.engine.api.KtLintRuleEngine
import com.pinterest.ktlint.rule.engine.api.LintError
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.RuleProvider
import com.winterbe.expekt.should
import org.junit.jupiter.api.Test
import org.toilelibre.libe.domaindrivendesignktrules.DomainDrivenDesignRuleSetProvider.Companion.rulesetName

class NoBreakOrContinueTest {

    @Test
    fun testViolation() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { NoBreakOrContinue() }))
            .lint(
                Code.fromSnippet(
                    """
package some.packages

fun someMethod() {
  var i
  for (i in 0..10){
    if (i == 2) continue
    println (i + " (skipping 3)")
    if (i == 4) break
  }
}
                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.contain.all.the.elements(
            LintError(
                line = 6,
                col = 17,
                ruleId = RuleId("$rulesetName:no-break-or-continue"),
                canBeAutoCorrected = false,
                detail = "Loop or statement breakers like break or continue are not allowed. Please do it wiser",
            ),
            LintError(
                line = 8,
                col = 17,
                ruleId = RuleId("$rulesetName:no-break-or-continue"),
                canBeAutoCorrected = false,
                detail = "Loop or statement breakers like break or continue are not allowed. Please do it wiser",
            ),
        )
    }
}
