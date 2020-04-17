package org.toilelibre.libe.domaindrivendesignktrules

import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.allKindsOfTemplatesPackages
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.allThe
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.annotationNames
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.isNotAClass
import com.pinterest.ktlint.core.Rule
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression

class NoTemplateUseInActionOrDomainService : Rule("no-template-use-in-action-or-domain-service") {

    override fun visit(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: EmitFunction
    ) {
        if (node.isNotAClass()) return

        val classInformation = node.psi as KtClass

        if (!classInformation.annotationNames.contains("Action") &&
            !classInformation.annotationNames.contains("DomainService")
        ) return

        val allTheUsedTypes = classInformation
            .containingKtFile.allThe<KtDotQualifiedExpression>()

        val forbiddenPackages = allTheUsedTypes.map { it.text.substringBefore("(") }
            .intersect(allKindsOfTemplatesPackages)

        emit.problemWith(node.startOffset, forbiddenPackages)
    }

    private fun EmitFunction.problemWith(startOffset: Int, violations: Collection<String>) =
        if (violations.isNotEmpty())
            this(
                startOffset,
                "This|These forbidden package(s) is|are used in an Action or in a DomainService : $violations",
                false
            ) else Unit
}
