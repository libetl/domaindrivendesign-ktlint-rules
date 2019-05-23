package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.parent
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.stubs.elements.KtStubElementTypes
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.annotationNames
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.isNotAMethod
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.typeName

class NoPrimitiveObsessionInAnnotatedComponent : Rule("no-primitive-obsession-in-action-or-domain-service") {
    override fun visit(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: EmitFunction
    ) {

        if (node.isNotAMethod()) return

        val owningClass =
            node.parent(KtStubElementTypes.CLASS)?.psi as KtClass? ?: return

        if (owningClass.annotationNames.intersect(
            listOf(
                "Action", "DomainService", "Gateway", "Repository"
            )
        ).isEmpty()
        )
        /* We never know which kind of boilerplate we can pull
           Sometimes a vendor may ask for a contract with
           primitive values
           So we are excluding everything that is not annotated */
            return

        val function = node.psi as KtFunction

        val parameterTypes =
            function.valueParameters.map { it.typeName }

        if (parameterTypes.size <= 1)
            return

        // methods with only containing one parameter are tolerated

        if (parameterTypes.map {
            it?.replace("java.lang", "")
                ?.replace("kotlin.", "")
                ?.replace("java.math", "")
                ?.replace("com.fasterxml.jackson.databind.node", "")
                ?.replace("com.fasterxml.jackson.databind", "")
        }
            .intersect(
                listOf(
                    "ArrayNode", "ObjectNode", "JsonNode",
                    "String", "Int", "BigDecimal",
                    "Boolean", "Long", "Double", "Short", "Float"
                )
            ).isNotEmpty()
        ) {
            emit.problemWith(node.startOffset, function.name ?: "(unknown method)")
        }
    }

    private fun EmitFunction.problemWith(startOffset: Int, functionName: String) =
        this(
            startOffset,
            "This function $functionName uses too much primitive types. Please (re)use some @ValueType classes " +
                "and pass them as parameters",
            false
        )
}
