package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.RuleSet
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DataClassNotAnnotatedTest {

    @Test
    fun testViolation() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            """
            package some.packages

            @ForeignModel
            data class A(val i: Int)

            data class B(val j: Int)

            @ForeignModel
            data class C(val k: Int)

            data class D(val l: Int)

            """.trimIndent(),
            listOf(RuleSet("test", DataClassNotAnnotated()))
        ) { collector.add(it) }

        assertThat(collector).containsExactly(
            LintError(
                line = 6,
                col = 1,
                ruleId = "test:data-class-not-annotated",
                detail = "This data class is not annotated with @ForeignModel, @ValueType, @Entity or @Aggregate : some.packages.B"
            ),
            LintError(
                line = 11,
                col = 1,
                ruleId = "test:data-class-not-annotated",
                detail = "This data class is not annotated with @ForeignModel, @ValueType, @Entity or @Aggregate : some.packages.D"
            )
        )
    }

    @Test
    fun testNoViolation() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            """
            package some.packages

            @ForeignModel
            data class A(val i: Int)

            @ValueType
            data class B(val j: Int)

            @ForeignModel
            data class C(val k: Int)
            """.trimIndent(),
            listOf(RuleSet("test", NoForeignModelInAnnotatedComponentContract()))
        ) { collector.add(it) }

        assertThat(collector).isEmpty()
    }
}