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
package org.asciidoctor.gradle.jvm.slides

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.base.GitHubArchive
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.ysb33r.grolifant.api.Version
import org.ysb33r.grolifant.api.git.GitRepoArchiveDownloader

import static org.asciidoctor.gradle.base.AsciidoctorUtils.executeDelegatingClosure

/** Configures the Reveal.JS version and Reveal.JS template.
 *
 * @since 2.0
 */
@CompileStatic
class RevealJSExtension {

    final static String NAME = 'revealjs'
    final static String DEFAULT_VERSION = '1.1.3'
    final static String DEFAULT_TEMPLATE_VERSION = '3.7.0'
    final static Version FIRST_VERSION_WITH_PLUGIN_SUPPORT = Version.of('1.2.0')

    private String version = DEFAULT_VERSION
    private Provider<File> resolveRevealJs
    private final Project project

    RevealJSExtension(Project project) {
        this.project = project
        templateGitHub {
            organisation = 'hakimel'
            repository = 'reveal.js'
            tag = { DEFAULT_TEMPLATE_VERSION }
        }
    }

    void templateLocal(Object location) {
        resolveRevealJs = { ->
            project.file(location)
        } as Provider<File>
    }

    void templateGitHub(@DelegatesTo(GitHubArchive) Closure configurator) {
        GitHubArchive archive = new GitHubArchive()
        executeDelegatingClosure(archive, configurator)
        resolveViaGitHub(archive)
    }

    void templateGitHub(Action<GitHubArchive> configurator) {
        GitHubArchive archive = new GitHubArchive()
        configurator.execute(archive)
        resolveViaGitHub(archive)
    }

    void templateProvider(Provider<File> revealJsProvider) {
        this.resolveRevealJs = revealJsProvider
    }

    void setVersion(final String v) {
        this.version = v
    }

    String getVersion() {
        this.version
    }

    Provider<File> getTemplateProvider() {
        this.resolveRevealJs
    }

    private void resolveViaGitHub(final GitHubArchive archive) {
        final GitRepoArchiveDownloader downloader = new GitRepoArchiveDownloader(archive, project)

        resolveRevealJs = { ->
            downloader.downloadRoot = project.buildDir
            final File root = downloader.archiveRoot
            final String relativePath = archive.relativePath
            relativePath ? new File(root, relativePath) : root
        } as Provider<File>

    }

}
