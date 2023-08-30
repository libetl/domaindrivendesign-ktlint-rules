package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.rule.engine.api.Code
import com.pinterest.ktlint.rule.engine.api.KtLintRuleEngine
import com.pinterest.ktlint.rule.engine.api.LintError
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.RuleProvider
import com.winterbe.expekt.should
import org.junit.jupiter.api.Test
import org.toilelibre.libe.domaindrivendesignktrules.DomainDrivenDesignRuleSetProvider.Companion.rulesetName

class ADataClassCannotUseAMapTest {

    @Test
    fun testNoViolation() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { ADataClassCannotUseAMap() }))
            .lint(
                Code.fromSnippet(
                    """
package some.packages

@ValueType
data class MyClass (
  val myFieldOne: Int,
  val myFieldTwo: String,
  val myFieldThree: Boolean
)

                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.be.empty
    }

    @Test
    fun testViolation() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { ADataClassCannotUseAMap() }))
            .lint(
                Code.fromSnippet(
                    """
package some.packages

@ValueType
data class MyClass (
  val myFieldOne: Int,
  val myFieldTwo: String,
  val myFieldThree: Map<String, Any>
)

                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.contain(
            LintError(
                line = 7,
                col = 3,
                ruleId = RuleId("$rulesetName:data-class-cannot-use-a-map"),
                canBeAutoCorrected = false,
                detail = "This variable : some.packages.MyClass.myFieldThree is a map (we cannot accept map as data " +
                    "class members because marshalling / unmarshalling has a lot of concerns)",
            ),
        )
    }
}
