package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.RuleSet
import com.winterbe.expekt.should
import org.junit.jupiter.api.Test

class ADataClassCannotUseAMapTest {

    @Test
    fun testNoViolation() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            KtLint.Params(text =
            """
package some.packages

@ValueType
data class MyClass (
  val myFieldOne: Int,
  val myFieldTwo: String,
  val myFieldThree: Boolean
)

        """.trimIndent(), ruleSets = listOf(RuleSet("test", ADataClassCannotUseAMap())),
                cb = { e, _ -> collector.add(e) })
        )

        collector.should.be.empty
    }

    @Test
    fun testViolation() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            KtLint.Params(
                text =
                """
package some.packages

@ValueType
data class MyClass (
  val myFieldOne: Int,
  val myFieldTwo: String,
  val myFieldThree: Map<String, Any>
)

        """.trimIndent(), ruleSets = listOf(RuleSet("test", ADataClassCannotUseAMap())),
                cb = { e, _ -> collector.add(e) })
        )

        collector.should.contain(
            LintError(
                line = 7,
                col = 3,
                ruleId = "test:data-class-cannot-use-a-map",
                detail = "This variable : some.packages.MyClass.myFieldThree is a map (we cannot accept map as data " +
                    "class members because marshalling / unmarshalling has a lot of concerns)"
            )
        )
    }
}
