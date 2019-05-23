package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.RuleSet
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EachRoleShouldBeInTheRightPackageTest {

    @Test
    fun testViolationAction() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            """
package some.packages.infra

@Action
class MyAction {

  fun someMethod() {
  }
}
        """.trimIndent(), listOf(RuleSet("test", EachRoleShouldBeInTheRightPackage()))
        ) { collector.add(it) }

        assertThat(collector).containsExactly(
            LintError(
                line = 3, col = 1,
                ruleId = "test:each-role-should-be-in-the-right-package",
                detail = "While checking the package structure for the class some.packages.infra.MyAction, it has been discovered that it should be located in the actions package"
            )
        )
    }

    @Test
    fun testViolationDomainService() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            """
package some.packages.infra

@DomainService
class MyDomainService {

  fun someMethod() {
  }
}
        """.trimIndent(), listOf(RuleSet("test", EachRoleShouldBeInTheRightPackage()))
        ) { collector.add(it) }

        assertThat(collector).containsExactly(
            LintError(
                line = 3, col = 1,
                ruleId = "test:each-role-should-be-in-the-right-package",
                detail = "While checking the package structure for the class some.packages.infra.MyDomainService, it has been discovered that it should be located in the domain package"
            )
        )
    }

    @Test
    fun testViolationRepository() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            """
package some.packages.action

@Repository
class MyRepository {

  fun someMethod() {
  }
}
        """.trimIndent(), listOf(RuleSet("test", EachRoleShouldBeInTheRightPackage()))
        ) { collector.add(it) }

        assertThat(collector).containsExactly(
            LintError(
                line = 3, col = 1,
                ruleId = "test:each-role-should-be-in-the-right-package",
                detail = "While checking the package structure for the class some.packages.action.MyRepository, it has been discovered that it should be located in the infra.databases package"
            )
        )
    }

    @Test
    fun testViolationGateway() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            """
package some.packages.action

@Gateway
class MyGateway {

  fun someMethod() {
  }
}
        """.trimIndent(), listOf(RuleSet("test", EachRoleShouldBeInTheRightPackage()))
        ) { collector.add(it) }

        assertThat(collector).containsExactly(
            LintError(
                line = 3, col = 1,
                ruleId = "test:each-role-should-be-in-the-right-package",
                detail = "While checking the package structure for the class some.packages.action.MyGateway, it has been discovered that it should be located in the infra.gateways package"
            )
        )
    }

    @Test
    fun testViolationComponent() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            """
package some.packages.action

@Component
class MyComponent {

  fun someMethod() {
  }
}
        """.trimIndent(), listOf(RuleSet("test", EachRoleShouldBeInTheRightPackage()))
        ) { collector.add(it) }

        assertThat(collector).containsExactly(
            LintError(
                line = 3, col = 1,
                ruleId = "test:each-role-should-be-in-the-right-package",
                detail = "While checking the package structure for the class some.packages.action.MyComponent, it has been discovered that it should not be annotated with Component (or should be moved to the infra package)"
            )
        )
    }

    @Test
    fun testNoViolationComponent() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            """
package some.packages.infra.gateways.myservice

@Component
class MyComponent {

  fun someMethod() {
  }
}
        """.trimIndent(), listOf(RuleSet("test", EachRoleShouldBeInTheRightPackage()))
        ) { collector.add(it) }

        assertThat(collector).isEmpty()
    }

    @Test
    fun testNoViolationAction() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            """
package some.packages.actions

@Action
class MyAction {

  fun someMethod() {
  }
}
        """.trimIndent(), listOf(RuleSet("test", EachRoleShouldBeInTheRightPackage()))
        ) { collector.add(it) }

        assertThat(collector).isEmpty()
    }

    @Test
    fun testNoViolationWhenNoPackage() {

        val collector = mutableListOf<LintError>()
        KtLint.lint(
            """

class MyClass {

  fun someMethod() {
  }
}
        """.trimIndent(), listOf(RuleSet("test", EachRoleShouldBeInTheRightPackage()))
        ) { collector.add(it) }

        assertThat(collector).isEmpty()
    }
}