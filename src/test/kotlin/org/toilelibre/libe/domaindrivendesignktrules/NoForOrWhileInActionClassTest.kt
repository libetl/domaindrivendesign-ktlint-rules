package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.rule.engine.api.Code
import com.pinterest.ktlint.rule.engine.api.KtLintRuleEngine
import com.pinterest.ktlint.rule.engine.api.LintError
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.RuleProvider
import com.winterbe.expekt.should
import org.junit.jupiter.api.Test
import org.toilelibre.libe.domaindrivendesignktrules.DomainDrivenDesignRuleSetProvider.Companion.rulesetName

class NoForOrWhileInActionClassTest {

    @Test
    fun testViolation() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { NoForOrWhileInActionClass() }))
            .lint(
                Code.fromSnippet(
                    """
package some.packages

@Action
class MyAction {

  fun someMethod() {
    for (i in 0..10){
      println("Hello World")
    }
  }
}
                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.contain(
            LintError(
                line = 7,
                col = 5,
                ruleId = RuleId("$rulesetName:no-for-or-while-in-action-class"),
                canBeAutoCorrected = false,
                detail =
                """Action contains a `for(. in .)`, this is discouraged.
The business logic must be written in declarative programming.
If you need to tell your reader that you are iterating over a group,
you can consider implementing some idiomatics :
(e.g. : forEachUserCalled {traveler ->...})""",
            ),
        )
    }

    @Test
    fun testNestedWhile() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { NoForOrWhileInActionClass() }))
            .lint(
                Code.fromSnippet(
                    """
package some.packages

@Action
class MyAction {
  private var hello = true

  fun someMethod() {
    if (hello){
      var i = 0
      while (i < 10){
        println("Hello World")
        i++
      }
    }
  }
}
                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.contain(
            LintError(
                line = 10,
                col = 7,
                ruleId = RuleId("$rulesetName:no-for-or-while-in-action-class"),
                canBeAutoCorrected = false,
                detail =
                """Action contains a `while(...){...}`, this is discouraged.
The business logic must be written in declarative programming.
If you need to tell your reader that you are iterating over a group,
you can consider implementing some idiomatics :
(e.g. : forEachUserCalled {traveler ->...})""",
            ),
        )
    }

    @Test
    fun testNoViolation() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { NoForOrWhileInActionClass() }))
            .lint(
                Code.fromSnippet(
                    """
package some.packages

data class Traveler(val id: Int)

fun List<Traveler>.tookApart(toDo: (Traveler) -> Unit) = this.map { toDo(it) }

@Action
class MyAction {
  private var hello = true

  fun someMethod(theTravelers : List<Traveler>) {
    theTravelers.tookApart { println ("Hello " + it) }
  }
}
                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.be.empty
    }
}
