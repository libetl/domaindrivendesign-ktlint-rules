package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.rule.engine.api.Code
import com.pinterest.ktlint.rule.engine.api.KtLintRuleEngine
import com.pinterest.ktlint.rule.engine.api.LintError
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.RuleProvider
import com.winterbe.expekt.should
import org.junit.jupiter.api.Test
import org.toilelibre.libe.domaindrivendesignktrules.DomainDrivenDesignRuleSetProvider.Companion.rulesetName

class AllClassMembersMustBePrivateAndImmutableTest {

    @Test
    fun testViolation() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { AllClassMembersMustBePrivateAndImmutable() }))
            .lint(
                Code.fromSnippet(
                    """
package some.packages

@Repository
class MyRepository(var dataSource: DataSource, private var jdbcUrl: String, val password: String){

  fun find(id: ObjectId){ // this should be allowed
    var ref = id.toString() // this should be allowed
    return dataSource.findOne(ref)
  }

}

                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.contain.all.the.elements(
            LintError(
                line = 4,
                col = 20,
                ruleId = RuleId("$rulesetName:no-class-member-public-or-mutable"),
                canBeAutoCorrected = false,
                detail = "This variable : some.packages.MyRepository.dataSource is mutable and is not private (and should be not mutable and private)",
            ),
            LintError(
                line = 4,
                col = 48,
                ruleId = RuleId("$rulesetName:no-class-member-public-or-mutable"),
                canBeAutoCorrected = false,
                detail = "This variable : some.packages.MyRepository.jdbcUrl is mutable and is private (and should be not mutable and private)",
            ),
            LintError(
                line = 4,
                col = 77,
                ruleId = RuleId("$rulesetName:no-class-member-public-or-mutable"),
                canBeAutoCorrected = false,
                detail = "This variable : some.packages.MyRepository.password is not mutable and is not private (and should be not mutable and private)",
            ),
        )
    }

    @Test
    fun testNoViolationForDataClasses() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { AllClassMembersMustBePrivateAndImmutable() }))
            .lint(
                Code.fromSnippet(
                    """
package some.packages

@Entity
@ForeignModel
data class Account(val id: ObjectId, val firstName: String, val lastName: String)

                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.be.empty
    }

    @Test
    fun testNoViolationForNonVarAndNonValParameters() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { AllClassMembersMustBePrivateAndImmutable() }))
            .lint(
                Code.fromSnippet(
                    """
package some.packages

class Tester(id: ObjectId) {
  fun isValid(){
    return true
  }
}

                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.be.empty
    }

    @Test
    fun testNoViolationForOverridenValueParameters() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { AllClassMembersMustBePrivateAndImmutable() }))
            .lint(
                Code.fromSnippet(
                    """
package some.packages

class Tester(override var id: ObjectId) {
  fun isValid(){
    return true
  }
}

                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )
        KtLintRuleEngine(setOf(RuleProvider { AllClassMembersMustBePrivateAndImmutable() }))
            .lint(
                Code.fromSnippet(
                    """
package some.packages

class TesterWithCoroutineContext(@Qualifier override var id: ObjectId) {
  fun isValid(){
    return true
  }
}

                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.be.empty
    }
}
