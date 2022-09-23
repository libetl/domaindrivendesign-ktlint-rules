package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.core.ast.parent
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtReferenceExpression
import org.jetbrains.kotlin.psi.psiUtil.getValueParameters
import org.jetbrains.kotlin.psi.psiUtil.isPublic
import org.jetbrains.kotlin.psi.stubs.elements.KtStubElementTypes
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.allThe
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.annotationNames
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.imports
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.isNotAMethod
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.typeName

class NeedsOneCallToAnActionFromAController : Rule("needs-one-call-to-an-action-from-a-controller") {

    override fun beforeVisitChildNodes(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
    ) {
        if (node.isNotAMethod()) return
        val method = node.psi as KtFunction

        // when run on a script, triggers "exceeded garbage collector overhead limit"
        if (method.containingKtFile.isScript()) return

        if (!method.isPublic) return

        val classInformation =
            node.parent(KtStubElementTypes.CLASS)?.psi as KtClass? ?: return

        val isListener = method.annotationNames.any { it.endsWith("Listener") }

        if (classInformation.name?.contains("GraphQL") == true) return

        if (classInformation.annotationNames.intersect(
                listOf("Controller", "RestController", "Endpoint")
            ).isEmpty() && !isListener
        ) {
            return
        }

        val imports = classInformation.imports
        val actionMembers = classInformation.getValueParameters()
            .map { it.name to (it.typeReference?.typeName ?: "") }
            .filter { (imports[it.second] ?: it.second).contains(".actions.") }
            .toMap()

        val allTheReferenceExpressions = (
            method.bodyBlockExpression?.statements
                ?: if (method is KtNamedFunction) listOf(method.initializer) else listOf()
            )
            .allThe<KtReferenceExpression>()

        val result =
            allTheReferenceExpressions.any { expression ->
                actionMembers.any { (key) ->
                    key == expression.text
                }
            }

        if (!result) {
            emit.problemWith(node.startOffset, method.fqName.toString())
        }
    }

    private fun EmitFunction.problemWith(startOffset: Int, functionName: String) =
        this(
            startOffset,
            "This function $functionName does not call anything in the actions package. And it should.",
            false
        )
}
