package org.toilelibre.libe.domaindrivendesignktrules

import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.annotationNames
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.isNotAClass
import com.pinterest.ktlint.core.Rule
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtClass

class EachRoleShouldBeInTheRightPackage : Rule("each-role-should-be-in-the-right-package") {
    override fun visit(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: EmitFunction
    ) {
        if (node.isNotAClass()) return

        val classInformation = node.psi as KtClass

        if (classInformation.isAnnotation()) return

        val className = classInformation.fqName.toString()
        val packageName = classInformation.containingKtFile.packageFqName.asString()
        val annotationNames = classInformation.annotationNames

        if (annotationNames.contains("Component") && !packageName.contains(".infra."))
            emit.problemWith(
                node.startOffset, className,
                "not be annotated with Component (or should be moved to the infra package)"
            )
        else if (annotationNames.contains("Action") && !packageName.endsWith(".actions"))
            emit.problemWith(node.startOffset, className, "be located in the actions package")
        else if (annotationNames.contains("DomainService") && !packageName.contains(".domain."))
            emit.problemWith(node.startOffset, className, "be located in the domain package")
        else if (annotationNames.contains("Gateway") && !packageName.contains(".infra.gateways."))
            emit.problemWith(node.startOffset, className, "be located in the infra.gateways package")
        else if (annotationNames.contains("Repository") && !packageName.contains(".infra.databases."))
            emit.problemWith(node.startOffset, className, "be located in the infra.databases package")
        else if (annotationNames.contains("ForeignModel") && !packageName.contains(".infra."))
            emit.problemWith(node.startOffset, className, "be located in the infra package")
    }

    private fun EmitFunction.problemWith(
        startOffset: Int,
        packageName: String,
        problem: String
    ) =
        this(
            startOffset,
            "While checking the package structure for the class $packageName, " +
                "it has been discovered that it should $problem",
            false
        )
}
