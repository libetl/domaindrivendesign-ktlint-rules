package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.RuleSet
import com.winterbe.expekt.should
import org.junit.jupiter.api.Test

class NoBreakOrContinueTest {

    @Test
    fun testViolation() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            KtLint.Params(
                text =
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
        """.trimIndent(), ruleSets = listOf(RuleSet("test", NoBreakOrContinue())),
                cb = { e, _ -> collector.add(e) })
        )

        collector.should.contain.all.the.elements(
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
