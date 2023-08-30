package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.rule.engine.api.Code
import com.pinterest.ktlint.rule.engine.api.KtLintRuleEngine
import com.pinterest.ktlint.rule.engine.api.LintError
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.RuleProvider
import com.winterbe.expekt.should
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.toilelibre.libe.domaindrivendesignktrules.DomainDrivenDesignRuleSetProvider.Companion.rulesetName

class NoForeignModelInAnnotatedComponentContractTest {

    @BeforeEach
    fun clear() {
        NoForeignModelInAnnotatedComponentContract.clear()
    }

    @Test
    fun testViolationActionRoleFirst() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { NoForeignModelInAnnotatedComponentContract() }))
            .lint(
                Code.fromSnippet(
                    """
package org.toilelibre.libe.domaindrivendesignktrules

import kotlin.String
import org.toilelibre.libe.domaindrivendesignktrules.A

@Action
class B {
    fun testingTheViolation(a: A) {
    }
}

@ForeignModel
data class A
                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.contain(
            LintError(
                line = 12,
                col = 1,
                ruleId = RuleId("$rulesetName:no-foreign-model-in-annotated-component-contract"),
                canBeAutoCorrected = false,
                detail = "Foreign models have been found in some Action or DomainService or Gateway or Repository contract : {org.toilelibre.libe.domaindrivendesignktrules.A=[org.toilelibre.libe.domaindrivendesignktrules.B.testingTheViolation]}",
            ),
        )
    }

    @Test
    fun testViolation() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { NoForeignModelInAnnotatedComponentContract() }))
            .lint(
                Code.fromSnippet(
                    """
package org.toilelibre.libe.domaindrivendesignktrules

import kotlin.String
import org.toilelibre.libe.domaindrivendesignktrules.A

@ForeignModel
data class A

@Action
class B {
    fun testingTheViolation(a: A) {
    }
}
                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.contain(
            LintError(
                line = 9,
                col = 1,
                ruleId = RuleId("$rulesetName:no-foreign-model-in-annotated-component-contract"),
                canBeAutoCorrected = false,
                detail = "Foreign models have been found in some Action or DomainService or Gateway or Repository contract : {testingTheViolation=[A]}",
            ),
        )
    }

    @Test
    fun testViolationInInfra() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { NoForeignModelInAnnotatedComponentContract() }))
            .lint(
                Code.fromSnippet(
                    """
package org.toilelibre.libe.domaindrivendesignktrules

import kotlin.String
import org.toilelibre.libe.domaindrivendesignktrules.A

@ForeignModel
data class A

@Gateway
class B {
    fun testingTheViolation(a: A) {
    }
}
                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.contain(
            LintError(
                line = 9,
                col = 1,
                ruleId = RuleId("$rulesetName:no-foreign-model-in-annotated-component-contract"),
                canBeAutoCorrected = false,
                detail = "Foreign models have been found in some Action or DomainService or Gateway or Repository contract : {testingTheViolation=[A]}",
            ),
        )
    }

    @Test
    fun testPrivateMethodInInfraShouldBeTolerated() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { NoForeignModelInAnnotatedComponentContract() }))
            .lint(
                Code.fromSnippet(
                    """
package org.toilelibre.libe.domaindrivendesignktrules

import kotlin.String
import org.toilelibre.libe.domaindrivendesignktrules.A

@ForeignModel
data class A

@Gateway
class B {
    private fun testingTheViolation(a: A) {
    }
}
                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.be.empty
    }

    @Test
    fun testViolationWithReturnType() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { NoForeignModelInAnnotatedComponentContract() }))
            .lint(
                Code.fromSnippet(
                    """
package org.toilelibre.libe.domaindrivendesignktrules

import org.toilelibre.libe.domaindrivendesignktrules.A

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
                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.contain(
            LintError(
                line = 11,
                col = 1,
                ruleId = RuleId("$rulesetName:no-foreign-model-in-annotated-component-contract"),
                canBeAutoCorrected = false,
                detail = "Foreign models have been found in some Action or DomainService or Gateway or Repository contract : {testingTheViolation=[A]}",
            ),
        )
    }

    @Test
    fun testViolationInDomainService() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { NoForeignModelInAnnotatedComponentContract() }))
            .lint(
                Code.fromSnippet(
                    """
package org.toilelibre.libe.domaindrivendesignktrules

import kotlin.String
import org.toilelibre.libe.domaindrivendesignktrules.A

@ForeignModel
data class A

@DomainService
class B {
    fun testingTheViolation(a: A) {
    }
}
                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.contain(
            LintError(
                line = 9,
                col = 1,
                ruleId = RuleId("$rulesetName:no-foreign-model-in-annotated-component-contract"),
                canBeAutoCorrected = false,
                detail = "Foreign models have been found in some Action or DomainService or Gateway or Repository contract : {testingTheViolation=[A]}",
            ),
        )
    }

    @Test
    fun shouldNotReportAnything() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { NoForeignModelInAnnotatedComponentContract() }))
            .lint(
                Code.fromSnippet(
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
                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.be.empty
    }
}
