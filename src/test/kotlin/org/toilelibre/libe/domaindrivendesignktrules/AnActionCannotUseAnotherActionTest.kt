package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.RuleSet
import com.winterbe.expekt.should
import org.junit.jupiter.api.Test

class AnActionCannotUseAnotherActionTest {

    @Test
    fun testViolation1() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            KtLint.Params(
                text =
            """
package some.packages

@Action
class MyAction1 {
}

@Action
class MyAction2 {
   val action1: MyAction1
}


        """.trimIndent(), ruleSets = listOf(RuleSet("test", AnActionCannotUseAnotherAction())),
                cb = { e, _ -> collector.add(e) })
        )

        collector.should.contain(
            LintError(
                line = 7, col = 1, ruleId = "test:an-action-cannot-use-another-action",
                detail = "Action MyAction2 should not use Action MyAction1"
            )
        )
    }

    @Test
    fun testViolation2() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            KtLint.Params(
                text =
                """
package some.packages

@Action
class MyAction1 {
  val myAction2: MyAction2
}

@Action
class MyAction2 {
}


        """.trimIndent(), ruleSets = listOf(RuleSet("test", AnActionCannotUseAnotherAction())),
                cb = { e, _ -> collector.add(e) })
        )

        collector.should.contain(
            LintError(
                line = 3, col = 1, ruleId = "test:an-action-cannot-use-another-action",
                detail = "Action MyAction1 should not use Action MyAction2"
            )
        )
    }
}
