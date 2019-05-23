package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.RuleSet
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class NoForeignInfraUsageInInfraTest {

    @Test
    fun integrationTestInTest() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            """
package org.toilelibre.libe.infra.test1

import org.toilelibre.libe.infra.test1.Test1
import org.toilelibre.libe.infra.test2.Test2

@DomainService
class HelloIT
        """.trimIndent(), listOf(RuleSet("test", NoForeignInfraUsageInInfra()))
        ) { collector.add(it) }

        assertThat(collector).isEmpty()
    }

    @Test
    fun testInTest() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            """
package org.toilelibre.libe.infra.test1

import org.toilelibre.libe.infra.test1.Test1
import org.toilelibre.libe.infra.test2.Test2

@DomainService
class MyTest
        """.trimIndent(), listOf(RuleSet("test", NoForeignInfraUsageInInfra()))
        ) { collector.add(it) }

        assertThat(collector).isEmpty()
    }

    @Test
    fun testOutsideInfra() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            """
package org.toilelibre.libe.domain.ktlintrules

import org.toilelibre.libe.infra.test1.Test1
import org.toilelibre.libe.infra.test2.Test2

@DomainService
class Hello
        """.trimIndent(), listOf(RuleSet("test", NoForeignInfraUsageInInfra()))
        ) { collector.add(it) }

        assertThat(collector).isEmpty()
    }

    @Test
    fun testNoViolationInInfra() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            """
package org.toilelibre.libe.infra.test1

import org.toilelibre.libe.infra.test1.Test1
import org.toilelibre.libe.infra.test1.Test11

@Gateway
class Hello
        """.trimIndent(), listOf(RuleSet("test", NoForeignInfraUsageInInfra()))
        ) { collector.add(it) }

        assertThat(collector).isEmpty()
    }

    @Test
    fun testNoViolationForSpringConfig() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            """
package org.toilelibre.libe.infra.test1

import org.toilelibre.libe.infra.test1.Test1
import org.toilelibre.libe.infra.test2.Test2

@Configuration
class TestConfiguration
        """.trimIndent(), listOf(RuleSet("test", NoForeignInfraUsageInInfra()))
        ) { collector.add(it) }

        assertThat(collector).isEmpty()
    }

    @Test
    fun testViolationInInfra() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            """
package org.toilelibre.libe.infra.test1

import org.toilelibre.libe.infra.test1.Test1
import org.toilelibre.libe.infra.test2.Test2

@Gateway
class Hello
        """.trimIndent(), listOf(RuleSet("test", NoForeignInfraUsageInInfra()))
        ) { collector.add(it) }

        assertThat(collector).containsExactly(
            LintError(
                line = 6, col = 1, ruleId = "test:no-foreign-infra-usage-in-infra",
                detail = "This class : org.toilelibre.libe.infra.test1.Hello is in infra package and uses at least " +
                    "one class from another infra package : [org.toilelibre.libe.infra.test2.Test2]"
            )
        )
    }
}