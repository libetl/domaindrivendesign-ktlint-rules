package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.rule.engine.api.Code
import com.pinterest.ktlint.rule.engine.api.KtLintRuleEngine
import com.pinterest.ktlint.rule.engine.api.LintError
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.RuleProvider
import com.winterbe.expekt.should
import org.junit.jupiter.api.Test
import org.toilelibre.libe.domaindrivendesignktrules.DomainDrivenDesignRuleSetProvider.Companion.rulesetName

class ActionOnlyHasOnePublicMethodTest {

    @Test
    fun testViolation() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { ActionOnlyHasOnePublicMethod() }))
            .lint(
                Code.fromSnippet(
                    """
package some.packages

@Action
class MyAction {

  fun doIt(){
    this.doItBetter()
  }

  private fun doItBetter(){
  }

  fun doItAgain(){
    this.doItAgainBetter()
  }

  private fun doItAgainBetter(){
  }

}

                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.contain(
            LintError(
                line = 3,
                col = 1,
                ruleId = RuleId("$rulesetName:only-one-public-method-in-action"),
                canBeAutoCorrected = false,
                detail = "Action some.packages.MyAction should have one public method (found 2), and no private method (found 2)",
            ),
        )
    }

    @Test
    fun testNoViolation() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { ActionOnlyHasOnePublicMethod() }))
            .lint(
                Code.fromSnippet(
                    """
package some.packages

@Action
class MyAction {

  fun doIt(){
  }

}
                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.be.empty
    }
}
