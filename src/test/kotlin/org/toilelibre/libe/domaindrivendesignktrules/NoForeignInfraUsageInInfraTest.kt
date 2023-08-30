package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.rule.engine.api.Code
import com.pinterest.ktlint.rule.engine.api.KtLintRuleEngine
import com.pinterest.ktlint.rule.engine.api.LintError
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.RuleProvider
import com.winterbe.expekt.should
import org.junit.jupiter.api.Test
import org.toilelibre.libe.domaindrivendesignktrules.DomainDrivenDesignRuleSetProvider.Companion.rulesetName

class NoForeignInfraUsageInInfraTest {

    @Test
    fun integrationTestInTest() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { NoForeignInfraUsageInInfra() }))
            .lint(
                Code.fromSnippet(
                    """
package com.egencia.service.infra.test1

import com.egencia.service.infra.test1.Test1
import com.egencia.service.infra.test2.Test2

@DomainService
class HelloIT
                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.be.empty
    }

    @Test
    fun testInTest() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { NoForeignInfraUsageInInfra() }))
            .lint(
                Code.fromSnippet(
                    """
package com.egencia.service.infra.test1

import com.egencia.service.infra.test1.Test1
import com.egencia.service.infra.test2.Test2

@DomainService
class MyTest
                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.be.empty
    }

    @Test
    fun testOutsideInfra() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { NoForeignInfraUsageInInfra() }))
            .lint(
                Code.fromSnippet(
                    """
package com.egencia.service.domain.ktlintrules

import com.egencia.service.infra.test1.Test1
import com.egencia.service.infra.test2.Test2

@DomainService
class Hello
                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.be.empty
    }

    @Test
    fun testNoViolationInInfra() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { NoForeignInfraUsageInInfra() }))
            .lint(
                Code.fromSnippet(
                    """
package com.egencia.service.infra.test1

import com.egencia.service.infra.test1.Test1
import com.egencia.service.infra.test1.Test11

@Gateway
class Hello
                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.be.empty
    }

    @Test
    fun testNoViolationForSpringConfig() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { NoForeignInfraUsageInInfra() }))
            .lint(
                Code.fromSnippet(
                    """
package com.egencia.service.infra.test1

import com.egencia.service.infra.test1.Test1
import com.egencia.service.infra.test2.Test2

@Configuration
class TestConfiguration
                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.be.empty
    }

    @Test
    fun testViolationInInfra() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { NoForeignInfraUsageInInfra() }))
            .lint(
                Code.fromSnippet(
                    """
package com.egencia.service.infra.test1

import com.egencia.service.infra.test1.Test1
import com.egencia.service.infra.test2.Test2

@Gateway
class Hello
                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.contain(
            LintError(
                line = 6,
                col = 1,
                ruleId = RuleId("$rulesetName:no-foreign-infra-usage-in-infra"),
                canBeAutoCorrected = false,
                detail = "This class : com.egencia.service.infra.test1.Hello is in infra package and uses at least " +
                    "one class from another infra package : [com.egencia.service.infra.test2.Test2]",
            ),
        )
    }
}
