package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.RuleSet
import com.winterbe.expekt.should
import org.junit.jupiter.api.Test

class NoIfInsideIfTest {

    @Test
    fun allowedWhenInExpression() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            KtLint.Params(
                text =
                """
package some.packages

fun someMethod() {
    val i = 0
    var below2 = false
    if (i > 0)
      below2 = if (i < 2) true else false
}
        """.trimIndent(), ruleSets = listOf(RuleSet("test", NoIfInsideIf())),
                cb = { e, _ -> collector.add(e) })
        )

        collector.should.be.empty
    }

    @Test
    fun elseIfIsAllowed() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            KtLint.Params(
                text =
                """
package some.packages

fun someMethod() {
    val i = 0
    if (i < 0)
      println("i must be below 0")
    else if (i >= 0)
       println ("i must be equal to 0")
    
}
        """.trimIndent(), ruleSets = listOf(RuleSet("test", NoIfInsideIf())),
                cb = { e, _ -> collector.add(e) })
        )

        collector.should.be.empty
    }

    @Test
    fun testViolation() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            KtLint.Params(
                text =
            """
package some.packages

fun someMethod() {
    val i = 0
    if (i <= 0) {
        if (i >= 0) {
            println ("i must be equal to 0")
        }
    }
}
        """.trimIndent(), ruleSets = listOf(RuleSet("test", NoIfInsideIf())),
                cb = { e, _ -> collector.add(e) })
        )

        collector.should.contain.all.the.elements(
            LintError(
                line = 6, col = 9,
                ruleId = "test:no-if-inside-if",
                detail = "This 'if' statement is nested inside another if. This is not allowed here"
            )
        )
    }
}
