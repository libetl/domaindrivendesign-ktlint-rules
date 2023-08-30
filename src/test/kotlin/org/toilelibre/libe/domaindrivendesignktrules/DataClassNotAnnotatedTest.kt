package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.rule.engine.api.Code
import com.pinterest.ktlint.rule.engine.api.KtLintRuleEngine
import com.pinterest.ktlint.rule.engine.api.LintError
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.RuleProvider
import com.winterbe.expekt.should
import org.junit.jupiter.api.Test
import org.toilelibre.libe.domaindrivendesignktrules.DomainDrivenDesignRuleSetProvider.Companion.rulesetName

class DataClassNotAnnotatedTest {

    @Test
    fun testViolation() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { DataClassNotAnnotated() }))
            .lint(
                Code.fromSnippet(
                    """
package some.packages

@ForeignModel
data class A(val i: Int)

data class B(val j: Int)

@ForeignModel
data class C(val k: Int)

data class D(val l: Int)

                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.contain.all.the.elements(
            LintError(
                line = 6,
                col = 1,
                ruleId = RuleId("$rulesetName:data-class-not-annotated"),
                canBeAutoCorrected = false,
                detail = "This data class is not annotated with @ForeignModel, @ValueType, @Entity or @Aggregate : some.packages.B",
            ),
            LintError(
                line = 11,
                col = 1,
                ruleId = RuleId("$rulesetName:data-class-not-annotated"),
                canBeAutoCorrected = false,
                detail = "This data class is not annotated with @ForeignModel, @ValueType, @Entity or @Aggregate : some.packages.D",
            ),
        )
    }

    @Test
    fun testNoViolation() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { DataClassNotAnnotated() }))
            .lint(
                Code.fromSnippet(
                    """
package some.packages

@ForeignModel
data class A(val i: Int)

@ValueType
data class B(val j: Int)

@ForeignModel
data class C(val k: Int)
                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.be.empty
    }
}
