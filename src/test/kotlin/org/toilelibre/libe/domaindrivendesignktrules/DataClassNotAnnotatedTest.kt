package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.RuleSet
import com.winterbe.expekt.should
import org.junit.jupiter.api.Test

class DataClassNotAnnotatedTest {

    @Test
    fun testViolation() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            KtLint.Params(
                text =
            """
package some.packages

@ForeignModel
data class A(val i: Int)

data class B(val j: Int)

@ForeignModel
data class C(val k: Int)

data class D(val l: Int)

        """.trimIndent(), ruleSets = listOf(RuleSet("test", DataClassNotAnnotated())),
                cb = { e, _ -> collector.add(e) })
        )

        collector.should.contain.all.the.elements(
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
            KtLint.Params(
                text =
            """
package some.packages

@ForeignModel
data class A(val i: Int)

@ValueType
data class B(val j: Int)

@ForeignModel
data class C(val k: Int)
        """.trimIndent(), ruleSets = listOf(RuleSet("test", NoForeignModelInAnnotatedComponentContract())),
                cb = { e, _ -> collector.add(e) })
        )

        collector.should.be.empty
    }
}
