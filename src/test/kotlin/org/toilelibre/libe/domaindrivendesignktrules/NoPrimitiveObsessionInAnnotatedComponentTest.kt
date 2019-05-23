package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.RuleSet
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class NoPrimitiveObsessionInAnnotatedComponentTest {

    @Test
    fun testWaivedViolationWhenThereIsOnlyOneArgument() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            """
            package some.packages

            @Action
            class MyAction {

              fun someAssertion(b: Boolean) {
              }

            }
            """.trimIndent(),
            listOf(RuleSet("test", NoPrimitiveObsessionInAnnotatedComponent()))
        ) { collector.add(it) }

        assertThat(collector).isEmpty()
    }

    @Test
    fun testViolation() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            """
            package some.packages

            @Action
            class MyAction {

              fun someMethod(i: Int, b: Boolean) {
              }

            }
            """.trimIndent(),
            listOf(RuleSet("test", NoPrimitiveObsessionInAnnotatedComponent()))
        ) { collector.add(it) }

        assertThat(collector).containsExactly(
            LintError(
                line = 6,
                col = 3,
                ruleId = "test:no-primitive-obsession-in-action-or-domain-service",
                detail = "This function someMethod uses too much primitive types. Please (re)use some @ValueType classes and pass them as parameters"
            )
        )
    }

    @Test
    fun testViolationWithOnlyOnePrimitiveArgument() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            """
            package some.packages

            @Action
            class MyAction {

              fun someMethod(paymentMean: PaymentMean, b: Boolean) {
              }

            }
            """.trimIndent(),
            listOf(RuleSet("test", NoPrimitiveObsessionInAnnotatedComponent()))
        ) { collector.add(it) }

        assertThat(collector).containsExactly(
            LintError(
                line = 6,
                col = 3,
                ruleId = "test:no-primitive-obsession-in-action-or-domain-service",
                detail = "This function someMethod uses too much primitive types. Please (re)use some @ValueType classes and pass them as parameters"
            )
        )
    }

    @Test
    fun testViolationWithOnlyOneNullablePrimitiveArgument() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            """
            package some.packages

            @Action
            class MyAction {

              fun someMethod(paymentMean: PaymentMean, b: Boolean?) {
              }

            }
            """.trimIndent(),
            listOf(RuleSet("test", NoPrimitiveObsessionInAnnotatedComponent()))
        ) { collector.add(it) }

        assertThat(collector).containsExactly(
            LintError(
                line = 6,
                col = 3,
                ruleId = "test:no-primitive-obsession-in-action-or-domain-service",
                detail = "This function someMethod uses too much primitive types. Please (re)use some @ValueType classes and pass them as parameters"
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

              fun someMethod(paymentMean: PaymentMean, billingInfo: BillingInfo) {
              }

            }
            """.trimIndent(),
            listOf(RuleSet("test", NoPrimitiveObsessionInAnnotatedComponent()))
        ) { collector.add(it) }

        assertThat(collector).isEmpty()
    }
}