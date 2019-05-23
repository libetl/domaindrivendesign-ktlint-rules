package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.RuleSet
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class NoForeignModelInAnnotatedComponentContractTest {

    @Before
    fun clear() {
        NoForeignModelInAnnotatedComponentContract.clear()
    }

    @Test
    fun testViolationActionRoleFirst() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            """
package org.toilelibre.libe.ktlintrules

import kotlin.String
import org.toilelibre.libe.ktlintrules.A

@Action
class B {
    fun testingTheViolation(a: A) {
    }
}

@ForeignModel
data class A
        """.trimIndent(), listOf(RuleSet("test", NoForeignModelInAnnotatedComponentContract()))
        ) { collector.add(it) }

        assertThat(collector).containsExactly(
            LintError(
                line = 12, col = 1, ruleId = "test:no-foreign-model-in-annotated-component-contract",
                detail = "Foreign models have been found in some Action or DomainService or Gateway or Repository contract : {org.toilelibre.libe.ktlintrules.A=[org.toilelibre.libe.ktlintrules.B.testingTheViolation]}"
            )
        )
    }

    @Test
    fun testViolation() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            """
package org.toilelibre.libe.ktlintrules

import kotlin.String
import org.toilelibre.libe.ktlintrules.A

@ForeignModel
data class A

@Action
class B {
    fun testingTheViolation(a: A) {
    }
}
        """.trimIndent(), listOf(RuleSet("test", NoForeignModelInAnnotatedComponentContract()))
        ) { collector.add(it) }

        assertThat(collector).containsExactly(
            LintError(
                line = 9, col = 1, ruleId = "test:no-foreign-model-in-annotated-component-contract",
                detail = "Foreign models have been found in some Action or DomainService or Gateway or Repository contract : {testingTheViolation=[A]}"
            )
        )
    }

    @Test
    fun testViolationWithReturnType() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            """
package org.toilelibre.libe.ktlintrules

import org.toilelibre.libe.ktlintrules.A

@ForeignModel
data class A(val e: Int)

@ValueType
data class C(val f: Int)

@Action
class B {
    fun testingTheViolation(): A {
      return A()
    }
}
        """.trimIndent(), listOf(RuleSet("test", NoForeignModelInAnnotatedComponentContract()))
        ) { collector.add(it) }

        assertThat(collector).containsExactly(
            LintError(
                line = 11, col = 1, ruleId = "test:no-foreign-model-in-annotated-component-contract",
                detail = "Foreign models have been found in some Action or DomainService or Gateway or Repository contract : {testingTheViolation=[A]}"
            )
        )
    }

    @Test
    fun testViolationInDomainService() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            """
package org.toilelibre.libe.ktlintrules

import kotlin.String
import org.toilelibre.libe.ktlintrules.A

@ForeignModel
data class A

@DomainService
class B {
    fun testingTheViolation(a: A) {
    }
}
        """.trimIndent(), listOf(RuleSet("test", NoForeignModelInAnnotatedComponentContract()))
        ) { collector.add(it) }

        assertThat(collector).containsExactly(
            LintError(
                line = 9, col = 1, ruleId = "test:no-foreign-model-in-annotated-component-contract",
                detail = "Foreign models have been found in some Action or DomainService or Gateway or Repository contract : {testingTheViolation=[A]}"
            )
        )
    }

    @Test
    fun shouldNotReportAnything() {
        val collector = mutableListOf<LintError>()
        KtLint.lint(
            """
package some.packages.actions

import some.packages.domain.transverse.DomainDrivenDesignAnnotations.Action
import some.packages.infra.database.Storage
import org.bson.types.ObjectId

@Entity
data class Instance(val id: Int)

@Action
class GetInstanceByRowId(private val storage: Storage) {

    infix fun retrievePaymentBy(`an id`: ObjectId): Instance? = storage rowMatching `an id`
}
        """.trimIndent(), listOf(RuleSet("test", NoForeignModelInAnnotatedComponentContract()))
        ) { collector.add(it) }

        assertThat(collector).isEmpty()
    }
}