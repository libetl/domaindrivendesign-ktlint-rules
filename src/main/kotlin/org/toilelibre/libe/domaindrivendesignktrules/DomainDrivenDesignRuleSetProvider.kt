package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.cli.ruleset.core.api.RuleSetProviderV3
import com.pinterest.ktlint.rule.engine.core.api.RuleProvider
import com.pinterest.ktlint.rule.engine.core.api.RuleSetId

internal class DomainDrivenDesignRuleSetProvider : RuleSetProviderV3(RuleSetId(rulesetName)) {
    companion object {
        const val rulesetName = "domain-driven-design-ktlint-rules"
    }

    override fun getRuleProviders() = setOf(
        RuleProvider { NoForeignModelInAnnotatedComponentContract() },
        RuleProvider { DataClassNotAnnotated() },
        RuleProvider { NoForeignInfraUsageInInfra() },
        RuleProvider { NoGenericCatch() },
        RuleProvider { ActionOnlyHasOnePublicMethod() },
        RuleProvider { NoForOrWhileInActionClass() },
        RuleProvider { NoIfInsideIf() },
        RuleProvider { NoTemplateUseInActionOrDomainService() },
        RuleProvider { GatewayOrRepositoryMustHaveOnlyOneTemplateVariable() },
        RuleProvider { NoBreakOrContinue() },
        RuleProvider { AllClassMembersMustBePrivateAndImmutable() },
        RuleProvider { AllNonForeignDataClassesMembersMustBeImmutable() },
        RuleProvider { AClassWithoutFunctionMustBeADataClass() },
        RuleProvider { NoPrimitiveObsessionInAnnotatedComponent() },
        RuleProvider { NeedsOneCallToAnActionFromAController() },
        RuleProvider { EachRoleShouldBeInTheRightPackage() },
        RuleProvider { ADataClassCannotUseAMap() },
        RuleProvider { AnActionCannotUseAnotherAction() },
        RuleProvider { ADataClassCannotUseAComponent() },
        RuleProvider { FunctionShouldBeOwnedByValueType() },
    )
}
