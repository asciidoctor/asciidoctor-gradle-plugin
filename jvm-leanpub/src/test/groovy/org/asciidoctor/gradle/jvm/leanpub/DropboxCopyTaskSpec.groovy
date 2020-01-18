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
package org.asciidoctor.gradle.jvm.leanpub

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

import static org.asciidoctor.gradle.jvm.leanpub.AsciidoctorJLeanpubDropboxCopyPlugin.COPY_TASK_NAME

@SuppressWarnings(['MethodName', 'DuplicateStringLiteral'])
class DropboxCopyTaskSpec extends Specification {

    Project project = ProjectBuilder.builder().build()
    DropboxCopyTask copy

    void setup() {
        project.apply plugin: 'org.asciidoctor.jvm.leanpub.dropbox-copy'
        copy = project.tasks.getByName(COPY_TASK_NAME)
    }

    void 'Dropbox folder has a default value'() {
        expect:
        copy.dropboxRoot != null
    }

    void 'Can set a book path'() {
        when:
        copy.bookPath = { 'foo' }

        then:
        copy.bookPath == 'foo'
    }

    void 'Destination directory is a combination of Dropbox root and book path'() {
        when:
        File fakeDropboxRoot = new File(project.projectDir, 'Dropbox')
        String bookIsAt = 'Leanpub/mybook'
        copy.dropboxRoot = { fakeDropboxRoot }
        copy.bookPath = bookIsAt

        then:
        copy.destinationDir.absoluteFile == new File(fakeDropboxRoot, "${bookIsAt}/manuscript").absoluteFile
    }

    void 'Can set source directory'() {
        when:
        copy.sourceDir = { 'foo' }

        then:
        copy.sourceDir == new File(project.projectDir, 'foo')
    }
}