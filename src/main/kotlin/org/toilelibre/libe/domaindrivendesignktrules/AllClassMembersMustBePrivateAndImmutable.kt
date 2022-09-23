package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.core.ast.isPartOf
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.psiUtil.getNonStrictParentOfType
import org.jetbrains.kotlin.psi.psiUtil.isAbstract
import org.jetbrains.kotlin.psi.stubs.elements.KtStubElementTypes

class AllClassMembersMustBePrivateAndImmutable : Rule("no-class-member-public-or-mutable") {

    override fun beforeVisitChildNodes(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: EmitFunction
    ) {
        if (node.elementType != KtStubElementTypes.VALUE_PARAMETER) {
            return
        }

        if (!node.isPartOf(KtStubElementTypes.PRIMARY_CONSTRUCTOR) &&
            !node.isPartOf(KtStubElementTypes.SECONDARY_CONSTRUCTOR)
        ) {
            return
        }

        val parameter = node.psi as KtParameter

        val owningClass = parameter.getNonStrictParentOfType(KtClass::class.java)

        val isDataClass = owningClass?.isData() ?: false || owningClass?.isEnum() ?: false

        if (owningClass?.isAbstract() == true) return

        if (isDataClass || !parameter.hasValOrVar()) return

        if (parameter.modifierList?.firstChild?.text == "override") return

        val isMutable = parameter.isMutable
        val isPrivate = parameter.modifierList?.text?.contains("private") ?: false

        if (isMutable || !isPrivate) {
            emit.problemWith(node.startOffset, parameter.fqName?.asString() ?: "(not found)", isMutable, isPrivate)
        }
    }

    private fun EmitFunction.problemWith(startOffset: Int, name: String, isMutable: Boolean, isPrivate: Boolean) =
        this(
            startOffset,
            "This variable : $name is ${if (isMutable) "" else "not "}mutable and is ${
            if (isPrivate) "" else "not "
            }private (and should be not mutable and private)",
            false
        )
}
