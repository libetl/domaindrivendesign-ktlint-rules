package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.rule.engine.api.Code
import com.pinterest.ktlint.rule.engine.api.KtLintRuleEngine
import com.pinterest.ktlint.rule.engine.api.LintError
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.RuleProvider
import com.winterbe.expekt.should
import org.junit.jupiter.api.Test
import org.toilelibre.libe.domaindrivendesignktrules.DomainDrivenDesignRuleSetProvider.Companion.rulesetName

class AllNonForeignDataClassesMembersMustBeImmutableTest {
    @Test
    fun testNoViolationWithForeignModel() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { AllNonForeignDataClassesMembersMustBeImmutable() }))
            .lint(
                Code.fromSnippet(
                    """
package some.packages

@Entity
@ForeignModel
data class Account(var id: ObjectId, var firstName: String, var lastName: String)

                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.be.empty
    }

    @Test
    fun testViolation() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { AllNonForeignDataClassesMembersMustBeImmutable() }))
            .lint(
                Code.fromSnippet(
                    """
package some.packages

@Entity
data class Account(var id: ObjectId, var firstName: String, var lastName: String)

                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.contain.all.the.elements(
            LintError(
                line = 4,
                col = 20,
                ruleId = RuleId("$rulesetName:no-non-foreign-data-class-member-mutable"),
                canBeAutoCorrected = false,
                detail = "This variable : some.packages.Account.id is mutable (should be immutable)",
            ),
            LintError(
                line = 4,
                col = 38,
                ruleId = RuleId("$rulesetName:no-non-foreign-data-class-member-mutable"),
                canBeAutoCorrected = false,
                detail = "This variable : some.packages.Account.firstName is mutable (should be immutable)",
            ),
            LintError(
                line = 4,
                col = 61,
                ruleId = RuleId("$rulesetName:no-non-foreign-data-class-member-mutable"),
                canBeAutoCorrected = false,
                detail = "This variable : some.packages.Account.lastName is mutable (should be immutable)",
            ),
        )
    }
}
