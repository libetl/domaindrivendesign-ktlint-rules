package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.rule.engine.api.Code
import com.pinterest.ktlint.rule.engine.api.KtLintRuleEngine
import com.pinterest.ktlint.rule.engine.api.LintError
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.RuleProvider
import com.winterbe.expekt.should
import org.junit.jupiter.api.Test
import org.toilelibre.libe.domaindrivendesignktrules.DomainDrivenDesignRuleSetProvider.Companion.rulesetName

class ADataClassCannotUseAComponentTest {

    @Test
    fun testNoViolation() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { ADataClassCannotUseAComponent() }))
            .lint(
                Code.fromSnippet(
                    """
package some.packages

@ValueType
data class Price (
  val currency: Currency,
  val amount: Float,
) {
  fun toCurrency(newCurrency: Currency): Float {
    return Currency.rate(currency, newCurrency) * amount
  }
}

                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.be.empty
    }

    @Test
    fun testViolationInConstructor() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { ADataClassCannotUseAComponent() }))
            .lint(
                Code.fromSnippet(
                    """
package some.packages

@ValueType
data class Price (
  @Inject
  val currencyConverter: CurrencyConverter,
  val amount: Float,
) {
  fun toCurrency(newCurrency: Currency): Float {
    return currencyConverter.rate(currency, newCurrency) * amount
  }
}

                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.contain(
            LintError(
                line = 5,
                col = 3,
                ruleId = RuleId("$rulesetName:data-class-cannot-use-a-component"),
                canBeAutoCorrected = false,
                detail = "This variable : some.packages.Price.currencyConverter is a spring component. Components cannot be used in data classes.\n" +
                    "If you need them for data injection (using jackson / graphQL), you need to fork the data class as a serialization bean",
            ),
        )
    }

    @Test
    fun testViolationInGetter() {
        val collector = mutableListOf<LintError>()
        KtLintRuleEngine(setOf(RuleProvider { ADataClassCannotUseAComponent() }))
            .lint(
                Code.fromSnippet(
                    """
package some.packages

@ValueType
data class USDPrice (
  val amount: Float,
) {
  fun getInEuros(@Autowired currencyConverter: CurrencyConverter): Float {
    return currencyConverter.rate("USD", "EUR") * amount
  }
}

                    """.trimIndent(),
                ),
                callback = { e -> collector.add(e) },
            )

        collector.should.contain(
            LintError(
                line = 7,
                col = 18,
                ruleId = RuleId("$rulesetName:data-class-cannot-use-a-component"),
                canBeAutoCorrected = false,
                detail = "This variable : currencyConverter is a spring component. Components cannot be used in data classes.\n" +
                    "If you need them for data injection (using jackson / graphQL), you need to fork the data class as a serialization bean",
            ),
        )
    }
}
