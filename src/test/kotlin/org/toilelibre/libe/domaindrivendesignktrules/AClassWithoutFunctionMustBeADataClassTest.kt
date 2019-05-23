package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.RuleSet
import org.assertj.core.api.Assertions
import org.junit.Test

class AClassWithoutFunctionMustBeADataClassTest {

    @Test
    fun testViolation() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            """
            package some.packages

            class MyClass {
              private var myFieldOne: Int
              private var myFieldTwo: String
              private var myFieldThree: Boolean
            }

            """.trimIndent(),
            listOf(RuleSet("test", AClassWithoutFunctionMustBeADataClass()))
        ) { collector.add(it) }

        Assertions.assertThat(collector).containsExactly(
            LintError(
                line = 3,
                col = 1,
                ruleId = "test:a-class-without-function-must-be-a-data-class",
                detail = "This class some.packages.MyClass does not have any function. Should not it be a data class ? In any case,Domain Driven Design discourages the use of anemic classes (POJO or value objects)"
            )
        )
    }

    @Test
    fun testViolationWhileCheatingOnTheParentClass() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            """
            package some.packages

            class MyClass : Object {
              private var myFieldOne: Int
              private var myFieldTwo: String
              private var myFieldThree: Boolean
            }

            """.trimIndent(),
            listOf(RuleSet("test", AClassWithoutFunctionMustBeADataClass()))
        ) { collector.add(it) }

        Assertions.assertThat(collector).containsExactly(
            LintError(
                line = 3,
                col = 1,
                ruleId = "test:a-class-without-function-must-be-a-data-class",
                detail = "This class some.packages.MyClass does not have any function. Should not it be a data class ? In any case,Domain Driven Design discourages the use of anemic classes (POJO or value objects)"
            )
        )
    }
}