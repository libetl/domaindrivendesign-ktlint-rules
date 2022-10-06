package org.toilelibre.libe.domaindrivendesignktrules

import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.com.intellij.psi.impl.PsiFileFactoryImpl
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import org.jetbrains.kotlin.psi.KtValueArgument
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.allThe
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.annotationNames
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.methods
import org.toilelibre.libe.domaindrivendesignktrules.SomeHelpers.variables
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.readText
import kotlin.streams.asSequence

object GenerateDocumentation {

    @Suppress("UNCHECKED_CAST")
    operator fun String.invoke(vararg attrs: Pair<String, Any>, nested: StringBuilder.() -> Any?): String {
        return "<$this${attrs.joinToString("") { """ ${it.first}="${it.second}"""" }}>${
            StringBuilder().apply {
                val result = nested(this)
                if (result is Iterable<*> && result.all { it is Iterable<*> })
                    this.append((result as Iterable<Iterable<*>>).flatten().joinToString(""))
                else if (result is Iterable<*>) this.append(result.joinToString(""))
                else if (result != null) this.append(result)
            }
        }</$this>"
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val compilerConfiguration = CompilerConfiguration().apply {
            put(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)
        }
        val disposable = Disposer.newDisposable()
        val project = KotlinCoreEnvironment.createForProduction(
            disposable,
            compilerConfiguration,
            EnvironmentConfigFiles.JVM_CONFIG_FILES
        ).project as MockProject

        val programStructureInterfaceFactory = PsiFileFactoryImpl.getInstance(project)

        val testSources = Files.walk(
            Paths.get(
                System.getProperty("user.dir"),
                "src",
                "test",
                "kotlin",
                "org",
                "toilelibre",
                "libe",
                "domaindrivendesignktrules"
            )
        ).asSequence().filter { it.fileName.toString().endsWith("Test.kt") }
            .map {
                it.fileName.toString().replace("Test.kt", "") to programStructureInterfaceFactory.createFileFromText(
                    it.fileName.toString(),
                    KotlinLanguage.INSTANCE,
                    it.readText()
                ) as KtFile
            }.toMap()

        val rulesContent = testSources.entries.joinToString(separator = "<br/>") { (name, file) ->
            "section"("id" to name) {
                "div"("class" to "mdl-grid") {
                    "div"("class" to "mdl-cell mdl-cell--3-col mdl-cell--12-col-phone") {
                        "h6" {
                            "a"("href" to "#$name") {
                                name.replace(Regex("[a-z][A-Z]")) {
                                    "${it.value[0]} ${it.value[1]}"
                                }
                            }
                        } +
                            "div"("class" to "padding-block") {
                                "&nbsp;"
                            }
                    } +
                        "div"("class" to "mdl-cell mdl-cell--9-col mdl-cell--12-col-phone") {
                            "div"("class" to "snippet-captions") {
                                file.allThe<KtClass>().flatMap { it.methods }
                                    .filter { it.annotationNames.contains("Test") }
                                    .joinToString(separator = "\n") { function ->
                                        val good = function.variables.any { it.text == "empty" }
                                        val code = (function.allThe<KtValueArgument>()
                                            .filter {
                                                it.isNamed() &&
                                                    it.getArgumentName()?.referenceExpression?.getIdentifier()?.text == "text"
                                            }
                                            .allThe<KtStringTemplateExpression>().firstOrNull()
                                            ?: function.allThe<KtStringTemplateExpression>().first()).text
                                            .replace(Regex("^\"\"\""), "")
                                            .replace(Regex("\"\"\"$"), "")
                                        "div"("class" to "snippet-caption snippet-${if (good) "good" else "bad"}") {
                                            "span"("class" to "snippet-hint") {
                                                function.name?.replace(Regex("^test"), "")
                                                    ?.replace(Regex("[a-z][A-Z]")) {
                                                        "${it.value[0]} ${it.value[1]}"
                                                    } ?: ""
                                            } + "<br/>" +
                                                "span"("class" to "snippet-hint") {
                                                    if (good) "Do." else "Don't."
                                                } +
                                                "div"("class" to "snippet-code snipper-code-${if (good) "good" else "bad"}") {
                                                    "pre" {
                                                        "code"("class" to "language-kotlin") {
                                                            code.replace("<", "&lt;")
                                                                .replace(">", "&gt;")
                                                        }
                                                    }
                                                }
                                        }
                                    }
                            }
                        }
                }
            }
        }

        val rules = "div"("id" to "rules", "class" to "ktlint-screen-section") {
            "div"("class" to "mdl-typography--display-1-color-contrast mdl-color-text--white mdl-typography--text-center ktlint-category") {
                "Domain Driven Design Ktlint-rules"
            } +
                "div"("class" to "ktlint-content") {
                    rulesContent
                }
        }

        val html = """
            <html>
              <head>
                <meta charset="utf-8">
                <meta http-equiv="X-UA-Compatible" content="IE=edge">
                <meta name="description" content="Additional ktlint-rules for domain driven design">
                <meta name="viewport" content="width=device-width, initial-scale=1.0, minimum-scale=1.0">
                <title>domain-driven-design-ktlint-rules</title>

                <link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Roboto:regular,bold,thin,light,black,medium&amp;lang=en">
                <link rel="stylesheet" href="https://fonts.googleapis.com/icon?family=Material+Icons">

                <link rel="stylesheet" href="https://code.getmdl.io/1.3.0/material.min.css">
                <script src="https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/prism.min.js"
                 crossorigin="anonymous" referrerpolicy="no-referrer"></script>
                <script src="https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/components/prism-kotlin.min.js" 
                 crossorigin="anonymous" referrerpolicy="no-referrer"></script>
                <style>
                body {
                  background-color: black;
                }
                
                h6 {
                  position: sticky;
                  top: 0
                }
                
                .padding-block {
                  width: 100%;
                  height: 100%;
                }
                
                .snippet-good {
                  color: #2e7b32;
                  font-weight: 500;
                  font-family: Roboto, Helvetica, Arial, sans-serif;
                }
                .snippet-bad {
                  color: red;
                  font-weight: 500;
                  font-family: Roboto, Helvetica, Arial, sans-serif;
                }
                
                code[class*="language-"],
                pre[class*="language-"] {
                	color: #f92aad;
                	text-shadow: 0 0 2px #100c0f, 0 0 5px #dc078e33, 0 0 10px #fff3;
                	background: none;
                	font-family: Consolas, Monaco, 'Andale Mono', 'Ubuntu Mono', monospace;
                	font-size: 1em;
                	text-align: left;
                	white-space: pre;
                	word-spacing: normal;
                	word-break: normal;
                	word-wrap: normal;
                	line-height: 1.5;

                	-moz-tab-size: 4;
                	-o-tab-size: 4;
                	tab-size: 4;

                	-webkit-hyphens: none;
                	-moz-hyphens: none;
                	-ms-hyphens: none;
                	hyphens: none;
                }

                /* Code blocks */
                pre[class*="language-"] {
                	padding: 1em;
                	margin: .5em 0;
                	overflow: auto;
                }

                :not(pre) > code[class*="language-"],
                pre[class*="language-"] {
                	background-color: transparent !important;
                	background-image: linear-gradient(to bottom, #2a2139 75%, #34294f);
                }

                /* Inline code */
                :not(pre) > code[class*="language-"] {
                	padding: .1em;
                	border-radius: .3em;
                	white-space: normal;
                }

                .token.comment,
                .token.block-comment,
                .token.prolog,
                .token.doctype,
                .token.cdata {
                	color: #8e8e8e;
                }

                .token.punctuation {
                	color: #ccc;
                }

                .token.tag,
                .token.attr-name,
                .token.namespace,
                .token.number,
                .token.unit,
                .token.hexcode,
                .token.deleted {
                	color: #e2777a;
                }

                .token.property,
                .token.selector {
                	color: #72f1b8;
                	text-shadow: 0 0 2px #100c0f, 0 0 10px #257c5575, 0 0 35px #21272475;
                }

                .token.function-name {
                	color: #6196cc;
                }

                .token.boolean,
                .token.selector .token.id,
                .token.function {
                	color: #fdfdfd;
                	text-shadow: 0 0 2px #001716, 0 0 3px #03edf975, 0 0 5px #03edf975, 0 0 8px #03edf975;
                }

                .token.class-name {
                	color: #fff5f6;
                	text-shadow: 0 0 2px #000, 0 0 10px #fc1f2c75, 0 0 5px #fc1f2c75, 0 0 25px #fc1f2c75;
                }

                .token.constant,
                .token.symbol {
                	color: #f92aad;
                	text-shadow: 0 0 2px #100c0f, 0 0 5px #dc078e33, 0 0 10px #fff3;
                }

                .token.important,
                .token.atrule,
                .token.keyword,
                .token.selector .token.class,
                .token.builtin {
                	color: #f4eee4;
                	text-shadow: 0 0 2px #393a33, 0 0 8px #f39f0575, 0 0 2px #f39f0575;
                }

                .token.string,
                .token.char,
                .token.attr-value,
                .token.regex,
                .token.variable {
                	color: #f87c32;
                }

                .token.operator,
                .token.entity,
                .token.url {
                	color: #67cdcc;
                }

                .token.important,
                .token.bold {
                	font-weight: bold;
                }

                .token.italic {
                	font-style: italic;
                }

                .token.entity {
                	cursor: help;
                }

                .token.inserted {
                	color: green;
                }
                </style>
              </head>
              <body>
                $rules
              </body>
            </html>
              """.trimIndent()

        Paths.get(
            System.getProperty("user.dir"),
            "build",
            "reports",
            "rules-doc.html"
        ).toFile().writeText(html)
    }
}