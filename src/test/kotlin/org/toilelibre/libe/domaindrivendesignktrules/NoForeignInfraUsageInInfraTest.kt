package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.core.KtLint
import com.winterbe.expekt.should
import org.junit.jupiter.api.Test

class NoForeignInfraUsageInInfraTest {

    @Test
    fun integrationTestInTest() {
        val collector = mutableListOf<com.pinterest.ktlint.core.LintError>()
        KtLint.lint(
            KtLint.ExperimentalParams(
                text =
                """
package com.egencia.service.infra.test1

import com.egencia.service.infra.test1.Test1
import com.egencia.service.infra.test2.Test2

@DomainService
class HelloIT
                """.trimIndent(),
                ruleProviders = setOf(RuleProvider { NoForeignInfraUsageInInfra() }),
                cb = { e, _ -> collector.add(e) }
            )
        )

        collector.should.be.empty
    }

    @Test
    fun testInTest() {
        val collector = mutableListOf<com.pinterest.ktlint.core.LintError>()
        KtLint.lint(
            KtLint.ExperimentalParams(
                text =
                """
package com.egencia.service.infra.test1

import com.egencia.service.infra.test1.Test1
import com.egencia.service.infra.test2.Test2

@DomainService
class MyTest
                """.trimIndent(),
                ruleProviders = setOf(RuleProvider { NoForeignInfraUsageInInfra() }),
                cb = { e, _ -> collector.add(e) }
            )
        )

        collector.should.be.empty
    }

    @Test
    fun testOutsideInfra() {
        val collector = mutableListOf<com.pinterest.ktlint.core.LintError>()
        KtLint.lint(
            KtLint.ExperimentalParams(
                text =
                """
package com.egencia.service.domain.ktlintrules

import com.egencia.service.infra.test1.Test1
import com.egencia.service.infra.test2.Test2

@DomainService
class Hello
                """.trimIndent(),
                ruleProviders = setOf(RuleProvider { NoForeignInfraUsageInInfra() }),
                cb = { e, _ -> collector.add(e) }
            )
        )

        collector.should.be.empty
    }

    @Test
    fun testNoViolationInInfra() {
        val collector = mutableListOf<com.pinterest.ktlint.core.LintError>()
        KtLint.lint(
            KtLint.ExperimentalParams(
                text =
                """
package com.egencia.service.infra.test1

import com.egencia.service.infra.test1.Test1
import com.egencia.service.infra.test1.Test11

@Gateway
class Hello
                """.trimIndent(),
                ruleProviders = setOf(RuleProvider { NoForeignInfraUsageInInfra() }),
                cb = { e, _ -> collector.add(e) }
            )
        )

        collector.should.be.empty
    }

    @Test
    fun testNoViolationForSpringConfig() {
        val collector = mutableListOf<com.pinterest.ktlint.core.LintError>()
        KtLint.lint(
            KtLint.ExperimentalParams(
                text =
                """
package com.egencia.service.infra.test1

import com.egencia.service.infra.test1.Test1
import com.egencia.service.infra.test2.Test2

@Configuration
class TestConfiguration
                """.trimIndent(),
                ruleProviders = setOf(RuleProvider { NoForeignInfraUsageInInfra() }),
                cb = { e, _ -> collector.add(e) }
            )
        )

        collector.should.be.empty
    }

    @Test
    fun testViolationInInfra() {
        val collector = mutableListOf<com.pinterest.ktlint.core.LintError>()
        KtLint.lint(
            KtLint.ExperimentalParams(
                text =
                """
package com.egencia.service.infra.test1

import com.egencia.service.infra.test1.Test1
import com.egencia.service.infra.test2.Test2

@Gateway
class Hello
                """.trimIndent(),
                ruleProviders = setOf(RuleProvider { NoForeignInfraUsageInInfra() }),
                cb = { e, _ -> collector.add(e) }
            )
        )

        collector.should.contain(
            LintError(
                line = 6,
                col = 1,
                ruleId = "no-foreign-infra-usage-in-infra",
                detail = "This class : com.egencia.service.infra.test1.Hello is in infra package and uses at least " +
                    "one class from another infra package : [com.egencia.service.infra.test2.Test2]"
            )
        )
    }
}
