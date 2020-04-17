package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.RuleSet
import com.winterbe.expekt.should
import org.junit.jupiter.api.Test

class NoGenericCatchTest {

    @Test
    fun testViolation() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            KtLint.Params(
                text =
            """
package some.packages


fun someMethod() {
 try {
   println("Hello World")
 } catch (e: Exception){
 }
}
        """.trimIndent(), ruleSets = listOf(RuleSet("test", NoGenericCatch())),
                cb = { e, _ -> collector.add(e) })
        )

        collector.should.contain(
            LintError(
                line = 7,
                col = 4,
                ruleId = "test:no-generic-catch",
                detail = "Please avoid catching generic Exception classes... like Exception"
            )
        )
    }
}
