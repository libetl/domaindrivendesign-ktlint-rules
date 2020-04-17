package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.RuleSet
import com.winterbe.expekt.should
import org.junit.jupiter.api.Test

class NoForeignInfraUsageInInfraTest {

    @Test
    fun integrationTestInTest() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            KtLint.Params(
                text =
            """
package com.egencia.service.infra.test1

import com.egencia.service.infra.test1.Test1
import com.egencia.service.infra.test2.Test2

@DomainService
class HelloIT
        """.trimIndent(), ruleSets = listOf(RuleSet("test", NoForeignInfraUsageInInfra())),
                cb = { e, _ -> collector.add(e) })
        )

        collector.should.be.empty
    }

    @Test
    fun testInTest() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            KtLint.Params(
                text =
            """
package com.egencia.service.infra.test1

import com.egencia.service.infra.test1.Test1
import com.egencia.service.infra.test2.Test2

@DomainService
class MyTest
        """.trimIndent(), ruleSets = listOf(RuleSet("test", NoForeignInfraUsageInInfra())),
                cb = { e, _ -> collector.add(e) })
        )

        collector.should.be.empty
    }

    @Test
    fun testOutsideInfra() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            KtLint.Params(
                text =
            """
package com.egencia.service.domain.ktlintrules

import com.egencia.service.infra.test1.Test1
import com.egencia.service.infra.test2.Test2

@DomainService
class Hello
        """.trimIndent(), ruleSets = listOf(RuleSet("test", NoForeignInfraUsageInInfra())),
                cb = { e, _ -> collector.add(e) })
        )

        collector.should.be.empty
    }

    @Test
    fun testNoViolationInInfra() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            KtLint.Params(
                text =
            """
package com.egencia.service.infra.test1

import com.egencia.service.infra.test1.Test1
import com.egencia.service.infra.test1.Test11

@Gateway
class Hello
        """.trimIndent(), ruleSets = listOf(RuleSet("test", NoForeignInfraUsageInInfra())),
                cb = { e, _ -> collector.add(e) })
        )

        collector.should.be.empty
    }

    @Test
    fun testNoViolationForSpringConfig() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            KtLint.Params(
                text =
            """
package com.egencia.service.infra.test1

import com.egencia.service.infra.test1.Test1
import com.egencia.service.infra.test2.Test2

@Configuration
class TestConfiguration
        """.trimIndent(), ruleSets = listOf(RuleSet("test", NoForeignInfraUsageInInfra())),
                cb = { e, _ -> collector.add(e) })
        )

        collector.should.be.empty
    }

    @Test
    fun testViolationInInfra() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            KtLint.Params(
                text =
            """
package com.egencia.service.infra.test1

import com.egencia.service.infra.test1.Test1
import com.egencia.service.infra.test2.Test2

@Gateway
class Hello
        """.trimIndent(), ruleSets = listOf(RuleSet("test", NoForeignInfraUsageInInfra())),
                cb = { e, _ -> collector.add(e) })
        )

        collector.should.contain(
            LintError(
                line = 6, col = 1, ruleId = "test:no-foreign-infra-usage-in-infra",
                detail = "This class : com.egencia.service.infra.test1.Hello is in infra package and uses at least " +
                    "one class from another infra package : [com.egencia.service.infra.test2.Test2]"
            )
        )
    }
}
