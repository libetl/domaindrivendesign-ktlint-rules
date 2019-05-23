package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.RuleSet
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class NoGenericCatchTest {

    @Test
    fun testViolation() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            """
            package some.packages


            fun someMethod() {
             try {
               println("Hello World")
             } catch (e: Exception){
             }
            }
            """.trimIndent(),
            listOf(RuleSet("test", NoGenericCatch()))
        ) { collector.add(it) }

        assertThat(collector).containsExactly(
            LintError(
                line = 7,
                col = 4,
                ruleId = "test:no-generic-catch",
                detail = "Please avoid catching generic Exception classes... like Exception"
            )
        )
    }
}