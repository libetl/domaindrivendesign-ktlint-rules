package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.RuleSet
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class NoForOrWhileInActionClassTest {

    @Test
    fun testViolation() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
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
            listOf(RuleSet("test", NoForOrWhileInActionClass()))
        ) { collector.add(it) }

        assertThat(collector).containsExactly(
            LintError(
                line = 7, col = 5, ruleId = "test:no-for-or-while-in-action-class",
                detail =
                    """Action contains a `for(. in .)`, this is discouraged.
The business logic must be written in declarative programming.
If you need to tell your reader that you are iterating over a group,
you can consider implementing some idiomatics :
(e.g. : forEachUserCalled {traveler ->...})"""
            )
        )
    }

    @Test
    fun testNestedWhile() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
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
            listOf(RuleSet("test", NoForOrWhileInActionClass()))
        ) { collector.add(it) }

        assertThat(collector).containsExactly(
            LintError(
                line = 10, col = 7, ruleId = "test:no-for-or-while-in-action-class",
                detail =
                    """Action contains a `while(...){...}`, this is discouraged.
The business logic must be written in declarative programming.
If you need to tell your reader that you are iterating over a group,
you can consider implementing some idiomatics :
(e.g. : forEachUserCalled {traveler ->...})"""
            )
        )
    }

    @Test
    fun testNoViolation() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
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
            listOf(RuleSet("test", NoForOrWhileInActionClass()))
        ) { collector.add(it) }

        assertThat(collector).isEmpty()
    }
}