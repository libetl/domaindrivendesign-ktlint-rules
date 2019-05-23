package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.core.Rule
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtClass
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.annotationNames
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.imports
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.isNotAClass

class NoForeignInfraUsageInInfra : Rule("no-foreign-infra-usage-in-infra") {

    override fun visit(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: EmitFunction
    ) {
        if (node.isNotAClass()) return

        val classInformation = node.psi as KtClass

        val className = classInformation.fqName.toString()
        val packageName = classInformation.fqName?.parent().toString()

        val imports = classInformation.imports.values

        if (classInformation.isData() || classInformation.isEnum()) return

        if (className.endsWith("ITConfiguration") || className.endsWith("IT") || className.endsWith("Test")) return

        if (!packageName.contains(".infra.")) return

        if (classInformation.annotationNames.contains("Configuration")) return

        val wrongImports = imports.filter { import -> import.contains(".infra.") && !import.startsWith(packageName) }

        if (wrongImports.isNotEmpty())
            emit.problemWith(node.startOffset, className, wrongImports)
    }

    private fun EmitFunction.problemWith(startOffset: Int, className: String, wrongImports: List<String>) =
        this(
            startOffset,
            "This class : $className is in infra package and uses at least one class from " +
                "another infra package : $wrongImports",
            false
        )
}
