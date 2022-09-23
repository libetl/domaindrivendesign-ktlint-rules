package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.core.KtLint
import com.winterbe.expekt.should
import org.junit.jupiter.api.Test

class AClassWithoutFunctionMustBeADataClassTest {

    @Test
    fun testViolation() {
        val collector = mutableListOf<com.pinterest.ktlint.core.LintError>()
        KtLint.lint(
            KtLint.ExperimentalParams(
                text = """
package some.packages
class MyClass {
  private var myFieldOne: Int
  private var myFieldTwo: String
  private var myFieldThree: Boolean
}
                """.trimIndent(),
                ruleProviders = setOf(RuleProvider { AClassWithoutFunctionMustBeADataClass() }),
                cb = { e, _ -> collector.add(e) }
            )
        )

        collector.should.contain(
            LintError(
                line = 2,
                col = 1,
                ruleId = "a-class-without-function-must-be-a-data-class",
                detail = "This class some.packages.MyClass does not have any function. Should not it be a data class ? In any case,Domain Driven Design discourages the use of anemic classes (POJO or value objects)"
            )
        )
    }

    @Test
    fun testViolationWhileCheatingOnTheParentClass() {
        val collector = mutableListOf<com.pinterest.ktlint.core.LintError>()
        KtLint.lint(
            KtLint.ExperimentalParams(
                text = """
package some.packages
class MyClass : Object {
  private var myFieldOne: Int
  private var myFieldTwo: String
  private var myFieldThree: Boolean
}
                """.trimIndent(),
                ruleProviders = setOf(RuleProvider { AClassWithoutFunctionMustBeADataClass() }),
                cb = { e, _ -> collector.add(e) }
            )
        )

        collector.should.contain(
            LintError(
                line = 2,
                col = 1,
                ruleId = "a-class-without-function-must-be-a-data-class",
                detail = "This class some.packages.MyClass does not have any function. Should not it be a data class ? In any case,Domain Driven Design discourages the use of anemic classes (POJO or value objects)"
            )
        )
    }
}
