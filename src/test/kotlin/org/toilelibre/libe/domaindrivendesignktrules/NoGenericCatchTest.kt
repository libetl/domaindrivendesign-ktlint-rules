package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.core.KtLint
import com.winterbe.expekt.should
import org.junit.jupiter.api.Test

class NoGenericCatchTest {

    @Test
    fun testViolation() {
        val collector = mutableListOf<com.pinterest.ktlint.core.LintError>()
        KtLint.lint(
            KtLint.ExperimentalParams(
                text =
                """
package some.packages


fun someMethod() {
 try {
   println("Hello World")
 } catch (e: Exception){
 }
}
                """.trimIndent(),
                ruleProviders = setOf(RuleProvider { NoGenericCatch() }),
                cb = { e, _ -> collector.add(e) }
            )
        )

        collector.should.contain(
            LintError(
                line = 7,
                col = 4,
                ruleId = "no-generic-catch",
                detail = "Please avoid catching generic Exception classes... like Exception"
            )
        )
    }
}
