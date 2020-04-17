package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.RuleSet
import com.winterbe.expekt.should
import org.junit.jupiter.api.Test

class NeedsOneCallToAnActionFromAControllerTest {
    @Test
    fun testViolation() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            KtLint.Params(
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

        """.trimIndent(), ruleSets = listOf(RuleSet("test", NeedsOneCallToAnActionFromAController())),
                cb = { e, _ -> collector.add(e) })
        )

        collector.should.contain(
            LintError(
                line = 11, col = 3,
                ruleId = "test:needs-one-call-to-an-action-from-a-controller",
                detail = "This function some.packages.TheController.someMethod does not call anything in the actions package. And it should."
            )
        )
    }
}
