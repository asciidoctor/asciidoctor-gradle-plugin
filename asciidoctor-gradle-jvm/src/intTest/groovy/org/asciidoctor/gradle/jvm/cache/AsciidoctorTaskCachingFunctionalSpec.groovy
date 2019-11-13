package org.asciidoctor.gradle.jvm.cache

import org.asciidoctor.gradle.internal.FunctionalSpecification
import org.asciidoctor.gradle.testfixtures.jvm.CachingTest

import static org.asciidoctor.gradle.testfixtures.jvm.AsciidoctorjTestVersions.SERIES_16
import static org.asciidoctor.gradle.testfixtures.jvm.AsciidoctorjTestVersions.SERIES_20
import static org.asciidoctor.gradle.testfixtures.jvm.JRubyTestVersions.AJ16_ABSOLUTE_MINIMUM
import static org.asciidoctor.gradle.testfixtures.jvm.JRubyTestVersions.AJ16_SAFE_MAXIMUM
import static org.asciidoctor.gradle.testfixtures.jvm.JRubyTestVersions.AJ20_ABSOLUTE_MINIMUM


class AsciidoctorTaskCachingFunctionalSpec extends FunctionalSpecification implements CachingTest {
    static final String DEFAULT_TASK = 'asciidoctor'
    static final String DEFAULT_OUTPUT_FILE = 'build/docs/asciidoc/html5/sample.html'
    static final String DOCBOOK_OUTPUT_FILE = 'build/docs/asciidoc/docbook/sample.xml'

    void setup() {
        setupCache()
        createTestProject()
    }

    def "asciidoctor task is cacheable and relocatable"() {
        given:
        getBuildFile("""
            asciidoctor {
                sourceDir 'src/docs/asciidoc'
                
                outputOptions {
                    backends 'html5', 'docbook'
                }
            }
        """)

        when:
        assertDefaultTaskExecutes()

        then:
        outputFile.exists()
        file(DOCBOOK_OUTPUT_FILE).exists()

        when:
        assertDefaultTaskIsCachedAndRelocatable()

        then:
        outputFile.exists()
        file(DOCBOOK_OUTPUT_FILE).exists()
        outputFileInRelocatedDirectory.exists()
        fileInRelocatedDirectory(DOCBOOK_OUTPUT_FILE).exists()
    }

    def "asciidoctor task is cached when only output directory is changed"() {
        given:
        getBuildFile("""
            asciidoctor {
                sourceDir 'src/docs/asciidoc'
                
                outputOptions {
                    backends 'html5', 'docbook'
                }
            }
        """)

        when:
        assertDefaultTaskExecutes()

        then:
        outputFile.exists()

        when:
        changeBuildConfigurationTo("""
            asciidoctor {
                sourceDir 'src/docs/asciidoc'
                outputDir 'build/asciidoc'
                
                outputOptions {
                    backends 'html5', 'docbook'
                }
            }
        """)

        then:
        assertDefaultTaskIsCachedAndRelocatable()

        and:
        file('build/asciidoc/html5/sample.html').exists()
        file('build/asciidoc/docbook/sample.xml').exists()

        and:
        fileInRelocatedDirectory('build/asciidoc/html5/sample.html').exists()
        fileInRelocatedDirectory('build/asciidoc/docbook/sample.xml').exists()
    }

    def "asciidoctor task is not cached when backends change"() {
        given:
        getBuildFile("""
            asciidoctor {
                sourceDir 'src/docs/asciidoc'
                
                outputOptions {
                    backends 'html5', 'html'
                }
            }
        """)

        when:
        assertDefaultTaskExecutes()

        then:
        outputFile.exists()

        when:
        changeBuildConfigurationTo("""
            asciidoctor {
                sourceDir 'src/docs/asciidoc'
                
                outputOptions {
                    backends 'html5', 'docbook'
                }
            }
        """)

        then:
        assertDefaultTaskExecutes()

        then:
        assertDefaultTaskIsCachedAndRelocatable()
    }

    def "asciidoctor task is not cached when asciidoctorj/jruby versions change"() {
        given:
        getBuildFile("""
            asciidoctorj {
                version = '${SERIES_20}'
                jrubyVersion = '${AJ20_ABSOLUTE_MINIMUM}'
            }
            asciidoctor {
                sourceDir 'src/docs/asciidoc'
                
                outputOptions {
                    backends 'html5', 'docbook'
                }
            }
        """)

        when:
        assertDefaultTaskExecutes()

        then:
        outputFile.exists()

        when:
        changeBuildConfigurationTo("""
            asciidoctorj {
                version = '${SERIES_16}'
                jrubyVersion = '${AJ16_ABSOLUTE_MINIMUM}'
            }
            
            asciidoctor {
                sourceDir 'src/docs/asciidoc'
                
                outputOptions {
                    backends 'html5', 'docbook'
                }
            }
        """)

        then:
        assertDefaultTaskExecutes()

        then:
        assertDefaultTaskIsCachedAndRelocatable()
    }

    def "asciidoctor task is not cached when attributes change"() {
        given:
        getBuildFile("""
            asciidoctor {
                sourceDir 'src/docs/asciidoc'
                
                outputOptions {
                    backends 'html5', 'docbook'
                }
                
                attributes 'source-highlighter': 'coderay',
                            'imagesdir': 'images',
                            'toc': 'left',
                            'icons': 'font',
                            'setanchors': '',
                            'idprefix': '',
                            'idseparator': '-'
            }
        """)

        when:
        assertDefaultTaskExecutes()

        then:
        outputFile.exists()

        when:
        changeBuildConfigurationTo("""
            asciidoctor {
                sourceDir 'src/docs/asciidoc'
                
                outputOptions {
                    backends 'html5', 'docbook'
                }
                
                attributes 'source-highlighter': 'coderay',
                            'imagesdir': 'images',
                            'toc': 'right',
                            'icons': 'font',
                            'setanchors': '',
                            'idprefix': '',
                            'idseparator': '--'
            }
        """)

        then:
        assertDefaultTaskExecutes()

        then:
        assertDefaultTaskIsCachedAndRelocatable()
    }

    @Override
    File getOutputFile() {
        return file(DEFAULT_OUTPUT_FILE)
    }

    @Override
    String getDefaultTask() {
        return ":${DEFAULT_TASK}"
    }

    File getBuildFile(String extraContent) {
        getJvmConvertGroovyBuildFile("""
            ${-> scan ? buildScanConfiguration : ""}

            asciidoctorj {
                jrubyVersion = '${AJ16_SAFE_MAXIMUM}'
            }
            
            ${extraContent}
        """)
    }
}
