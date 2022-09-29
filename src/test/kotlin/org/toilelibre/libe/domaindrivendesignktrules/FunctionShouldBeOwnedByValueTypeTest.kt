package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.core.KtLint
import com.winterbe.expekt.should
import org.junit.jupiter.api.Test

class FunctionShouldBeOwnedByValueTypeTest {

    @Test
    fun testNoViolation() {
        val collector = mutableListOf<com.pinterest.ktlint.core.LintError>()
        KtLint.lint(
            KtLint.ExperimentalParams(
                text =
                """
package some.packages

@ValueType
data class Currency {
  fun rate(olderCurrency: Currency): Float {
     return CURRENCY_TABLE[olderCurrency]
  }
}

@ValueType
data class Price (
  val currency: Currency,
  val amount: Float,
) {
  fun toCurrency(newCurrency: Currency): Float {
    return newCurrency.rate(currency) * amount
  }
}

                """.trimIndent(),
                ruleProviders = setOf(RuleProvider { FunctionShouldBeOwnedByValueType() }),
                cb = { e, _ -> collector.add(e) }
            )
        )

        collector.should.be.empty
    }

    @Test
    fun testNoViolationWhenTheFunctionUsesAClassMember() {
        val collector = mutableListOf<com.pinterest.ktlint.core.LintError>()
        KtLint.lint(
            KtLint.ExperimentalParams(
                text =
                """
package some.packages

@ValueType
data class Currency(val name: String, val currencyTable: Map<String, Float>)

class PriceConverter(val currencyHelper: CurrencyHelper) {
  fun getRate(newCurrency: Currency): Float {
    return currencyHelper.extractRateFromCurrencyTable(currency.name, newCurrency.name)
  }
}

                """.trimIndent(),
                ruleProviders = setOf(RuleProvider { FunctionShouldBeOwnedByValueType() }),
                cb = { e, _ -> collector.add(e) }
            )
        )

        collector.should.be.empty
    }

    @Test
    fun testNoViolationWhenTheFunctionUsesAComponent() {
        val collector = mutableListOf<com.pinterest.ktlint.core.LintError>()
        KtLint.lint(
            KtLint.ExperimentalParams(
                text =
                """
package com.company.service.test.infra.gateways.configservice

import com.company.library.configuration.configservice.resolver.ConfigurationResolver
import com.company.library.configuration.configservice.resolver.ResolverParams
import com.company.service.test.infra.gateways.configservice.ProductAndTravelerReferences
import com.company.service.test.domain.transverse.DomainDrivenDesignAnnotations.Gateway

@ValueType
data class ProductAndTravelerReferences(pointOfSale: PointOfSale, company: Company, productsAndTravelersMapping: List<Mapping>)

@Gateway
class ArrangerCardsToggler(private val config: ConfigurationResolver) {

    infix fun displayOrHideArrangerCardsAccordingTo(theInput: ProductAndTravelerReferences): Boolean =
        theInput.productsAndTravelersMapping.any {
            val params = ResolverParams()
                .withProductId(theInput.pointOfSale.code)
                .withCompanyId(theInput.company.id.value)
                .withStripe("lineOfBusiness", it.designation.lineOfBusiness.name)
            config.resolve("arrangerCreditCardAllowed", params) as String? == "true" &&
                config.resolve("arrangercc", params) as String? == "1"
        }
}
                """.trimIndent(),
                ruleProviders = setOf(RuleProvider { FunctionShouldBeOwnedByValueType() }),
                cb = { e, _ -> collector.add(e) }
            )
        )

        collector.should.be.empty
    }

    @Test
    fun testNoViolationWhenTheFunctionUsesAClassFunction() {
        val collector = mutableListOf<com.pinterest.ktlint.core.LintError>()
        KtLint.lint(
            KtLint.ExperimentalParams(
                text =
                """
package some.packages

@ValueType
data class Currency(val name: String, val currencyTable: Map<String, Float>)

class PriceConverter(val currencyHelper: CurrencyHelper) {
  fun getRate(newCurrency: Currency): Float {
    return this.delegateToCurrencyHelper(newCurrency)
  }

  private fun delegateToCurrencyHelper(newCurrency: Currency) {
    return currencyHelper.extractRateFromCurrencyTable(currency.name, newCurrency.name)
  }
}

                """.trimIndent(),
                ruleProviders = setOf(RuleProvider { FunctionShouldBeOwnedByValueType() }),
                cb = { e, _ -> collector.add(e) }
            )
        )

        collector.should.be.empty
    }

    @Test
    fun testViolation() {
        val collector = mutableListOf<com.pinterest.ktlint.core.LintError>()
        KtLint.lint(
            KtLint.ExperimentalParams(
                text =
                """
package some.packages

@ValueType
data class Currency(val name: String, val currencyTable: Map<String, Float>)

class PriceConverter {
  fun getRate(newCurrency: Currency): Float {
    return newCurrency.currencyTable[currency.name]
  }
}

                """.trimIndent(),
                ruleProviders = setOf(RuleProvider { FunctionShouldBeOwnedByValueType() }),
                cb = { e, _ -> collector.add(e) }
            )
        )

        collector.should.contain(
            LintError(
                line = 7,
                col = 3,
                ruleId = "function-should-be-owned-by-value-type",
                detail = "The function some.packages.PriceConverter.getRate uses the value type newCurrency (Currency) as its only parameter.\n" +
                    "In this situation, you should make it a member of Currency."
            )
        )
    }
}
