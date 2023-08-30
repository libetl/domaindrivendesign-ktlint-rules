package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.rule.engine.api.Code
import com.pinterest.ktlint.rule.engine.api.KtLintRuleEngine
import com.pinterest.ktlint.rule.engine.api.LintError
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.RuleProvider
import com.winterbe.expekt.should
import org.junit.jupiter.api.Test
import org.toilelibre.libe.domaindrivendesignktrules.DomainDrivenDesignRuleSetProvider.Companion.rulesetName

class AnActionCannotUseAnotherActionTest {

    @Test
    fun testViolation1() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { AnActionCannotUseAnotherAction() }))
            .lint(
                Code.fromSnippet(
                    """
package some.packages

@Action
class MyAction1 {
}

@Action
class MyAction2 {
   val action1: MyAction1
}


                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.contain(
            LintError(
                line = 7,
                col = 1,
                ruleId = RuleId("$rulesetName:an-action-cannot-use-another-action"),
                canBeAutoCorrected = false,
                detail = "Action MyAction2 should not use Action MyAction1",
            ),
        )
    }

    @Test
    fun testViolation2() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { AnActionCannotUseAnotherAction() }))
            .lint(
                Code.fromSnippet(
                    """
package some.packages

@Action
class MyAction1 {
  val myAction2: MyAction2
}

@Action
class MyAction2 {
}


                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.contain(
            LintError(
                line = 3,
                col = 1,
                ruleId = RuleId("$rulesetName:an-action-cannot-use-another-action"),
                canBeAutoCorrected = false,
                detail = "Action MyAction1 should not use Action MyAction2",
            ),
        )
    }
}
