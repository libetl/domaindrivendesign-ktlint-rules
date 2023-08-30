package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.rule.engine.api.Code
import com.pinterest.ktlint.rule.engine.api.KtLintRuleEngine
import com.pinterest.ktlint.rule.engine.api.LintError
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.RuleProvider
import com.winterbe.expekt.should
import org.junit.jupiter.api.Test
import org.toilelibre.libe.domaindrivendesignktrules.DomainDrivenDesignRuleSetProvider.Companion.rulesetName

class GatewayOrRepositoryMustHaveOnlyOneTemplateVariableTest {

    @Test
    fun testViolation() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { GatewayOrRepositoryMustHaveOnlyOneTemplateVariable() }))
            .lint(
                Code.fromSnippet(
                    """
package some.packages

import org.springframework.web.client.RestTemplate

@Gateway
class MyGateway {

  private val restTemplate1: RestTemplate
  private val restTemplate2: RestTemplate

}
                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.contain(
            LintError(
                line = 5,
                col = 1,
                ruleId = RuleId("$rulesetName:gateway-or-repository-must-have-only-one-template-variable"),
                canBeAutoCorrected = false,
                detail = "This infra role defines more than one *Template class. Only one is allowed. (found : {org.springframework.web.client=2})",
            ),
        )
    }

    @Test
    fun testMultipleKindsViolation() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { GatewayOrRepositoryMustHaveOnlyOneTemplateVariable() }))
            .lint(
                Code.fromSnippet(
                    """
package some.packages

import org.springframework.web.client.RestTemplate
import org.springframework.data.mongodb.core.MongoTemplate

@Gateway
class MyGateway {

  private val restTemplate: RestTemplate
  private val mongoTemplate: MongoTemplate

}
                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.contain(
            LintError(
                line = 6,
                col = 1,
                ruleId = RuleId("$rulesetName:gateway-or-repository-must-have-only-one-template-variable"),
                canBeAutoCorrected = false,
                detail = "This infra role defines more than one *Template class. Only one is allowed. (found : {org.springframework.web.client=1, org.springframework.data.mongodb.core=1})",
            ),
        )
    }

    @Test
    fun testRepositoryViolation() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { GatewayOrRepositoryMustHaveOnlyOneTemplateVariable() }))
            .lint(
                Code.fromSnippet(
                    """
package some.packages

import org.springframework.web.client.RestTemplate
import org.springframework.data.mongodb.core.MongoTemplate

@Repository
class MyRepository {

  private val restTemplate: RestTemplate
  private val mongoTemplate: MongoTemplate

}
                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.contain(
            LintError(
                line = 6,
                col = 1,
                ruleId = RuleId("$rulesetName:gateway-or-repository-must-have-only-one-template-variable"),
                canBeAutoCorrected = false,
                detail = "This infra role defines more than one *Template class. Only one is allowed. (found : {org.springframework.web.client=1, org.springframework.data.mongodb.core=1})",
            ),
        )
    }

    @Test
    fun testNoViolation() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { GatewayOrRepositoryMustHaveOnlyOneTemplateVariable() }))
            .lint(
                Code.fromSnippet(
                    """
package some.packages

import org.springframework.web.client.RestTemplate
import org.springframework.data.mongodb.core.MongoTemplate

@Repository
class MyRepository {

  private val mongoTemplate: MongoTemplate

}
                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.be.empty
    }

    @Test
    fun testRepositoryNoViolation() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { GatewayOrRepositoryMustHaveOnlyOneTemplateVariable() }))
            .lint(
                Code.fromSnippet(
                    """
package some.packages

import org.springframework.web.client.RestTemplate
import org.springframework.data.mongodb.core.MongoTemplate

@Repository
class MyRepository {

  private val mongoTemplate: MongoTemplate

}
                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.be.empty
    }
}
