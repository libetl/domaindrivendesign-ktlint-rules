package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.core.Rule
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtClass
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.annotationNames
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.isNotAClass

class DataClassNotAnnotated : Rule("data-class-not-annotated") {

    override fun visit(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: EmitFunction
    ) {
        if (node.isNotAClass()) return

        val classInformation = node.psi as KtClass

        val isDataClass = classInformation.isData() || classInformation.isEnum()

        val annotationNames = classInformation.annotationNames

        if (isDataClass && listOf("ForeignModel", "ValueType", "Entity", "Aggregate")
            .intersect(annotationNames).isEmpty()
        )
            emit.problemWith(node.startOffset, classInformation.fqName.toString())
    }

    private fun EmitFunction.problemWith(startOffset: Int, className: String) =
        this(
            startOffset,
            "This data class is not annotated with @ForeignModel, @ValueType, @Entity or @Aggregate : $className",
            false
        )
}
