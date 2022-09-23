package org.toilelibre.libe.domaindrivendesignktrules

import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.psiUtil.getNonStrictParentOfType
import org.jetbrains.kotlin.psi.psiUtil.isAbstract
import org.jetbrains.kotlin.psi.stubs.elements.KtStubElementTypes

class ADataClassCannotUseAComponent : Rule("data-class-cannot-use-a-component") {

    override fun beforeVisitChildNodes(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: EmitFunction
    ) {
        if (node.elementType != KtStubElementTypes.VALUE_PARAMETER) {
            return
        }

        val parameter = node.psi as KtParameter

        val owningClass = parameter.getNonStrictParentOfType(KtClass::class.java)

        if (owningClass?.isAbstract() == true) return

        val isDataClass = owningClass?.isData() ?: false || owningClass?.isEnum() ?: false

        if (!isDataClass) return

        if (parameter.modifierList?.annotationEntries?.mapNotNull { it.shortName?.asString() }
            ?.any { listOf("Inject", "Autowired").contains(it) } == true
        ) {
            emit.problemWith(
                node.startOffset,
                parameter.fqName?.asString() ?: parameter.name ?: "(not found)"
            )
        }
    }

    private fun EmitFunction.problemWith(startOffset: Int, name: String) =
        this(
            startOffset,
            "This variable : $name is a spring component. Components cannot be used in data classes.\n" +
                "If you need them for data injection (using jackson / graphQL), you need to fork the data class as a serialization bean",
            false
        )
}
