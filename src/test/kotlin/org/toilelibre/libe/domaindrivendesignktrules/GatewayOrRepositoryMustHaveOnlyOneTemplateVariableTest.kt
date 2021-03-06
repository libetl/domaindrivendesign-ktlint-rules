package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.RuleSet
import com.winterbe.expekt.should
import org.junit.jupiter.api.Test

class GatewayOrRepositoryMustHaveOnlyOneTemplateVariableTest {

    @Test
    fun testViolation() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            KtLint.Params(
                text =
            """
package some.packages

import org.springframework.web.client.RestTemplate

@Gateway
class MyGateway {

  private val restTemplate1: RestTemplate
  private val restTemplate2: RestTemplate

}
        """.trimIndent(), ruleSets = listOf(RuleSet("test", GatewayOrRepositoryMustHaveOnlyOneTemplateVariable())),
                cb = { e, _ -> collector.add(e) })
        )

        collector.should.contain(
            LintError(
                line = 5,
                col = 1,
                ruleId = "test:gateway-or-repository-must-have-only-one-template-variable",
                detail = "This infra role defines more than one *Template class. Only one is allowed. (found : {org.springframework.web.client=2})"
            )
        )
    }

    @Test
    fun testMultipleKindsViolation() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            KtLint.Params(
                text =
            """
package some.packages

import org.springframework.web.client.RestTemplate
import org.springframework.data.mongodb.core.MongoTemplate

@Gateway
class MyGateway {

  private val restTemplate: RestTemplate
  private val mongoTemplate: MongoTemplate

}
        """.trimIndent(), ruleSets = listOf(RuleSet("test", GatewayOrRepositoryMustHaveOnlyOneTemplateVariable())),
                cb = { e, _ -> collector.add(e) })
        )

        collector.should.contain(
            LintError(
                line = 6,
                col = 1,
                ruleId = "test:gateway-or-repository-must-have-only-one-template-variable",
                detail = "This infra role defines more than one *Template class. Only one is allowed. (found : {org.springframework.web.client=1, org.springframework.data.mongodb.core=1})"
            )
        )
    }

    @Test
    fun testRepositoryViolation() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            KtLint.Params(
                text =
            """
package some.packages

import org.springframework.web.client.RestTemplate
import org.springframework.data.mongodb.core.MongoTemplate

@Repository
class MyRepository {

  private val restTemplate: RestTemplate
  private val mongoTemplate: MongoTemplate

}
        """.trimIndent(), ruleSets = listOf(RuleSet("test", GatewayOrRepositoryMustHaveOnlyOneTemplateVariable())),
                cb = { e, _ -> collector.add(e) })
        )

        collector.should.contain(
            LintError(
                line = 6,
                col = 1,
                ruleId = "test:gateway-or-repository-must-have-only-one-template-variable",
                detail = "This infra role defines more than one *Template class. Only one is allowed. (found : {org.springframework.web.client=1, org.springframework.data.mongodb.core=1})"
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

import org.springframework.web.client.RestTemplate
import org.springframework.data.mongodb.core.MongoTemplate

@Repository
class MyRepository {

  private val mongoTemplate: MongoTemplate

}
        """.trimIndent(), ruleSets = listOf(RuleSet("test", GatewayOrRepositoryMustHaveOnlyOneTemplateVariable())),
                cb = { e, _ -> collector.add(e) })
        )

        collector.should.be.empty
    }

    @Test
    fun testRepositoryNoViolation() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            KtLint.Params(
                text =
            """
package some.packages

import org.springframework.web.client.RestTemplate
import org.springframework.data.mongodb.core.MongoTemplate

@Repository
class MyRepository {

  private val mongoTemplate: MongoTemplate

}
        """.trimIndent(), ruleSets = listOf(RuleSet("test", GatewayOrRepositoryMustHaveOnlyOneTemplateVariable())),
                cb = { e, _ -> collector.add(e) })
        )

        collector.should.be.empty
    }
}
