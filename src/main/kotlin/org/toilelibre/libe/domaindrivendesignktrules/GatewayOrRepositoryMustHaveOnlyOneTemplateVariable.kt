package org.toilelibre.libe.domaindrivendesignktrules

import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtTypeReference
import org.jetbrains.kotlin.psi.KtVariableDeclaration
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.allKindsOfTemplatesPackages
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.allThe
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.annotationNames
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.imports
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.isNotAClass
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.typeName

internal class GatewayOrRepositoryMustHaveOnlyOneTemplateVariable :
    Rule("gateway-or-repository-must-have-only-one-template-variable") {

    override fun beforeVisitChildNodes(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: EmitFunction,
    ) {
        if (node.isNotAClass()) return

        val classInformation = node.psi as KtClass

        if (!classInformation.annotationNames.contains("Gateway") &&
            !classInformation.annotationNames.contains("Repository")
        ) {
            return
        }

        val currentFileImports = classInformation.imports

        val variableDeclarations = classInformation.allThe<KtVariableDeclaration>()

        val types = variableDeclarations.flatMap { it.allThe<KtTypeReference>() }
            .map { it.typeName }.map { currentFileImports[it] ?: it }
            .map { it?.substringBeforeLast(".") }
            .filter { allKindsOfTemplatesPackages.contains(it) }

        val tooMuchOccurrences =
            types.map { it to types.count { value -> value == it } }.toMap()

        if (types.size > 1) {
            emit.problemWith(node.startOffset, tooMuchOccurrences)
        }
    }

    private fun EmitFunction.problemWith(
        startOffset: Int,
        tooMuchOccurrences: Map<String?, Int>,
    ) =
        this(
            startOffset,
            "This infra role defines more than one *Template class. Only one is allowed. (found : $tooMuchOccurrences)",
            false,
        )
}
