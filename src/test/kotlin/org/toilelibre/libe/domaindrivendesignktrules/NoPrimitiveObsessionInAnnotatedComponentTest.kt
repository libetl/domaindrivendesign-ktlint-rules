package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.rule.engine.api.Code
import com.pinterest.ktlint.rule.engine.api.KtLintRuleEngine
import com.pinterest.ktlint.rule.engine.api.LintError
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.RuleProvider
import com.winterbe.expekt.should
import org.junit.jupiter.api.Test
import org.toilelibre.libe.domaindrivendesignktrules.DomainDrivenDesignRuleSetProvider.Companion.rulesetName

class NoPrimitiveObsessionInAnnotatedComponentTest {

    @Test
    fun testWaivedViolationWhenThereIsOnlyOneArgument() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { NoPrimitiveObsessionInAnnotatedComponent() }))
            .lint(
                Code.fromSnippet(
                    """
package some.packages

@Action
class MyAction {

  fun someAssertion(b: Boolean) {
  }

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
        KtLintRuleEngine(setOf(RuleProvider { NoPrimitiveObsessionInAnnotatedComponent() }))
            .lint(
                Code.fromSnippet(
                    """
package some.packages

@Action
class MyAction {

  fun someMethod(i: Int, b: Boolean) {
  }

}
                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.contain(
            LintError(
                line = 6,
                col = 3,
                ruleId = RuleId("$rulesetName:no-primitive-obsession-in-action-or-domain-service"),
                canBeAutoCorrected = false,
                detail = "This function someMethod uses too much primitive types. Please (re)use some @ValueType classes and pass them as parameters",
            ),
        )
    }

    @Test
    fun testViolationWithOnlyOnePrimitiveArgument() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { NoPrimitiveObsessionInAnnotatedComponent() }))
            .lint(
                Code.fromSnippet(
                    """
package some.packages

@Action
class MyAction {

  fun someMethod(paymentMean: PaymentMean, b: Boolean) {
  }

}
                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.contain(
            LintError(
                line = 6,
                col = 3,
                ruleId = RuleId("$rulesetName:no-primitive-obsession-in-action-or-domain-service"),
                canBeAutoCorrected = false,
                detail = "This function someMethod uses too much primitive types. Please (re)use some @ValueType classes and pass them as parameters",
            ),
        )
    }

    @Test
    fun testViolationWithOnlyOneNullablePrimitiveArgument() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { NoPrimitiveObsessionInAnnotatedComponent() }))
            .lint(
                Code.fromSnippet(
                    """
package some.packages

@Action
class MyAction {

  fun someMethod(paymentMean: PaymentMean, b: Boolean?) {
  }

}
                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.contain(
            LintError(
                line = 6,
                col = 3,
                ruleId = RuleId("$rulesetName:no-primitive-obsession-in-action-or-domain-service"),
                canBeAutoCorrected = false,
                detail = "This function someMethod uses too much primitive types. Please (re)use some @ValueType classes and pass them as parameters",
            ),
        )
    }

    @Test
    fun testNoViolation() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { NoPrimitiveObsessionInAnnotatedComponent() }))
            .lint(
                Code.fromSnippet(
                    """
package some.packages

@Action
class MyAction {

  fun someMethod(paymentMean: PaymentMean, billingInfo: BillingInfo) {
  }

}
                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.be.empty
    }
}
