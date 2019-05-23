package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.RuleSet
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ActionOnlyHasOnePublicMethodTest {

    @Test
    fun testViolation() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
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

        """.trimIndent(), listOf(RuleSet("test", ActionOnlyHasOnePublicMethod()))
        ) { collector.add(it) }

        assertThat(collector).containsExactly(
            LintError(
                line = 3, col = 1, ruleId = "test:only-one-public-method-in-action",
                detail = "Action some.packages.MyAction should have one public method (found 2), and no private method (found 2)"
            )
        )
    }

    @Test
    fun testNoViolation() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            """
package some.packages

@Action
class MyAction {

  fun doIt(){
  }

}
        """.trimIndent(), listOf(RuleSet("test", ActionOnlyHasOnePublicMethod()))
        ) { collector.add(it) }

        assertThat(collector).isEmpty()
    }
}