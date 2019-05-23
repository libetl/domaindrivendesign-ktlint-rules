package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.RuleSet
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class NoBreakOrContinueTest {

    @Test
    fun testViolation() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
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
        """.trimIndent(), listOf(RuleSet("test", NoBreakOrContinue()))
        ) { collector.add(it) }

        assertThat(collector).containsExactly(
            LintError(
                line = 6, col = 17,
                ruleId = "test:no-break-or-continue",
                detail = "Loop or statement breakers like break or continue are not allowed. Please do it wiser"
            ),
            LintError(
                line = 8, col = 17,
                ruleId = "test:no-break-or-continue",
                detail = "Loop or statement breakers like break or continue are not allowed. Please do it wiser"
            )
        )
    }
}