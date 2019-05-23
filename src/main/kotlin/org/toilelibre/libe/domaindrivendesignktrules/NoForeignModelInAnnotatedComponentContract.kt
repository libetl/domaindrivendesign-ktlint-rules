package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.core.Rule
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtClass
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.annotationNames
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.imports
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.isNotAClass
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.methods
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.parameterTypes
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.typeName

class NoForeignModelInAnnotatedComponentContract : Rule("no-foreign-model-in-annotated-component-contract") {

    companion object {
        private var listOfMethodParameterTypes: MutableMap<String, List<String>> = mutableMapOf()
        private var listOfForeignModels: MutableList<String> = mutableListOf()

        fun clear() {
            listOfMethodParameterTypes.clear()
            listOfForeignModels.clear()
        }
    }

    @Synchronized
    override fun visit(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: EmitFunction
    ) {
        if (node.isNotAClass()) return

        val classInformation = node.psi as KtClass

        val currentFileImports = classInformation.imports

        if (classInformation.annotationNames.contains("ForeignModel")) {
            val violations: Map<String?, List<String?>> = mapOf(
                classInformation.fqName.toString() to
                    (listOfMethodParameterTypes[classInformation.fqName.toString()] ?: listOf())
            )

            emit.problemWith(node.startOffset, violations)

            listOfForeignModels.add(classInformation.fqName.toString())
            return
        }

        if (classInformation.annotationNames.intersect(
            listOf(
                "Action", "DomainService", "Gateway", "Repository"
            )
        ).isEmpty()
        )
            return

        val methods = classInformation.methods

        val violations =
            methods.map { method ->
                method.name to method.parameterTypes.filter { parameter ->
                    listOfForeignModels.contains(currentFileImports[parameter.typeName])
                }.map { it.typeName }
            }.filter { it.second.isNotEmpty() }.toMap()

        methods.forEach { method ->
            method.parameterTypes.forEach { parameter ->
                val fullyQualifiedName = currentFileImports[parameter.typeName]
                if (fullyQualifiedName != null)
                    listOfMethodParameterTypes[fullyQualifiedName] =
                        (
                            (listOfMethodParameterTypes[fullyQualifiedName] ?: listOf()) +
                                ("${classInformation.fqName}.${method.name}")
                            )
            }
        }

        emit.problemWith(node.startOffset, violations)
    }

    private fun EmitFunction.problemWith(startOffset: Int, violations: Map<String?, List<String?>>) =
        if (violations.isNotEmpty() &&
            violations.values.any { it.isNotEmpty() }
        )
            this(
                startOffset,
                "Foreign models have been found in some Action or DomainService or Gateway or Repository" +
                    " contract : $violations",
                false
            ) else Unit
}
