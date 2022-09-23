package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.core.ast.nextCodeSibling
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtFunctionType
import org.jetbrains.kotlin.psi.KtModifierListOwner
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtNullableType
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtReferenceExpression
import org.jetbrains.kotlin.psi.KtTypeElement
import org.jetbrains.kotlin.psi.KtTypeReference
import org.jetbrains.kotlin.psi.KtUserType
import org.jetbrains.kotlin.psi.psiUtil.forEachDescendantOfType
import org.jetbrains.kotlin.psi.psiUtil.getChildrenOfType
import org.jetbrains.kotlin.psi.stubs.elements.KtStubElementTypes

object SomeHelpers {
    private const val CATCH_CLAUSE: Short = 251

    private fun determineType(type: KtTypeElement?): String? =
        when (type) {
            is KtUserType -> type.referenceExpression?.getReferencedName()!!
            is KtNullableType -> if (type == type.innerType) {
                null
            } else determineType(type.innerType as KtTypeElement)

            is KtFunctionType -> type.text
            else -> null
        }

    val KtModifierListOwner.annotationNames
        get() = this.modifierList?.annotationEntries?.map {
            determineType(it.calleeExpression?.typeReference?.typeElement)
        }.orEmpty().filterNotNull()

    val KtClass.imports
        get() = this.containingKtFile.importDirectives.map {
            (it.alias?.name ?: it.importPath.toString().substringAfterLast(".")) to
                it.importPath.toString()
        }.toMap()

    val KtFunction.variables
        get(): List<KtReferenceExpression> = bodyBlockExpression?.statements
            ?.flatMap { it.getChildrenOfType<KtDotQualifiedExpression>().toList() }
            ?.flatMap { it.getChildrenOfType<KtReferenceExpression>().toList() } ?: listOf()

    fun ASTNode.isNotAClass() = this.elementType != KtStubElementTypes.CLASS
    fun ASTNode.isNotAMethod() = this.elementType != KtStubElementTypes.FUNCTION
    fun ASTNode.isNotACatchElement() = this.elementType.index != CATCH_CLAUSE

    @Suppress("UNCHECKED_CAST")
    val KtClassOrObject.methods
        get() = this.body?.declarations?.filter { it is KtNamedFunction }.orEmpty() as List<KtNamedFunction>

    val KtClassOrObject.members
        get() = (
            body?.declarations?.filterIsInstance<KtProperty>()?.filter { it.isMember }.orEmpty() +
                primaryConstructor?.valueParameters.orEmpty()
            )
            .mapNotNull { it.typeReference?.text to it.name }

    val KtNamedFunction.parameters get() = this.valueParameterList?.parameters
    val KtNamedFunction.returnType get() = (this.colon as ASTNode?)?.nextCodeSibling()?.psi as KtTypeReference?
    val KtNamedFunction.parameterTypes
        get() = (
            (this.parameters?.map { it.typeReference } ?: listOf<KtTypeReference>()) +
                this.returnType
            ).filterNotNull()
    val KtParameter.typeName get() = this.typeReference?.typeName
    val KtTypeReference.typeName get() = determineType(this.typeElement)

    inline fun <reified T : KtElement> KtElement.allThe(): List<T> {
        val collection: MutableList<T> = mutableListOf()
        this.forEachDescendantOfType<T> { collection.add(it) }
        return collection.toList()
    }

    inline fun <reified T : KtElement> List<KtElement>.allThe() =
        this.flatMap { it.allThe<T>() }

    val allKindsOfTemplatesPackages = listOf(
        "org.springframework.data.mongodb.core",
        "org.springframework.web.client",
        "org.springframework.amqp.rabbit.core",
        "org.springframework.ws.client.core",
        "org.springframework.jdbc.core",
        "org.springframework.web.reactive.function.client"
    )
}
