package org.toilelibre.libe.domaindrivendesignktrules

import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtPackageDirective
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType
import org.jetbrains.kotlin.psi.psiUtil.getNonStrictParentOfType
import org.jetbrains.kotlin.psi.psiUtil.isAbstract
import org.jetbrains.kotlin.psi.psiUtil.referenceExpression
import org.jetbrains.kotlin.psi.stubs.elements.KtStubElementTypes
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.annotationNames
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.imports
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.members
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.methods
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.typeName
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.variables

internal class FunctionShouldBeOwnedByValueType : Rule("function-should-be-owned-by-value-type") {

    companion object {
        private var listOfDataClasses: MutableSet<String> = mutableSetOf()

        fun clear() {
            listOfDataClasses.clear()
        }
    }

    override fun beforeVisitChildNodes(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: EmitFunction,
    ) {
        if (node.elementType == KtStubElementTypes.CLASS) {
            val classInformation = node.psi as KtClass
            if (classInformation.annotationNames.contains("ForeignModel") ||
                classInformation.annotationNames.contains("Aggregate") ||
                classInformation.annotationNames.contains("ValueType") ||
                classInformation.annotationNames.contains("Entity")
            ) {
                classInformation.fqName?.asString()?.let { listOfDataClasses.add(it) }
            }
            return
        }
    }

    override fun afterVisitChildNodes(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: EmitFunction,
    ) {
        if (node.elementType == KtStubElementTypes.CLASS) {
            return
        }

        if (node.elementType != KtStubElementTypes.FUNCTION) {
            return
        }

        val function = node.psi as KtFunction

        val owningClass = function.getNonStrictParentOfType(KtClass::class.java)
        val owningPackage = function.getNonStrictParentOfType(KtFile::class.java)
            ?.getChildOfType<KtPackageDirective>()
            ?.fqName

        val members = owningClass?.members?.map { it.second }.orEmpty() +
            owningClass?.methods?.map { it.name }.orEmpty()
        val variables = function.variables.mapNotNull { it.referenceExpression() }
            .filterIsInstance<KtNameReferenceExpression>()
            .map { it.getReferencedName() }

        val currentFileImports = owningClass?.imports ?: mapOf()

        if (owningClass?.isAbstract() == true) return

        val isDataClass = owningClass?.isData() ?: false || owningClass?.isEnum() ?: false

        if (isDataClass) return

        if (function.receiverTypeReference == null &&
            function.valueParameters.size == 1 &&
            !function.valueParameters[0].isVarArg &&
            function.annotationNames.intersect(
                listOf(
                    "ExceptionHandler",
                    "RestController",
                    "Controller",
                    "RabbitListener",
                    "Endpoint",
                ),
            ).isEmpty() &&
            listOfDataClasses.contains(
                currentFileImports[function.valueParameters[0].typeName]
                    ?: "$owningPackage.${function.valueParameters[0].typeName}",
            ) &&
            variables.none { members.contains(it) }
        ) {
            emit.problemWith(
                node.startOffset,
                function.fqName?.asString() ?: "(not found)",
                function.valueParameters[0].name ?: "(not found)",
                function.valueParameters[0].typeName ?: "(not found)",
            )
        }
    }

    private fun EmitFunction.problemWith(startOffset: Int, name: String, valueName: String, valueType: String) =
        this(
            startOffset,
            "The function $name uses the value type $valueName ($valueType) as its only parameter.\n" +
                "In this situation, you should make it a member of $valueType.",
            false,
        )
}
