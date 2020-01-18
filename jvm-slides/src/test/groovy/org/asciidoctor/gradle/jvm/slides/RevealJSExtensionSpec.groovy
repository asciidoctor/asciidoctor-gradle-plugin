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
package org.asciidoctor.gradle.jvm.slides

import org.asciidoctor.gradle.base.GitHubArchive
import org.asciidoctor.gradle.base.ModuleVersionLoader
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Transformer
import org.gradle.api.provider.Provider
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Shared
import spock.lang.Specification

class RevealJSExtensionSpec extends Specification {

    @Shared
    Map<String,String> versionMap = ModuleVersionLoader.load('revealjs-extension')

    Project project = ProjectBuilder.builder().build()
    RevealJSExtension ext

    void setup() {
        ext = new RevealJSExtension(project)
    }

    void 'Default Reveal.Js version is set'() {
        expect:
        ext.version == versionMap['revealjs.gem']
    }

    void 'Reveal.Js version can be set'() {
        when:
        ext.version = '1.2.3'

        then:
        ext.version == '1.2.3'
    }

    void 'Default template provider is set'() {
        expect:
        ext.templateProvider != null
    }

    void 'Can override template provider with new GitHub location using an action'() {
        given:
        def defaultProvider = ext.templateProvider

        when:
        ext.templateGitHub(new Action<GitHubArchive>() {
            @Override
            void execute(GitHubArchive gitHubArchive) {
                gitHubArchive.organisation = 'foo'
                gitHubArchive.repository = 'bar'
                gitHubArchive.branch = 'master'
            }
        })

        then:
        defaultProvider != ext.templateProvider
    }

    void 'Can override template provider with new GitHub location using a closure'() {
        given:
        def defaultProvider = ext.templateProvider

        when:
        ext.templateGitHub {
            organisation = 'foo'
            repository = 'bar'
            branch = 'master'
        }

        then:
        defaultProvider != ext.templateProvider
    }

    void 'Can override template provider with custom provider'() {
        given:
        def defaultProvider = ext.templateProvider

        when:
        ext.templateProvider(nullProvider)

        then:
        defaultProvider != ext.templateProvider
    }

    void 'Can override template provider with local file location'() {
        given:
        def defaultProvider = ext.templateProvider

        when:
        ext.templateLocal('abc')

        then:
        defaultProvider != ext.templateProvider
    }

    Provider<File> getNullProvider() {
        new Provider<File>() {
            @Override
            File get() {
                null
            }

            @Override
            @SuppressWarnings('GetterMethodCouldBeProperty')
            File getOrNull() {
                null
            }

            @Override
            File getOrElse(File file) {
                null
            }

            @Override
            def <S> Provider<S> map(Transformer<? extends S, ? super File> transformer) {
                null
            }

            @Override
            def <S> Provider<S> flatMap(Transformer<? extends Provider<? extends S>, ? super File> transformer) {
                null
            }

            @Override
            boolean isPresent() {
                false
            }

            @SuppressWarnings('UnusedMethodParameter')
//            @Override
            Provider<File> orElse(File value) {
                null
            }

            @SuppressWarnings('UnusedMethodParameter')
//            @Override
            Provider<File> orElse(Provider<? extends File> provider) {
                null
            }
        }
    }
}