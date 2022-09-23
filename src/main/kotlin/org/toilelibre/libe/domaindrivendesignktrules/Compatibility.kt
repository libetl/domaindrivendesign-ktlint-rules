package org.toilelibre.libe.domaindrivendesignktrules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.RuleSet
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

// compatibility with ktlint >= 0.47

fun interface RuleProvider {
    fun provide(): Rule
}

fun LintError(
    line: Int,
    col: Int,
    ruleId: String,
    detail: String
) = com.pinterest.ktlint.core.LintError(line, col, "test:$ruleId", detail, false)

fun KtLint.ExperimentalParams(
    text: String,
    ruleProviders: Set<RuleProvider>,
    cb: (e: com.pinterest.ktlint.core.LintError, corrected: Boolean) -> Unit
) =
    KtLint.Params(text = text, cb = cb, ruleSets = ruleProviders.map { RuleSet("test", it.provide()) })

abstract class Rule(id: String) : Rule(id) {
    override fun visit(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
    ) {
        beforeVisitChildNodes(node, autoCorrect, emit)
    }

    abstract fun beforeVisitChildNodes(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: EmitFunction
    )
}
