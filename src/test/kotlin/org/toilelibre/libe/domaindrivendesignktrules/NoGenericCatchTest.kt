package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.rule.engine.api.Code
import com.pinterest.ktlint.rule.engine.api.KtLintRuleEngine
import com.pinterest.ktlint.rule.engine.api.LintError
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.RuleProvider
import com.winterbe.expekt.should
import org.junit.jupiter.api.Test
import org.toilelibre.libe.domaindrivendesignktrules.DomainDrivenDesignRuleSetProvider.Companion.rulesetName

class NoGenericCatchTest {

    @Test
    fun testViolation() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { NoGenericCatch() }))
            .lint(
                Code.fromSnippet(
                    """
package some.packages


fun someMethod() {
 try {
   println("Hello World")
 } catch (e: Exception){
 }
}
                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.contain(
            LintError(
                line = 7,
                col = 4,
                ruleId = RuleId("$rulesetName:no-generic-catch"),
                canBeAutoCorrected = false,
                detail = "Please avoid catching generic Exception classes... like Exception",
            ),
        )
    }
}
