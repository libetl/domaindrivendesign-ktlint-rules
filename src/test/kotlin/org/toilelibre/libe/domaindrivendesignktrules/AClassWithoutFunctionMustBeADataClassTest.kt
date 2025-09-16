package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.rule.engine.api.Code
import com.pinterest.ktlint.rule.engine.api.KtLintRuleEngine
import com.pinterest.ktlint.rule.engine.api.LintError
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.RuleProvider
import com.winterbe.expekt.should
import org.junit.jupiter.api.Test
import org.toilelibre.libe.domaindrivendesignktrules.DomainDrivenDesignRuleSetProvider.Companion.rulesetName

class AClassWithoutFunctionMustBeADataClassTest {

    @Test
    fun testViolation() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { AClassWithoutFunctionMustBeADataClass() }))
            .lint(
                Code.fromSnippet(
                    """
package some.packages
class MyClass {
  private var myFieldOne: Int
  private var myFieldTwo: String
  private var myFieldThree: Boolean
}
                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.contain(
            LintError(
                line = 2,
                col = 1,
                ruleId = RuleId("$rulesetName:a-class-without-function-must-be-a-data-class"),
                canBeAutoCorrected = false,
                detail = "This class some.packages.MyClass does not have any function. Should not it be a data class ? In any case,Domain Driven Design discourages the use of anemic classes (POJO or value objects)",
            ),
        )
    }

    @Test
    fun testViolationWhileCheatingOnTheParentClass() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { AClassWithoutFunctionMustBeADataClass() }))
            .lint(
                Code.fromSnippet(
                    """
package some.packages
class MyClass : Object {
  private var myFieldOne: Int
  private var myFieldTwo: String
  private var myFieldThree: Boolean
}
                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.contain(
            LintError(
                line = 2,
                col = 1,
                ruleId = RuleId("$rulesetName:a-class-without-function-must-be-a-data-class"),
                canBeAutoCorrected = false,
                detail = "This class some.packages.MyClass does not have any function. Should not it be a data class ? In any case,Domain Driven Design discourages the use of anemic classes (POJO or value objects)",
            ),
        )
    }

    @Test
    fun testNoViolationForInlineValueClass() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { AClassWithoutFunctionMustBeADataClass() }))
            .lint(
                Code.fromSnippet(
                    """
package some.packages
@JvmInline
value class MyClassValue(val value: Int)
                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.be.empty
    }
}
