package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.RuleSet
import com.winterbe.expekt.should
import org.junit.jupiter.api.Test

class NoTemplateUseInActionOrDomainServiceTest {

    @Test
    fun testViolation() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            KtLint.Params(
                text =
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
        """.trimIndent(), ruleSets = listOf(RuleSet("test", NoTemplateUseInActionOrDomainService())),
                cb = { e, _ -> collector.add(e) })
        )

        collector.should.contain(
            LintError(
                line = 5, col = 1,
                ruleId = "test:no-template-use-in-action-or-domain-service",
                detail = "This|These forbidden package(s) is|are used in an Action or in a DomainService : [org.springframework.web.client, org.springframework.data.mongodb.core]"
            )
        )
    }

    @Test
    fun testViolationInDomainService() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            KtLint.Params(
                text =
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
        """.trimIndent(), ruleSets = listOf(RuleSet("test", NoTemplateUseInActionOrDomainService())),
                cb = { e, _ -> collector.add(e) })
        )

        collector.should.contain(
            LintError(
                line = 5, col = 1,
                ruleId = "test:no-template-use-in-action-or-domain-service",
                detail = "This|These forbidden package(s) is|are used in an Action or in a DomainService : [org.springframework.web.client, org.springframework.data.mongodb.core]"
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

@DomainService
class MyAction {

  private val userProfileReader: UserProfileReader

  fun someMethod(val id: Int) {
    val userProfile = userProfileReader.read(id)
  }
}
        """.trimIndent(), ruleSets = listOf(RuleSet("test", NoTemplateUseInActionOrDomainService())),
                cb = { e, _ -> collector.add(e) })
        )

        collector.should.be.empty
    }

    @Test
    fun testNoViolationInGateway() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            KtLint.Params(
                text =
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
        """.trimIndent(), ruleSets = listOf(RuleSet("test", NoTemplateUseInActionOrDomainService())),
                cb = { e, _ -> collector.add(e) })
        )

        collector.should.be.empty
    }
}
