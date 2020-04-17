package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.RuleSet
import com.winterbe.expekt.should
import org.junit.jupiter.api.Test

class AllNonForeignDataClassesMembersMustBeImmutableTest {
    @Test
    fun testNoViolationWithForeignModel() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            KtLint.Params(
                text =
            """
package some.packages

@Entity
@ForeignModel
data class Account(var id: ObjectId, var firstName: String, var lastName: String)

        """.trimIndent(), ruleSets = listOf(RuleSet("test", AllNonForeignDataClassesMembersMustBeImmutable())),
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

@Entity
data class Account(var id: ObjectId, var firstName: String, var lastName: String)

        """.trimIndent(), ruleSets = listOf(RuleSet("test", AllNonForeignDataClassesMembersMustBeImmutable())),
                cb = { e, _ -> collector.add(e) })
        )

        collector.should.contain.all.the.elements(
            LintError(
                line = 4, col = 20,
                ruleId = "test:no-non-foreign-data-class-member-mutable",
                detail = "This variable : some.packages.Account.id is mutable (should be immutable)"
            ),
            LintError(
                line = 4, col = 38,
                ruleId = "test:no-non-foreign-data-class-member-mutable",
                detail = "This variable : some.packages.Account.firstName is mutable (should be immutable)"
            ),
            LintError(
                line = 4, col = 61,
                ruleId = "test:no-non-foreign-data-class-member-mutable",
                detail = "This variable : some.packages.Account.lastName is mutable (should be immutable)"
            )
        )
    }
}
