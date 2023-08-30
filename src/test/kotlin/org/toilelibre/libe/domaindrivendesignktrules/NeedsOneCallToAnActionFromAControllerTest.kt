package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.rule.engine.api.Code
import com.pinterest.ktlint.rule.engine.api.KtLintRuleEngine
import com.pinterest.ktlint.rule.engine.api.LintError
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.RuleProvider
import com.winterbe.expekt.should
import org.junit.jupiter.api.Test
import org.toilelibre.libe.domaindrivendesignktrules.DomainDrivenDesignRuleSetProvider.Companion.rulesetName

class NeedsOneCallToAnActionFromAControllerTest {

    @Test
    fun noViolationIfActionInvocationInsideNestedBlock() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { NeedsOneCallToAnActionFromAController() }))
            .lint(
                Code.fromSnippet(
                    """
        package some.packages.infra.endpoint

        import some.packages.actions.HandleVerifyRequest
        
        @Action
        class HandleVerifyRequest {
            fun with(verifyOperationRequest: VerifyOperationRequest){
            }
        }

        @Controller
        class TheController(val handleVerifyRequest: HandleVerifyRequest) {
            fun performVerify(
                @RequestBody verifyOperationRequest: VerifyOperationRequest
            ) = runBlocking {
                handleVerifyRequest.with(verifyOperationRequest)
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
        KtLintRuleEngine(setOf(RuleProvider { NeedsOneCallToAnActionFromAController() }))
            .lint(
                Code.fromSnippet(
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
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.contain(
            LintError(
                line = 11,
                col = 3,
                ruleId = RuleId("$rulesetName:needs-one-call-to-an-action-from-a-controller"),
                canBeAutoCorrected = false,
                detail = "This function some.packages.TheController.someMethod does not call anything in the actions package. And it should.",
            ),
        )
    }

    @Test
    fun violationIfTheMethodIsAMessageQueueListener() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { NeedsOneCallToAnActionFromAController() }))
            .lint(
                Code.fromSnippet(
                    """
package some.packages

class TheController {
  @RabbitListener
  fun someMethod() {
    println("Hello world")
  }
}

                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.contain(
            LintError(
                line = 4,
                col = 3,
                ruleId = RuleId("$rulesetName:needs-one-call-to-an-action-from-a-controller"),
                canBeAutoCorrected = false,
                detail = "This function some.packages.TheController.someMethod does not call anything in the actions package. And it should.",
            ),
        )
    }

    @Test
    fun noViolationIfTheMethodIsJustAnInternalEventListener() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { NeedsOneCallToAnActionFromAController() }))
            .lint(
                Code.fromSnippet(
                    """
package some.packages

class TheController {
  @EventListener
  fun someMethod() {
    println("Hello world")
  }
}

                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.be.empty
    }
}
