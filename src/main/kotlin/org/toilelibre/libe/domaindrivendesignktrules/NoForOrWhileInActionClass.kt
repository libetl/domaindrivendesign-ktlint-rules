package org.toilelibre.libe.domaindrivendesignktrules

import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtDoWhileExpression
import org.jetbrains.kotlin.psi.KtForExpression
import org.jetbrains.kotlin.psi.KtLoopExpression
import org.jetbrains.kotlin.psi.KtWhileExpression
import org.jetbrains.kotlin.psi.psiUtil.startOffset
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.allThe
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.annotationNames
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.isNotAClass
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.methods

internal class NoForOrWhileInActionClass : Rule("no-for-or-while-in-action-class") {

    override fun beforeVisitChildNodes(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: EmitFunction,
    ) {
        if (node.isNotAClass()) return

        val classInformation = node.psi as KtClass

        val methods = classInformation.methods +
            classInformation.companionObjects.flatMap { it.methods }

        if (!classInformation.annotationNames.contains("Action")) return

        val allTheLoops = methods
            .flatMap { it.bodyBlockExpression?.statements ?: emptyList() }
            .allThe<KtLoopExpression>()

        if (allTheLoops.isNotEmpty()) {
            allTheLoops.map {
                emit.problemWith(
                    it.startOffset,
                    when (it) {
                        is KtForExpression -> "for(. in .)"
                        is KtWhileExpression -> "while(...){...}"
                        is KtDoWhileExpression -> "do{...}while"
                        else -> "loop"
                    },
                )
            }
        }
    }

    private fun EmitFunction.problemWith(
        startOffset: Int,
        kindOfLoop: String,
    ) =
        this(
            startOffset,
            """Action contains a `$kindOfLoop`, this is discouraged.
The business logic must be written in declarative programming.
If you need to tell your reader that you are iterating over a group,
you can consider implementing some idiomatics :
(e.g. : forEachUserCalled {traveler ->...})""",
            false,
        )
}
