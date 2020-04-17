package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.core.Rule
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.psiUtil.parents

class NoIfInsideIf : Rule("no-if-inside-if") {

    override fun visit(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: EmitFunction
    ) {

        if (node.elementType.toString() == "IF" &&
            !listOf("ELSE", "BINARY_EXPRESSION",
                "VALUE_ARGUMENT", "RETURN").contains(node.treeParent.elementType.toString()) &&
            node.parents().map { it.elementType.toString() }.toList().contains("IF")) {
            emit.problemWith(node.startOffset)
        }
    }

    private fun EmitFunction.problemWith(startOffset: Int) =
        this(
            startOffset,
            "This 'if' statement is nested inside another if. This is not allowed here",
            false
        )
}
