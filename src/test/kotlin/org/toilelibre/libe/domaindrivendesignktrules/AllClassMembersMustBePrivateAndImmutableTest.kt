package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.core.KtLint
import com.winterbe.expekt.should
import org.junit.jupiter.api.Test

class AllClassMembersMustBePrivateAndImmutableTest {

    @Test
    fun testViolation() {
        val collector = mutableListOf<com.pinterest.ktlint.core.LintError>()
        KtLint.lint(
            KtLint.ExperimentalParams(
                text =
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
                ruleProviders = setOf(RuleProvider { AllClassMembersMustBePrivateAndImmutable() }),
                cb = { e, _ -> collector.add(e) }
            )
        )

        collector.should.contain.all.the.elements(
            LintError(
                line = 4,
                col = 20,
                ruleId = "no-class-member-public-or-mutable",
                detail = "This variable : some.packages.MyRepository.dataSource is mutable and is not private (and should be not mutable and private)"
            ),
            LintError(
                line = 4,
                col = 48,
                ruleId = "no-class-member-public-or-mutable",
                detail = "This variable : some.packages.MyRepository.jdbcUrl is mutable and is private (and should be not mutable and private)"
            ),
            LintError(
                line = 4,
                col = 77,
                ruleId = "no-class-member-public-or-mutable",
                detail = "This variable : some.packages.MyRepository.password is not mutable and is not private (and should be not mutable and private)"
            )
        )
    }

    @Test
    fun testNoViolationForDataClasses() {
        val collector = mutableListOf<com.pinterest.ktlint.core.LintError>()
        KtLint.lint(
            KtLint.ExperimentalParams(
                text =
                """
package some.packages

@Entity
@ForeignModel
data class Account(val id: ObjectId, val firstName: String, val lastName: String)

                """.trimIndent(),
                ruleProviders = setOf(RuleProvider { AllClassMembersMustBePrivateAndImmutable() }),
                cb = { e, _ -> collector.add(e) }
            )
        )

        collector.should.be.empty
    }

    @Test
    fun testNoViolationForNonVarAndNonValParameters() {
        val collector = mutableListOf<com.pinterest.ktlint.core.LintError>()
        KtLint.lint(
            KtLint.ExperimentalParams(
                text =
                """
package some.packages

class Tester(id: ObjectId) {
  fun isValid(){
    return true
  }
}

                """.trimIndent(),
                ruleProviders = setOf(RuleProvider { AllClassMembersMustBePrivateAndImmutable() }),
                cb = { e, _ -> collector.add(e) }
            )
        )

        collector.should.be.empty
    }

    @Test
    fun testNoViolationForOverridenValueParameters() {
        val collector = mutableListOf<com.pinterest.ktlint.core.LintError>()
        KtLint.lint(
            KtLint.ExperimentalParams(
                text =
                """
package some.packages

class Tester(override var id: ObjectId) {
  fun isValid(){
    return true
  }
}

                """.trimIndent(),
                ruleProviders = setOf(RuleProvider { AllClassMembersMustBePrivateAndImmutable() }),
                cb = { e, _ -> collector.add(e) }
            )
        )

        collector.should.be.empty
    }
}
