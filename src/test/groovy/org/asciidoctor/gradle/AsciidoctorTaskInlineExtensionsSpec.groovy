/*
 * Copyright 2013-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.asciidoctor.gradle

import org.asciidoctor.SafeMode
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.internal.file.collections.SimpleFileCollection
import org.gradle.testfixtures.ProjectBuilder
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import spock.lang.Specification

/**
 * Asciidoctor task inline extensions specification
 *
 * @author Robert Panzer
 */
class AsciidoctorTaskInlineExtensionsSpec extends Specification {
	private static final String ASCIIDOCTOR = 'asciidoctor'
	private static final String ASCIIDOC_RESOURCES_DIR = 'build/resources/test/src/asciidocextensions'
	private static final String ASCIIDOC_BUILD_DIR = 'build/asciidocextensions'
	private static final String ASCIIDOC_MACRO_EXTENSION_SCRIPT = 'blockMacro.groovy'
	private static final String ASCIIDOC_INLINE_EXTENSIONS_FILE = 'inlineextensions.asciidoc'
	private static final String ASCIIDOC_TREEPROCESSOR_EXTENSIONS_FILE = 'sample-with-terminal-command.ad'
	private static final String ASCIIDOC_INLINE_EXTENSIONS_RESULT_FILE = 'inlineextensions.html'
	private static final String ASCIIDOC_TREEPROCESSOR_EXTENSIONS_RESULT_FILE = 'sample-with-terminal-command.html'
	private static final DOCINFO_FILE_PATTERN = ~/^(.+\-)?docinfo(-footer)?\.[^.]+$/

	Project project
	File testRootDir

	def setup() {
		project = ProjectBuilder.builder().withName('test').build()
                project.configurations.create(ASCIIDOCTOR)
		testRootDir = new File('.')
	}


	def "Should apply inline BlockProcessor"() {
		given:
		Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
			sourceDir = new File(testRootDir, ASCIIDOC_RESOURCES_DIR)
			sourceDocumentName = new File(new File(testRootDir, ASCIIDOC_RESOURCES_DIR), ASCIIDOC_INLINE_EXTENSIONS_FILE)
			outputDir = new File(testRootDir, ASCIIDOC_BUILD_DIR)
			extensions {
				block(name: "BIG", contexts: [":paragraph"]) {
					parent, reader, attributes ->
					def upperLines = reader.readLines()
					.collect {it.toUpperCase()}
					.inject("") {a, b -> a + '\n' + b}

					createBlock(parent, "paragraph", [upperLines], attributes, [:])
				}
				block("small") {
					parent, reader, attributes ->
					def lowerLines = reader.readLines()
					.collect {it.toLowerCase()}
					.inject("") {a, b -> a + '\n' + b}

					createBlock(parent, "paragraph", [lowerLines], attributes, [:])
				}

			}
		}
		File resultFile = new File(testRootDir, ASCIIDOC_BUILD_DIR + File.separator + ASCIIDOC_INLINE_EXTENSIONS_RESULT_FILE)
		when:
		task.processAsciidocSources()
		then:
		resultFile.exists()
		resultFile.getText().contains("WRITE THIS IN UPPERCASE")
		resultFile.getText().contains("and write this in lowercase")
	}

	def "Should apply BlockProcessor from file"() {
		given:
                
                print project.files('src/test/resources/src/asciidocextensions/blockMacro.groovy').each {println ">>> $it"}
		Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
			sourceDir = new File(testRootDir, ASCIIDOC_RESOURCES_DIR)
			sourceDocumentName = new File(sourceDir, ASCIIDOC_INLINE_EXTENSIONS_FILE)
			outputDir = new File(testRootDir, ASCIIDOC_BUILD_DIR)
			extensions new File(sourceDir, ASCIIDOC_MACRO_EXTENSION_SCRIPT)
		}
		File resultFile = new File(testRootDir, ASCIIDOC_BUILD_DIR + File.separator + ASCIIDOC_INLINE_EXTENSIONS_RESULT_FILE)
		when:
		task.processAsciidocSources()
		then:
		resultFile.exists()
		resultFile.getText().contains("WRITE THIS IN UPPERCASE")
		resultFile.getText().contains("and write this in lowercase")
	}
    
    
	def "Should apply inline Postprocessor"() {
		given:
		String copyright = "Copyright Acme, Inc." + System.currentTimeMillis()
		Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
			sourceDir = new File(testRootDir, ASCIIDOC_RESOURCES_DIR)
			sourceDocumentName = new File(new File(testRootDir, ASCIIDOC_RESOURCES_DIR), ASCIIDOC_INLINE_EXTENSIONS_FILE)
			outputDir = new File(testRootDir, ASCIIDOC_BUILD_DIR)
			extensions {
				postprocessor {
					document, output ->
					if(document.basebackend("html")) {
						org.jsoup.nodes.Document doc = Jsoup.parse(output, "UTF-8")

						Element contentElement = doc.getElementById("footer-text")
						contentElement.append(copyright)

						output = doc.html()
					} else {
						throw new IllegalArgumentException("Expected html!")
					}
				}
			}
		}
		File resultFile = new File(testRootDir, ASCIIDOC_BUILD_DIR + File.separator + ASCIIDOC_INLINE_EXTENSIONS_RESULT_FILE)
		when:
		task.processAsciidocSources()
		then:
		resultFile.exists()
		resultFile.getText().contains(copyright)
		resultFile.getText().contains("Inline Extension Test document")

	}

	def "Should fail if inline Postprocessor fails"() {
		given:
		String copyright = "Copyright Acme, Inc." + System.currentTimeMillis()
		Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
			sourceDir = new File(testRootDir, ASCIIDOC_RESOURCES_DIR)
			sourceDocumentName = new File(new File(testRootDir, ASCIIDOC_RESOURCES_DIR), ASCIIDOC_INLINE_EXTENSIONS_FILE)
			outputDir = new File(testRootDir, ASCIIDOC_BUILD_DIR)
			extensions {
				postprocessor {
					document, output ->
					if (output.contains("blacklisted")) {
						throw new IllegalArgumentException("Document contains a blacklisted word")
					}
				}
			}
		}
		File resultFile = new File(testRootDir, ASCIIDOC_BUILD_DIR + File.separator + ASCIIDOC_INLINE_EXTENSIONS_RESULT_FILE)
		when:
		task.processAsciidocSources()
		then:
		thrown(GradleException)
	}

	def "Should apply inline Preprocessor"() {
		given:
		Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
			sourceDir = new File(testRootDir, ASCIIDOC_RESOURCES_DIR)
			sourceDocumentName = new File(new File(testRootDir, ASCIIDOC_RESOURCES_DIR), ASCIIDOC_INLINE_EXTENSIONS_FILE)
			outputDir = new File(testRootDir, ASCIIDOC_BUILD_DIR)
			extensions {
				preprocessor {
					document, reader ->
					reader.advance()
					reader
				}
			}
		}
		File resultFile = new File(testRootDir, ASCIIDOC_BUILD_DIR + File.separator + ASCIIDOC_INLINE_EXTENSIONS_RESULT_FILE)
		when:
		task.processAsciidocSources()
		then:
		resultFile.exists()
		!resultFile.getText().contains("Inline Extension Test document")
	}

	def "Should apply inline Includeprocessor"() {
		given:
		String content = "The content of the URL " + System.currentTimeMillis()
		Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
			sourceDir = new File(testRootDir, ASCIIDOC_RESOURCES_DIR)
			sourceDocumentName = new File(new File(testRootDir, ASCIIDOC_RESOURCES_DIR), ASCIIDOC_INLINE_EXTENSIONS_FILE)
			outputDir = new File(testRootDir, ASCIIDOC_BUILD_DIR)
			extensions {
				includeprocessor (filter: {it.startsWith("http")}) {
					document, reader, target, attributes ->
					reader.push_include(content, target, target, 1, attributes);					}
			}
		}
		File resultFile = new File(testRootDir, ASCIIDOC_BUILD_DIR + File.separator + ASCIIDOC_INLINE_EXTENSIONS_RESULT_FILE)
		when:
		task.processAsciidocSources()
		then:
		resultFile.exists()
		resultFile.getText().contains(content)
	}

	def "Should apply inline BlockMacroProcessor"() {
		given:
		Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
			sourceDir = new File(testRootDir, ASCIIDOC_RESOURCES_DIR)
			sourceDocumentName = new File(new File(testRootDir, ASCIIDOC_RESOURCES_DIR), ASCIIDOC_INLINE_EXTENSIONS_FILE)
			outputDir = new File(testRootDir, ASCIIDOC_BUILD_DIR)
			extensions {
				blockMacro (name: "gist") {
					parent, target, attributes ->
					String content = """<div class="content"> 
<script src="https://gist.github.com/${target}.js"></script> 
</div>"""
					createBlock(parent, "pass", [content], attributes, config);
				}

			}
		}
		File resultFile = new File(testRootDir, ASCIIDOC_BUILD_DIR + File.separator + ASCIIDOC_INLINE_EXTENSIONS_RESULT_FILE)
		when:
		task.processAsciidocSources()
		then:
		resultFile.exists()
		resultFile.getText().contains("https://gist.github.com/123456.js")
	}

	def "Should apply inline InlineMacroProcessor"() {
		given:
		Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
			sourceDir = new File(testRootDir, ASCIIDOC_RESOURCES_DIR)
			sourceDocumentName = new File(new File(testRootDir, ASCIIDOC_RESOURCES_DIR), ASCIIDOC_INLINE_EXTENSIONS_FILE)
			outputDir = new File(testRootDir, ASCIIDOC_BUILD_DIR)
			extensions {
				inlineMacro (name: "man") {
					parent, target, attributes ->
					options=["type": ":link", "target": target + ".html"]
					createInline(parent, "anchor", target, attributes, options).render()
				}
			}
		}
		File resultFile = new File(testRootDir, ASCIIDOC_BUILD_DIR + File.separator + ASCIIDOC_INLINE_EXTENSIONS_RESULT_FILE)
		when:
		task.processAsciidocSources()
		then:
		resultFile.exists()
		resultFile.getText().contains('<a href="gittutorial.html">gittutorial</a>')
	}

	def "Should apply inline TreeProcessor"() {
		given:
		Task task = project.tasks.create(name: ASCIIDOCTOR, type: AsciidoctorTask) {
			sourceDir = new File(testRootDir, ASCIIDOC_RESOURCES_DIR)
			sourceDocumentName = new File(new File(testRootDir, ASCIIDOC_RESOURCES_DIR), ASCIIDOC_TREEPROCESSOR_EXTENSIONS_FILE)
			outputDir = new File(testRootDir, ASCIIDOC_BUILD_DIR)
			extensions {
				treeprocessor {
					document ->
					List blocks = document.blocks()
					(0..<blocks.length).each {
						def block = blocks[it]
						def lines = block.lines()
						if (lines.size() > 0 && lines[0].startsWith('$')) {
							Map attributes = block.attributes()
							attributes["role"] = "terminal";
							def resultLines = lines.collect {
								it.startsWith('$') ? "<span class=\"command\">${it.substring(2)}</span>" : it
							}
							blocks[it] = createBlock(document, "listing", resultLines, attributes,[:])
						}
					}
				}
			}
		}
		File resultFile = new File(testRootDir, ASCIIDOC_BUILD_DIR + File.separator + ASCIIDOC_TREEPROCESSOR_EXTENSIONS_RESULT_FILE)
		when:
			task.processAsciidocSources()
		then:
			resultFile.exists()
			resultFile.getText().contains('<span class="command">')
	}
}
