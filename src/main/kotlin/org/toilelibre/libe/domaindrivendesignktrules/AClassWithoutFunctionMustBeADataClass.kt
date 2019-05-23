package org.toilelibre.libe.domaindrivendesignktrules

import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.isNotAClass
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.methods
import com.pinterest.ktlint.core.Rule
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.psiUtil.getNextSiblingIgnoringWhitespaceAndComments
import org.jetbrains.kotlin.psi.psiUtil.isAbstract

class AClassWithoutFunctionMustBeADataClass : Rule("a-class-without-function-must-be-a-data-class") {

    override fun visit(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: EmitFunction
    ) {
        if (node.isNotAClass()) return

        val classInformation = node.psi as KtClass

        val superClassName = classInformation.getColon()
            ?.getNextSiblingIgnoringWhitespaceAndComments(false)?.text ?: "Object"

        val doesNotRequireAMethod = classInformation.isData() || classInformation.isEnum() ||
            classInformation.isAnnotation() || classInformation.isAbstract() ||
            (classInformation.getColon() != null &&
                !superClassName.contains("Object") && !superClassName.contains("Serializable"))

        val methods = classInformation.methods +
            classInformation.companionObjects.flatMap { it.methods }

        if (methods.isEmpty() && !doesNotRequireAMethod)
            emit.problemWith(node.startOffset, classInformation.fqName?.toString() ?: "(unknown class)")
    }

    private fun EmitFunction.problemWith(startOffset: Int, className: String?) =
        this(
            startOffset,
            "This class $className does not have any function. Should not it be a data class ? In any case," +
                "Domain Driven Design discourages the use of anemic classes (POJO or value objects)",
            false
        )
}
