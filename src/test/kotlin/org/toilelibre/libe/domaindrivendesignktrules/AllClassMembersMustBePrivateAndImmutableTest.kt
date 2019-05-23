package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.RuleSet
import org.assertj.core.api.Assertions
import org.junit.Test

class AllClassMembersMustBePrivateAndImmutableTest {

    @Test
    fun testViolation() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            """
package some.packages

@Repository
class MyRepository(var dataSource: DataSource, private var jdbcUrl: String, val password: String){

  fun find(id: ObjectId){ // this should be allowed
    var ref = id.toString() // this should be allowed
    return dataSource.findOne(ref)
  }

}

        """.trimIndent(), listOf(RuleSet("test", AllClassMembersMustBePrivateAndImmutable()))
        ) { collector.add(it) }

        Assertions.assertThat(collector).containsExactly(
            LintError(
                line = 4, col = 20,
                ruleId = "test:no-class-member-public-or-mutable",
                detail = "This variable : some.packages.MyRepository.dataSource is mutable and is not private (and should be not mutable and private)"
            ),
            LintError(
                line = 4, col = 48,
                ruleId = "test:no-class-member-public-or-mutable",
                detail = "This variable : some.packages.MyRepository.jdbcUrl is mutable and is private (and should be not mutable and private)"
            ),
            LintError(
                line = 4, col = 77,
                ruleId = "test:no-class-member-public-or-mutable",
                detail = "This variable : some.packages.MyRepository.password is not mutable and is not private (and should be not mutable and private)"
            )
        )
    }

    @Test
    fun testNoViolationForDataClasses() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            """
package some.packages

@Entity
@ForeignModel
data class Account(val id: ObjectId, val firstName: String, val lastName: String)

        """.trimIndent(), listOf(RuleSet("test", AllClassMembersMustBePrivateAndImmutable()))
        ) { collector.add(it) }

        Assertions.assertThat(collector).isEmpty()
    }

    @Test
    fun testNoViolationForNonVarAndNonValParameters() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            """
package some.packages

class Tester(id: ObjectId) {
  fun isValid(){
    return true
  }
}

        """.trimIndent(), listOf(RuleSet("test", AllClassMembersMustBePrivateAndImmutable()))
        ) { collector.add(it) }

        Assertions.assertThat(collector).isEmpty()
    }
}