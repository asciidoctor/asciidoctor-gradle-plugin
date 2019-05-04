/*
 * Copyright 2013-2019 the original author or authors.
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
package org.asciidoctor.gradle.compat

import org.asciidoctor.gradle.internal.FunctionalSpecification
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome

import java.util.regex.Pattern

/**
 * This was first functional specification. It is now the functional sepcification to ensure that
 * the compatibility plugin and task in 2.x still functions correctly.
 *
 * @author Peter Ledbrook
 * @author Schalk W. Cronjé
 */
class AsciidoctorFunctionalSpec extends FunctionalSpecification {

    static final String ASCIIDOCTOR_TASK = 'asciidoctor'
    static final String ASCIIDOCTOR_PATH = ':asciidoctor'
    static final String ASCIIDOC_SAMPLE_FILE = 'sample.asciidoc'
    static final String ASCIIDOC_INVALID_FILE = 'subdir/_include.adoc'
    static final Pattern DOCINFO_FILE_PATTERN = ~/^(.+\-)?docinfo(-footer)?\.[^.]+$/

    File buildDir

    void setup() {
        createTestProject('normal')
        buildDir = new File(testProjectDir.root, 'build')
    }

    void 'Should do nothing with an empty project'() {
        given: 'A minimal build file'
        getBuildFile('''
        asciidoctor {
            sourceDir 'non-existing'
        }
''')

        when:
        BuildResult result = runGradle()

        then:
        result.task(ASCIIDOCTOR_PATH).outcome == TaskOutcome.NO_SOURCE
    }

    void 'Should build normally for a standard project'() {
        given: 'A minimal build file'
        getBuildFile('')

        when:
        BuildResult result = runGradle()

        then:
        result.task(ASCIIDOCTOR_PATH).outcome == TaskOutcome.SUCCESS
        new File(buildDir, 'asciidoc/html5/sample.html').exists()
        new File(buildDir, 'asciidoc/html5/subdir/sample2.html').exists()
    }

    void 'Task should be up-to-date when executed a second time'() {
        given: 'A minimal build file'
        getBuildFile('')

        when:
        runGradle()
        BuildResult result = runGradle()

        then:
        result.task(ASCIIDOCTOR_PATH).outcome == TaskOutcome.UP_TO_DATE
    }

    void 'Task should not be up-to-date when classpath is changed'() {
        given: 'A minimal build file'
        getBuildFile('''
        if (project.hasProperty('modifyClasspath')) {
            dependencies {
                asciidoctor 'org.jsoup:jsoup:1.11.2'
            }
        }
        ''')

        when:
        runGradle()
        BuildResult result = getGradleRunner([ASCIIDOCTOR_TASK, '-PmodifyClasspath']).build()

        then:
        result.task(ASCIIDOCTOR_PATH).outcome == TaskOutcome.SUCCESS
    }

    void 'Should build normally for a standard project with multiple backends'() {
        given: 'A minimal build file'
        getBuildFile('''
        asciidoctor {
            backends 'html5', 'docbook'
        }
        ''')

        when:
        BuildResult result = runGradle()

        then:
        verifyAll {
            result.task(ASCIIDOCTOR_PATH).outcome == TaskOutcome.SUCCESS
            new File(buildDir, 'asciidoc/html5/sample.html').exists()
            new File(buildDir, 'asciidoc/html5/subdir/sample2.html').exists()
            new File(buildDir, 'asciidoc/docbook/sample.xml').exists()
            new File(buildDir, 'asciidoc/docbook/subdir/sample2.xml').exists()
        }
    }

    void 'Processes a single document given a value for sourceDocumentName'() {
        given:
        getBuildFile("""
        asciidoctor {
            sources {
                include '${ASCIIDOC_SAMPLE_FILE}'
            }
        }
        """)

        when:
        runGradle()

        then:
        new File(buildDir, 'asciidoc/html5/sample.html').exists()
        !new File(buildDir, 'asciidoc/html5/subdir/sample2.html').exists()
    }

    void 'Docinfo files are not copied to target directory'() {
        given:
        getBuildFile('')

        when:
        runGradle(['asciidoctor', '-i'])

        then:
        !new File(buildDir, 'asciidoctor/html5').listFiles({
            !it.directory && !(it.name =~ DOCINFO_FILE_PATTERN)
        } as FileFilter)
    }

    void 'When resources not specified, then copy all images to backend'() {
        given:
        getBuildFile("""
        asciidoctor {
            sources {
                include '${ASCIIDOC_SAMPLE_FILE}'
            }
        }
        """)

        when:
        runGradle()

        then:
        new File(buildDir, 'asciidoc/html5/images/fake.txt').exists()
        new File(buildDir, 'asciidoc/html5/images/fake2.txt').exists()
    }

    void 'When resources are specified, then copy images according to patterns'() {
        given:
        getBuildFile("""
        asciidoctor {
            sources {
                include '${ASCIIDOC_SAMPLE_FILE}'
            }
            resources {
                from sourceDir, {
                    include 'images/fake2.txt'
                }
            }
        }
        """)

        when:
        runGradle()

        then:
        verifyAll {
            !new File(buildDir, 'asciidoc/html5/images/fake.txt').exists()
            new File(buildDir, 'asciidoc/html5/images/fake2.txt').exists()
            new File(buildDir, 'asciidoc/html5/sample.html').exists()
        }
    }

    void 'Will not process file if it starts with underscore'() {
        given:
        getBuildFile("""
        asciidoctor {
            sources {
                include '${ASCIIDOC_INVALID_FILE}'
            }
        }
        """)

        when:
        BuildResult result = runGradle()

        then:
        verifyAll {
            result.task(ASCIIDOCTOR_PATH).outcome == TaskOutcome.NO_SOURCE
            !new File(buildDir, 'asciidoc/html5/subdir/_include.html').exists()
        }
    }

    File getBuildFile(final String extraContent = '') {
        getJvmConvertGroovyBuildFile("""
            asciidoctorj.noDefaultRepositories = true

            ${extraContent}
            """,
            'org.asciidoctor.convert'
        )
    }

    BuildResult runGradle(List<String> args = ['asciidoctor']) {
        getGradleRunner(args).build()
    }

    BuildResult failedGradle(List<String> args = ['asciidoctor', '-i']) {
        getGradleRunner(args).buildAndFail()
    }
}
