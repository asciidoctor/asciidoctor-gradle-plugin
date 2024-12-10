package org.asciidoctor.gradle.model5.core


import org.asciidoctor.gradle.testfixtures.UnitTestSpecification

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AsciidoctorPublicationSpec extends UnitTestSpecification {

    AsciidoctorPublication main

    void setup() {
        project.pluginManager.apply(AsciidoctorCorePlugin)
        main = project.extensions.getByType(AsciidoctorCoreExtension).publications.create('main')
    }

    void 'Can set a safe mode'() {
        when:
        main.processingOptions.safeMode = 'sAfe'

        then:
        main.processingOptions.safeMode.get() == SafeMode.SAFE

        when:
        main.processingOptions.safeMode = 10

        then:
        main.processingOptions.safeMode.get() == SafeMode.SERVER

        when:
        main.processingOptions.safeMode = SafeMode.SECURE

        then:
        main.processingOptions.safeMode.get() == SafeMode.SECURE

        when:
        main.processingOptions.safeMode = project.provider { -> SafeMode.UNSAFE }

        then:
        main.processingOptions.safeMode.get() == SafeMode.UNSAFE
    }

    void 'Can set attributes'() {
        setup:
        final now = LocalDateTime.now()

        when:
        main.attributes {
            replaceAll([abc: 123])
            add([defg: 'a-string'])
            add('hij', true)
            add('no-hij', false)
            attributeProvider { -> [abc: 456] }
            attributeProvider { -> [klm: asDate(now)] }
            attributeProvider { -> [opq: asTime(now)] }
        }
        final result = main.attributes.attributeResolver.get()

        then:
        result.abc == '456'
        result.defg == 'a-string'
        result.hij.empty
        result.'no-hij' == null
        result.klm == now.format(DateTimeFormatter.ISO_DATE)
        result.opq == now.format(DateTimeFormatter.ISO_TIME)
    }

    void 'Can configure publication language'() {
        when:
        project.allprojects {
            asciidoc {
                publications {
                    main {
                        languages {
                            fr {
                                attributes {
                                    replaceAll([abc: 123])
                                }
                            }
                        }
                    }
                }
            }
        }

        then:
        main.languages.getByName('fr').attributes.attributeResolver.get() == [abc: '123']
    }

    void 'Can set source directory'() {
        when:
        final initValue = main.sourceDir.get()

        then:
        initValue == new File(project.projectDir,'src/docs/asciidoc')

        when:
        main.sourceDir = 'foo'

        then:
        main.sourceDir.get() == new File(project.projectDir,'foo')
    }

//    void 'Can configure sources'() {
//        main.sources()
//    }
//    void "Should include patterns passed to sources method"() {
//        when:
//        def task1 = createTask('task') {
//            sources('myfile.adoc', 'otherfile.adoc')
//        }
//        then:
//        task1.internalSourceDocumentPattern.includes == ['myfile.adoc', 'otherfile.adoc'] as Set
//    }
//
//    void "Should support clearing configured patterns"() {
//        given:
//        def task = createTask('task') {
//            sources('myfile.adoc', 'otherfile.adoc')
//        }
//        when:
//        task.clearSources()
//        then:
//        task.internalSourceDocumentPattern == null
//    }
//
//    void "Should support replacing the configured patterns"() {
//        given:
//        def task = createTask('task') {
//            sources('myfile.adoc', 'otherfile.adoc')
//        }
//        when:
//        task.clearSources()
//        task.sources('myfile2.adoc', 'myfile3.adoc')
//        then:
//        task.internalSourceDocumentPattern.includes == ['myfile2.adoc', 'myfile3.adoc'] as Set
//    }

//    void 'Can set language-specific attributes at project level'() {
//        when:
//        projectExtension.attributesForLang EN, foo: 'bar'
//
//        then:
//        projectExtension.getAttributesForLang(ES).isEmpty()
//        projectExtension.getAttributesForLang(EN).foo == 'bar'
//    }
//
//    void 'Can augment language-specific attributes at task level'() {
//        when:
//        projectExtension.attributesForLang EN, foo: 'bar'
//        taskExtension.attributesForLang EN, foo2: 'bar2'
//
//        then:
//        taskExtension.getAttributesForLang(ES).isEmpty()
//        taskExtension.getAttributesForLang(EN).foo.call() == 'bar'
//        taskExtension.getAttributesForLang(EN).foo2.call() == 'bar2'
//        !projectExtension.getAttributesForLang(EN).foo2
//    }
//
//    void 'Can reset language-specific attributes at task level'() {
//        when:
//        projectExtension.attributesForLang EN, foo: 'bar'
//        taskExtension.resetAttributesForLang EN, foo2: 'bar2'
//
//        then:
//        taskExtension.getAttributesForLang(ES).isEmpty()
//        !taskExtension.getAttributesForLang(EN).foo
//        taskExtension.getAttributesForLang(EN).foo2.call() == 'bar2'
//        !projectExtension.getAttributesForLang(EN).foo2
//    }
//
//    void 'Set single attribute'() {
//        when:
//        projectExtension.attribute('name', 'value')
//
//        then:
//        projectExtension.attributes['name'].call() == 'value'
//    }

//    void 'Integers map to correct safe modes'() {
//        expect:
//        safeMode(0) == UNSAFE
//        safeMode(1) == SAFE
//        safeMode(10) == SERVER
//        safeMode(20) == SECURE
//        safeMode(35) == SECURE
//
//        SAFE.level == 1
//    }
}