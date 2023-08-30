package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.rule.engine.api.Code
import com.pinterest.ktlint.rule.engine.api.KtLintRuleEngine
import com.pinterest.ktlint.rule.engine.api.LintError
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.RuleProvider
import com.winterbe.expekt.should
import org.junit.jupiter.api.Test
import org.toilelibre.libe.domaindrivendesignktrules.DomainDrivenDesignRuleSetProvider.Companion.rulesetName

class NoIfInsideIfTest {

    @Test
    fun allowedWhenInExpression() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { NoIfInsideIf() }))
            .lint(
                Code.fromSnippet(
                    """
package some.packages

fun someMethod() {
    val i = 0
    var below2 = false
    if (i > 0)
      below2 = if (i < 2) true else false
}
                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.be.empty
    }

    @Test
    fun elseIfIsAllowed() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { NoIfInsideIf() }))
            .lint(
                Code.fromSnippet(
                    """
package some.packages

fun someMethod() {
    val i = 0
    if (i < 0)
      println("i must be below 0")
    else if (i >= 0)
       println ("i must be equal to 0")
    
}
                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.be.empty
    }

    @Test
    fun testViolation() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { NoIfInsideIf() }))
            .lint(
                Code.fromSnippet(
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
                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.contain.all.the.elements(
            LintError(
                line = 6,
                col = 9,
                ruleId = RuleId("$rulesetName:no-if-inside-if"),
                canBeAutoCorrected = false,
                detail = "This 'if' statement is nested inside another if. This is not allowed here",
            ),
        )
    }
}
