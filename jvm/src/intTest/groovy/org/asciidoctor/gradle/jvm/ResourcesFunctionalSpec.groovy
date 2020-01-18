/*
 * Copyright 2013-2020 the original author or authors.
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
package org.asciidoctor.gradle.jvm

@java.lang.SuppressWarnings('NoWildcardImports')
import org.asciidoctor.gradle.internal.FunctionalSpecification
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Timeout
import spock.lang.Unroll

class ResourcesFunctionalSpec extends FunctionalSpecification {

    static final List DEFAULT_ARGS = ['asciidoctor', '-s']
    static final String ASCIIDOC_FILE = 'sample.asciidoc'

    void setup() {
        createTestProject('resources')
    }

    @Timeout(value = 90)
    @Unroll
    @SuppressWarnings('LineLength')
    void 'When resources are not specified, copy all images to destination with intermediate workdir=#intermediate)'() {
        given:
        getBuildFile("""
        if(${intermediate}) {
            asciidoctor.useIntermediateWorkDir()
        }
        """)
        GradleRunner runner = getGradleRunner(DEFAULT_ARGS)
        File buildDir = new File(testProjectDir.root, 'build/docs/asciidoc')
        File imagesDir = new File(buildDir, 'images')

        when:
        runner.build()

        then:
        imagesDir.exists()
        verifyAll {
            containsFile(imagesDir, 'fake11.txt')
            containsFile(imagesDir, 'fake12.txt')
        }

        where:
        intermediate << [false, true]
    }

    @Timeout(value = 90)
    void 'Alternative resources can be used'() {
        given:
        getBuildFile('''
        asciidoctor {
            resources {
                from sourceDir, {
                    include 'images2/**'
                }
            }
        }
        ''')
        GradleRunner runner = getGradleRunner(DEFAULT_ARGS)
        File buildDir = new File(testProjectDir.root, 'build/docs/asciidoc')
        File imagesDir = new File(buildDir, 'images')
        File extraDir = new File(buildDir, 'images2')

        when:
        runner.build()

        then:
        verifyAll {
            !imagesDir.exists()
            extraDir.exists()
        }
        verifyAll {
            !containsFile(imagesDir, 'fake11.txt')
            !containsFile(imagesDir, 'fake12.txt')
            containsFile(extraDir, 'fake2.txt')
        }
    }

    @Timeout(value = 90)
    void 'Images can be copied selectively'() {
        given:
        getBuildFile('''
        asciidoctor {
            resources {
                from sourceDir, {
                    include 'images/fake*2.txt'
                }
            }
        }
        ''')
        GradleRunner runner = getGradleRunner(DEFAULT_ARGS)
        File buildDir = new File(testProjectDir.root, 'build/docs/asciidoc')
        File imagesDir = new File(buildDir, 'images')

        when:
        runner.build()

        then:
        imagesDir.exists()
        verifyAll {
            !containsFile(imagesDir, 'fake11.txt')
            containsFile(imagesDir, 'fake12.txt')
        }
    }

    @Timeout(value = 90)
    void 'When two backends are specified, copy resources to both backends'() {
        given:
        getBuildFile('''
        asciidoctor {
            outputOptions {
                backends 'html5', 'docbook'
            }
        }
        ''')
        GradleRunner runner = getGradleRunner(DEFAULT_ARGS)
        File buildDir = new File(testProjectDir.root, 'build/docs/asciidoc')
        File htmlImagesDir = new File(buildDir, 'html5/images')
        File docbookImagesDir = new File(buildDir, 'docbook/images')

        when:
        runner.build()

        then:
        verifyAll {
            htmlImagesDir.exists()
            docbookImagesDir.exists()
        }
        verifyAll {
            containsFile(htmlImagesDir, 'fake11.txt')
            containsFile(docbookImagesDir, 'fake11.txt')
        }
    }

    @Timeout(value = 90)
    void 'It is possible to build a document without copying resources'() {
        given:
        getBuildFile('''
        asciidoctor {
            copyNoResources()
        }
        ''')
        GradleRunner runner = getGradleRunner(DEFAULT_ARGS)
        File buildDir = new File(testProjectDir.root, 'build/docs/asciidoc')
        File imagesDir = new File(buildDir, 'images')

        when:
        runner.build()

        then:
        verifyAll {
            !containsFile(imagesDir, 'fake11.txt')
            !containsFile(imagesDir, 'fake12.txt')
            !imagesDir.exists()
        }
    }

    // 'Resources can be copied on a per-backend-basis'
    @Timeout(value = 90)
    void 'Resources can be copied on a per-backend-basis'() {
        given:
        getBuildFile('''
        asciidoctor {
            outputOptions {
                backends 'html5', 'docbook'
            }
            copyResourcesOnlyIf('html5')
        }
        ''')
        GradleRunner runner = getGradleRunner(DEFAULT_ARGS)
        File buildDir = new File(testProjectDir.root, 'build/docs/asciidoc')
        File htmlImagesDir = new File(buildDir, 'html5/images')
        File docbookImagesDir = new File(buildDir, 'docbook/images')

        when:
        runner.build()

        then:
        verifyAll {
            htmlImagesDir.exists()
            !docbookImagesDir.exists()
        }
        verifyAll {
            containsFile(htmlImagesDir, 'fake11.txt')
        }
    }

    File getBuildFile(final String extraContent) {
        getJvmConvertGroovyBuildFile("""
            ${extraContent}
        """
        )
    }

    boolean containsFile(File dir, String name) {
        new File(dir, name).exists()
    }
}
