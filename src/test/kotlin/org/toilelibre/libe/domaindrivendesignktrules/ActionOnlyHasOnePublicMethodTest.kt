package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.core.KtLint
import com.winterbe.expekt.should
import org.junit.jupiter.api.Test

class ActionOnlyHasOnePublicMethodTest {

    @Test
    fun testViolation() {
        val collector = mutableListOf<com.pinterest.ktlint.core.LintError>()
        KtLint.lint(
            KtLint.ExperimentalParams(
                text =
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
                ruleProviders = setOf(RuleProvider { ActionOnlyHasOnePublicMethod() }),
                cb = { e, _ -> collector.add(e) }
            )
        )

        collector.should.contain(
            LintError(
                line = 3,
                col = 1,
                ruleId = "only-one-public-method-in-action",
                detail = "Action some.packages.MyAction should have one public method (found 2), and no private method (found 2)"
            )
        )
    }

    @Test
    fun testNoViolation() {
        val collector = mutableListOf<com.pinterest.ktlint.core.LintError>()
        KtLint.lint(
            KtLint.ExperimentalParams(
                text =
                """
package some.packages

@Action
class MyAction {

  fun doIt(){
  }

}
                """.trimIndent(),
                ruleProviders = setOf(RuleProvider { ActionOnlyHasOnePublicMethod() }),
                cb = { e, _ -> collector.add(e) }
            )
        )

        collector.should.be.empty
    }
}
