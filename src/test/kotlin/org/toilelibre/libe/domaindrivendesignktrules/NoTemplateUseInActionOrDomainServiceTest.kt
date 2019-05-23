package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.RuleSet
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class NoTemplateUseInActionOrDomainServiceTest {

    @Test
    fun testViolation() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
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
        """.trimIndent(), listOf(RuleSet("test", NoTemplateUseInActionOrDomainService()))
        ) { collector.add(it) }

        assertThat(collector).containsExactly(
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
        """.trimIndent(), listOf(RuleSet("test", NoTemplateUseInActionOrDomainService()))
        ) { collector.add(it) }

        assertThat(collector).containsExactly(
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
            """
package some.packages

@DomainService
class MyAction {

  private val userProfileReader: UserProfileReader

  fun someMethod(val id: Int) {
    val userProfile = userProfileReader.read(id)
  }
}
        """.trimIndent(), listOf(RuleSet("test", NoTemplateUseInActionOrDomainService()))
        ) { collector.add(it) }

        assertThat(collector).isEmpty()
    }

    @Test
    fun testNoViolationInGateway() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
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
        """.trimIndent(), listOf(RuleSet("test", NoTemplateUseInActionOrDomainService()))
        ) { collector.add(it) }

        assertThat(collector).isEmpty()
    }
}