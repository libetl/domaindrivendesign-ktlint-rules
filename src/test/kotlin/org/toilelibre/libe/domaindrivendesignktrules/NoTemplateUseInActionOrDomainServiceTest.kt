package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.rule.engine.api.Code
import com.pinterest.ktlint.rule.engine.api.KtLintRuleEngine
import com.pinterest.ktlint.rule.engine.api.LintError
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.RuleProvider
import com.winterbe.expekt.should
import org.junit.jupiter.api.Test
import org.toilelibre.libe.domaindrivendesignktrules.DomainDrivenDesignRuleSetProvider.Companion.rulesetName

class NoTemplateUseInActionOrDomainServiceTest {

    @Test
    fun testViolation() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { NoTemplateUseInActionOrDomainService() }))
            .lint(
                Code.fromSnippet(
                    """
package some.packages

import org.springframework.web.client.RestTemplate

@Action
class MyAction {

  fun someMethod() {
    val template = RestTemplate()
    val template2 = org.springframework.data.mongodb.core.MongoTemplate()
  }
}
                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.contain(
            LintError(
                line = 5,
                col = 1,
                ruleId = RuleId("$rulesetName:no-template-use-in-action-or-domain-service"),
                canBeAutoCorrected = false,
                detail = "This|These forbidden package(s) is|are used in an Action or in a DomainService : [org.springframework.web.client, org.springframework.data.mongodb.core]",
            ),
        )
    }

    @Test
    fun testViolationInDomainService() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { NoTemplateUseInActionOrDomainService() }))
            .lint(
                Code.fromSnippet(
                    """
package some.packages

import org.springframework.web.client.RestTemplate

@DomainService
class MyDomainService {

  fun someMethod() {
    val template = RestTemplate()
    val template2 = org.springframework.data.mongodb.core.MongoTemplate()
  }
}
                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.contain(
            LintError(
                line = 5,
                col = 1,
                ruleId = RuleId("$rulesetName:no-template-use-in-action-or-domain-service"),
                canBeAutoCorrected = false,
                detail = "This|These forbidden package(s) is|are used in an Action or in a DomainService : [org.springframework.web.client, org.springframework.data.mongodb.core]",
            ),
        )
    }

    @Test
    fun testNoViolation() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { NoTemplateUseInActionOrDomainService() }))
            .lint(
                Code.fromSnippet(
                    """
package some.packages

@DomainService
class MyAction {

  private val userProfileReader: UserProfileReader

  fun someMethod(val id: Int) {
    val userProfile = userProfileReader.read(id)
  }
}
                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.be.empty
    }

    @Test
    fun testNoViolationInGateway() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { NoTemplateUseInActionOrDomainService() }))
            .lint(
                Code.fromSnippet(
                    """
package some.packages

import org.springframework.web.client.RestTemplate

@Gateway
class MyGateway {

  fun someMethod() {
    val template = RestTemplate()
    val template2 = org.springframework.data.mongodb.core.MongoTemplate()
  }
}
                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.be.empty
    }
}
