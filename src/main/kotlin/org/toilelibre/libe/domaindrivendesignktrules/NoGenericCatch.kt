package org.toilelibre.libe.domaindrivendesignktrules

import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtCatchClause
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.isNotACatchElement
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.typeName

internal class NoGenericCatch : Rule("no-generic-catch") {

    override fun beforeVisitChildNodes(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: EmitFunction,
    ) {
        if (node.isNotACatchElement()) return

        val catch = node.psi as KtCatchClause
        val exceptionClass = catch.catchParameter?.typeName

        if (listOf(
                "Exception",
                "Throwable",
                "RuntimeException",
                "Error",
                "java.lang.Exception",
                "java.lang.Throwable",
                "java.lang.RuntimeException",
                "java.lang.Error",
            ).contains(exceptionClass)
        ) {
            emit.problemWith(node.startOffset, exceptionClass!!)
        }
    }

    private fun EmitFunction.problemWith(startOffset: Int, exceptionClass: String) =
        this(
            startOffset,
            "Please avoid catching generic Exception classes... like $exceptionClass",
            false,
        )
}
