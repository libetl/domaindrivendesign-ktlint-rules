package org.toilelibre.libe.domaindrivendesignktrules

import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtClass
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.annotationNames
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.isNotAClass
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.members

class AnActionCannotUseAnotherAction : Rule("an-action-cannot-use-another-action") {

    companion object {
        private var listOfActions: MutableSet<String> = mutableSetOf<String>()

        fun clear() {
            listOfActions.clear()
        }
    }

    override fun beforeVisitChildNodes(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: EmitFunction
    ) {
        if (node.isNotAClass()) return

        val classInformation = node.psi as KtClass

        if (!classInformation.annotationNames.contains("Action")) return

        listOfActions.add(classInformation.name!!)

        val violations = classInformation.members.filter {
            listOfActions.contains(it.first)
        }

        violations.forEach {
            emit.problemWith(node.startOffset, classInformation.name!!, it.first!!)
        }
    }

    private fun EmitFunction.problemWith(
        startOffset: Int,
        action1: String,
        action2: String
    ) =
        this(
            startOffset,
            "Action $action1 should not use Action $action2",
            false
        )
}
