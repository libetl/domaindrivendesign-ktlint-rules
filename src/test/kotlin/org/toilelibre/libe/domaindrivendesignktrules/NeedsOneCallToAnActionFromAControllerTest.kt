package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.RuleSet
import org.assertj.core.api.Assertions
import org.junit.Test

class NeedsOneCallToAnActionFromAControllerTest {
    @Test
    fun testViolation() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            """
            package some.packages

            @Action
            class TheAction {
              fun doIt(){
              }
            }

            @Controller
            class TheController {
              fun someMethod() {
                println("Hello world")
              }
            }

            """.trimIndent(),
            listOf(RuleSet("test", NeedsOneCallToAnActionFromAController()))
        ) { collector.add(it) }

        Assertions.assertThat(collector).containsExactly(
            LintError(
                line = 11, col = 3,
                ruleId = "test:needs-one-call-to-an-action-from-a-controller",
                detail = "This function some.packages.TheController.someMethod does not call anything in the actions package. And it should."
            )
        )
    }
}