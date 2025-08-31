/*
 * Copyright 2013 - 2025 the original author or authors.
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
package org.asciidoctor.gradle.model5.core


import org.asciidoctor.gradle.model5.core.internal.basedir.BaseDirFollowSourceDir
import org.asciidoctor.gradle.model5.core.internal.basedir.BaseDirFollowSourceFiles
import org.asciidoctor.gradle.model5.core.internal.basedir.BaseDirFollowsProject
import org.asciidoctor.gradle.model5.core.internal.basedir.BaseDirFollowsRootProject
import org.asciidoctor.gradle.model5.core.internal.basedir.BaseDirIsFixedPath
import org.asciidoctor.gradle.model5.core.plugins.AsciidoctorCorePlugin
import org.asciidoctor.gradle.model5.core.publications.AsciidoctorPublication
import org.asciidoctor.gradle.model5.core.publications.AsciidoctorSourceSet
import org.asciidoctor.gradle.testfixtures.model5.UnitTestSpecification

import java.time.LocalDateTime

import static org.asciidoctor.gradle.model5.core.internal.publications.PublicationUtils.DEFAULT_PUBLICATION

class SourceSetSpec extends UnitTestSpecification {

    AsciidoctorPublication mainPublication
    AsciidoctorSourceSet main

    void setup() {
        project.pluginManager.apply(AsciidoctorCorePlugin)
        mainPublication = project.extensions
                .getByType(AsciidoctorModelExtension)
                .publications
                .getByName(DEFAULT_PUBLICATION)

        main = mainPublication.sourceSet
    }

    void 'Base directory is source directory by default'() {
        setup:
        final bd = main.baseDir.baseDirStrategy.get()
        final srcDir = main.sourceDir

        expect:
        bd instanceof BaseDirFollowSourceDir
        bd.getBaseDir(srcDir).get() == srcDir.get()
        !bd.adjustBaseDirPerFile.get()
    }

    void 'Base directory can be root project directory'() {
        setup:
        final srcDir = main.sourceDir

        when:
        main.baseDir.baseDirIsRootProjectDir()
        final bd = main.baseDir.baseDirStrategy.get()

        then:
        bd instanceof BaseDirFollowsRootProject
        bd.getBaseDir(srcDir).get().asFile == project.rootDir
        !bd.adjustBaseDirPerFile.get()
    }

    void 'Base directory can be project directory'() {
        setup:
        final srcDir = main.sourceDir

        when:
        main.baseDir.baseDirIsProjectDir()
        final bd = main.baseDir.baseDirStrategy.get()

        then:
        bd instanceof BaseDirFollowsProject
        bd.getBaseDir(srcDir).get().asFile == project.projectDir
        !bd.adjustBaseDirPerFile.get()
    }

    void 'Base directory can be fixed directory'() {
        setup:
        final srcDir = main.sourceDir
        final fooDir = new File(projectDir, 'foo')

        when:
        main.baseDir.baseDir = fooDir
        final bd = main.baseDir.baseDirStrategy.get()

        then:
        bd instanceof BaseDirIsFixedPath
        bd.getBaseDir(srcDir).get().asFile == fooDir
        !bd.adjustBaseDirPerFile.get()
    }

    void 'Base directory can follow source files'() {
        setup:
        final srcDir = main.sourceDir

        when:
        main.baseDir.baseDirFollowSourceFiles()
        final bd = main.baseDir.baseDirStrategy.get()

        then:
        bd instanceof BaseDirFollowSourceFiles
        bd.getBaseDir(srcDir).get().asFile == srcDir.get().asFile
        bd.adjustBaseDirPerFile.get()
    }

    void 'Can set source directory'() {
        when:
        final initValue = main.sourceDir.get().asFile

        then:
        initValue == new File(project.projectDir, 'src/docs/asciidoc')

        when:
        main.sourceDir = 'foo'

        then:
        main.sourceDir.get().asFile == new File(project.projectDir, 'foo')
    }

    void 'Can set attributes'() {
        when:
        main.attributes {
            add('simple', 'value')
            addAll([simple2: 'value2'])
            addAsDate('date1', LocalDateTime.now())
            addAsTime('time1', LocalDateTime.now())
            addAsBoolean('bool1', 33)
            addAll([
                    simple3: project.provider { -> 'value3' }
            ])
            attributeProvider(project.provider { ->
                [simple4: 'value4']
            })
        }

        final attrs = main.attributes.attributeResolver.get().findAll { k, v ->
            !k.startsWith('gradle')
        }

        then:
        attrs.keySet().size() == 7
        attrs.values().containsAll(['value3', 'true'])
    }

    void 'Output directory depends on publication name'() {
        setup:
        final secondPublication = project.extensions
                .getByType(AsciidoctorModelExtension)
                .publications
                .create('second')

        expect:
        mainPublication.outputPath('html') == 'docs/asciidoc/html'
        secondPublication.outputPath('html') == 'docs/asciidocSecond/html'
    }
}