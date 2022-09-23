package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.core.KtLint
import com.winterbe.expekt.should
import org.junit.jupiter.api.Test

class NeedsOneCallToAnActionFromAControllerTest {
    @Test
    fun testViolation() {
        val collector = mutableListOf<com.pinterest.ktlint.core.LintError>()
        KtLint.lint(
            KtLint.ExperimentalParams(
                text =
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
                ruleProviders = setOf(RuleProvider { NeedsOneCallToAnActionFromAController() }),
                cb = { e, _ -> collector.add(e) }
            )
        )

        collector.should.contain(
            LintError(
                line = 11,
                col = 3,
                ruleId = "needs-one-call-to-an-action-from-a-controller",
                detail = "This function some.packages.TheController.someMethod does not call anything in the actions package. And it should."
            )
        )
    }
}
