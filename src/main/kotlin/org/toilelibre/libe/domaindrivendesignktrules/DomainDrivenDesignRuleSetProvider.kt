package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.core.RuleSet
import com.pinterest.ktlint.core.RuleSetProvider

class DomainDrivenDesignRuleSetProvider : RuleSetProvider {
    override fun get() = RuleSet("domain-driven-design-ktlint-rules",
            NoForeignModelInAnnotatedComponentContract(),
            DataClassNotAnnotated(),
            NoForeignInfraUsageInInfra(),
            NoGenericCatch(),
            ActionOnlyHasOnePublicMethod(),
            NoForOrWhileInActionClass(),
            NoTemplateUseInActionOrDomainService(),
            GatewayOrRepositoryMustHaveOnlyOneTemplateVariable(),
            NoBreakOrContinue(),
            AllClassMembersMustBePrivateAndImmutable(),
            AllNonForeignDataClassesMembersMustBeImmutable(),
            AClassWithoutFunctionMustBeADataClass(),
            NoPrimitiveObsessionInAnnotatedComponent(),
            NeedsOneCallToAnActionFromAController(),
            EachRoleShouldBeInTheRightPackage())
}
