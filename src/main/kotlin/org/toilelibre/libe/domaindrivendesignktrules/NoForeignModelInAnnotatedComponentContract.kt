package org.toilelibre.libe.domaindrivendesignktrules

import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtPackageDirective
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType
import org.jetbrains.kotlin.psi.psiUtil.getNonStrictParentOfType
import org.jetbrains.kotlin.psi.psiUtil.isPrivate
import org.jetbrains.kotlin.psi.psiUtil.isProtected
import org.jetbrains.kotlin.psi.psiUtil.isPublic
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.annotationNames
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.imports
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.isNotAClass
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.methods
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.parameterTypes
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.typeName

internal class NoForeignModelInAnnotatedComponentContract : Rule("no-foreign-model-in-annotated-component-contract") {

    val KtClass.relevantMethods get() =
        if (annotationNames.intersect(listOf("Endpoint")).isNotEmpty()) {
            methods.filter { it.isProtected() || it.isPrivate() }
        } else if (annotationNames.intersect(listOf("Gateway", "Repository"))
                .isNotEmpty()
        ) {
            methods.filter { it.isPublic || it.isProtected() }
        } else {
            methods
        }

    companion object {
        private var listOfMethodParameterTypes: MutableMap<String, List<String>> = mutableMapOf()
        private var listOfForeignModels: MutableList<String> = mutableListOf()

        fun clear() {
            listOfMethodParameterTypes.clear()
            listOfForeignModels.clear()
        }
    }

    @Synchronized
    override fun beforeVisitChildNodes(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: EmitFunction,
    ) {
        if (node.isNotAClass()) return

        val classInformation = node.psi as KtClass
        if (classInformation.annotationNames.contains("ForeignModel")) {
            listOfForeignModels.add(classInformation.fqName.toString())
            return
        }

        val currentFileImports = classInformation.imports
        val owningPackage = classInformation.getNonStrictParentOfType(KtFile::class.java)
            ?.getChildOfType<KtPackageDirective>()
            ?.fqName

        classInformation.relevantMethods.forEach { method ->
            method.parameterTypes.forEach { parameter ->
                val fullyQualifiedName =
                    currentFileImports[parameter.typeName] ?: "$owningPackage.${parameter.typeName}"
                listOfMethodParameterTypes[fullyQualifiedName] =
                    (
                        (listOfMethodParameterTypes[fullyQualifiedName] ?: listOf()) +
                            ("${classInformation.fqName}.${method.name}")
                        )
            }
        }
    }

    override fun afterVisitChildNodes(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: EmitFunction,
    ) {
        if (node.isNotAClass()) return

        val classInformation = node.psi as KtClass
        if (classInformation.annotationNames.contains("ForeignModel")) {
            val violations: Map<String?, List<String?>> = mapOf(
                classInformation.fqName.toString() to
                        (listOfMethodParameterTypes[classInformation.fqName.toString()] ?: listOf()),
            )

            emit.problemWith(node.startOffset, violations)

            listOfForeignModels.add(classInformation.fqName.toString())
            return
        }
        val currentFileImports = classInformation.imports

        if (classInformation.annotationNames.contains("ForeignModel")) {
            return
        }

        if (classInformation.annotationNames.intersect(
                listOf(
                    "Action",
                    "DomainService",
                    "Gateway",
                    "Repository",
                ),
            ).isEmpty()
        ) {
            return
        }

        val violations =
            classInformation.relevantMethods.map { method ->
                method.name to method.parameterTypes.filter { parameter ->
                    listOfForeignModels.contains(currentFileImports[parameter.typeName])
                }.map { it.typeName }
            }.filter { it.second.isNotEmpty() }.toMap()

        emit.problemWith(node.startOffset, violations)
    }

    private fun EmitFunction.problemWith(startOffset: Int, violations: Map<String?, List<String?>>) =
        if (violations.isNotEmpty() &&
            violations.values.any { it.isNotEmpty() }
        ) {
            this(
                startOffset,
                "Foreign models have been found in some Action or DomainService or Gateway or Repository" +
                    " contract : $violations",
                false,
            )
        } else {
            Unit
        }
}
