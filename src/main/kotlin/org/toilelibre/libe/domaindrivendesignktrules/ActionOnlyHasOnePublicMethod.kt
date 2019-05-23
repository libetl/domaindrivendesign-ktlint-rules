package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.core.Rule
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.psiUtil.isPublic
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.annotationNames
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.isNotAClass
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.methods

class ActionOnlyHasOnePublicMethod : Rule("only-one-public-method-in-action") {

    override fun visit(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: EmitFunction
    ) {
        if (node.isNotAClass()) return

        val classInformation = node.psi as KtClass

        val methods =
            classInformation.methods +
                classInformation.companionObjects.flatMap { it.methods }

        if (!classInformation.annotationNames.contains("Action")) return

        val publicMethods = methods.filter { it.isPublic }
        val publicMethodsCount = publicMethods.size
        val nonPublicMethodsCount = (methods - publicMethods).size

        if (publicMethodsCount > 1 || nonPublicMethodsCount > 0)
            emit.problemWith(
                node.startOffset,
                classInformation.fqName.toString(), publicMethodsCount, nonPublicMethodsCount
            )
    }

    private fun EmitFunction.problemWith(
        startOffset: Int,
        className: String,
        publicMethodsCount: Int,
        nonPublicMethodsCount: Int
    ) =
        this(
            startOffset,
            "Action $className should have one public method (found $publicMethodsCount), " +
                "and no private method (found $nonPublicMethodsCount)",
            false
        )
}
