package org.toilelibre.libe.domaindrivendesignktrules

import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.lexer.KtTokens

class NoBreakOrContinue : Rule("no-break-or-continue") {

    override fun beforeVisitChildNodes(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: EmitFunction
    ) {
        if (node.elementType.index == KtTokens.BREAK_KEYWORD.index ||
            node.elementType.index == KtTokens.CONTINUE_KEYWORD.index
        ) {
            emit.problemWith(node.startOffset)
        }
    }

    private fun EmitFunction.problemWith(startOffset: Int) =
        this(
            startOffset,
            "Loop or statement breakers like break or continue are not allowed. Please do it wiser",
            false
        )
}
