package org.toilelibre.libe.domaindrivendesignktrules

import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.annotationNames
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.isPartOf
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.psiUtil.getNonStrictParentOfType
import org.jetbrains.kotlin.psi.psiUtil.isAbstract
import org.jetbrains.kotlin.psi.stubs.elements.KtStubElementTypes

class AllNonForeignDataClassesMembersMustBeImmutable : Rule("no-non-foreign-data-class-member-mutable") {

    override fun visit(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: EmitFunction
    ) {
        if (node.elementType != KtStubElementTypes.VALUE_PARAMETER)
            return

        if (!node.isPartOf(KtStubElementTypes.PRIMARY_CONSTRUCTOR) &&
            !node.isPartOf(KtStubElementTypes.SECONDARY_CONSTRUCTOR)
        )
            return

        val parameter = node.psi as KtParameter

        val owningClass = parameter.getNonStrictParentOfType(KtClass::class.java)

        if (owningClass?.annotationNames?.contains("ForeignModel") == true) return

        if (owningClass?.isAbstract() == true) return

        val isDataClass = owningClass?.isData() ?: false || owningClass?.isEnum() ?: false

        if (!isDataClass || !parameter.hasValOrVar()) return

        val isMutable = parameter.isMutable

        if (isMutable) {
            emit.problemWith(node.startOffset, parameter.fqName?.asString() ?: "(not found)")
        }
    }

    private fun EmitFunction.problemWith(startOffset: Int, name: String) =
        this(
            startOffset,
            "This variable : $name is mutable (should be immutable)",
            false
        )
}
