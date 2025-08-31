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

import org.asciidoctor.gradle.model5.core.basedir.BaseDirStrategy
import org.asciidoctor.gradle.model5.core.internal.basedir.BaseDirFollowSourceDir
import org.asciidoctor.gradle.model5.core.internal.basedir.BaseDirFollowsProject
import org.asciidoctor.gradle.model5.core.internal.basedir.BaseDirFollowsRootProject
import org.asciidoctor.gradle.model5.core.internal.basedir.BaseDirIsFixedPath
import org.asciidoctor.gradle.model5.core.plugins.AsciidoctorCorePlugin
import org.asciidoctor.gradle.model5.core.publications.AsciidoctorSourceSet
import org.asciidoctor.gradle.testfixtures.model5.UnitTestSpecification

import static org.asciidoctor.gradle.model5.core.internal.publications.PublicationUtils.DEFAULT_PUBLICATION

class SourceSetSpec extends UnitTestSpecification {

    AsciidoctorSourceSet main

    void setup() {
        project.pluginManager.apply(AsciidoctorCorePlugin)
        main = project.extensions
                .getByType(AsciidoctorModelExtension)
                .publications
                .getByName(DEFAULT_PUBLICATION)
                .sourceSet
    }

    void 'Base directory is source directory by default'() {
        setup:
        final bd = main.baseDir.baseDirStrategy.get()
        final srcDir = main.sourceDir

        expect:
        bd instanceof BaseDirFollowSourceDir
        bd.getBaseDir(srcDir).get() == srcDir.get()
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
    }

    void 'Base directory can be fixed directory'() {
        setup:
        final srcDir = main.sourceDir
        final fooDir = new File(projectDir,'foo')

        when:
        main.baseDir.baseDir = fooDir
        final bd = main.baseDir.baseDirStrategy.get()

        then:
        bd instanceof BaseDirIsFixedPath
        bd.getBaseDir(srcDir).get().asFile == fooDir
    }

    void 'Can set source directory'() {
        when:
        final initValue = main.sourceDir.get().asFile

        then:
        initValue == new File(project.projectDir,'src/docs/asciidoc')

        when:
        main.sourceDir = 'foo'

        then:
        main.sourceDir.get().asFile == new File(project.projectDir,'foo')
    }
}