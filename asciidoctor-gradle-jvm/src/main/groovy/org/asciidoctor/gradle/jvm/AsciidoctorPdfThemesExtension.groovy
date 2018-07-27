/*
 * Copyright 2013-2018 the original author or authors.
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

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.UnknownDomainObjectException
import org.ysb33r.grolifant.api.StringUtils
import org.ysb33r.grolifant.api.git.AbstractCloudGit
import org.ysb33r.grolifant.api.git.GitRepoArchiveDownloader

import static groovy.lang.Closure.DELEGATE_FIRST

/** Easy way to configure themes for Asciidoctor PDF either as local themes or
 * as downloadable.
 *
 * @since 2.0
 */
@CompileStatic
class AsciidoctorPdfThemesExtension {

    final static String NAME = 'pdfThemes'

    /** Describes tha name and location of a PDF theme.
     *
     */
    @SuppressWarnings('ClassName')
    static class ThemeDescriptor {
        final File styleDir
        final String styleName

        ThemeDescriptor(final String name, final File dir) {
            this.styleDir = dir
            this.styleName = name
        }
    }

    /** Defines a local theme.
     *
     */
    @SuppressWarnings('ClassName')
    static class LocalTheme {

        /** Directory where local theme is to be found/
         *
         */
        Object styleDir

        /** Name of theme.
         *
         */
        Object styleName
    }

    /** Defines a theme stored in a GitHub repository.
     *
     */
    @SuppressWarnings('ClassName')
    static class GitHubArchive extends org.ysb33r.grolifant.api.git.GitHubArchive {

        /** Relative path to locate the them inside the GitHub archive.
         *
         */
        Object relativePath
    }

    /** Defines a theme stored in a GitLab repository.
     *
     */
    @SuppressWarnings('ClassName')
    static class GitLabArchive extends org.ysb33r.grolifant.api.git.GitLabArchive {

        /** Relative path to locate the them inside the GitLab archive.
         *
         */
        Object relativePath
    }

    /** Extension for configuring PDF themes.
     *
     * @param project Prjoect this extension has been associated with.
     */
    AsciidoctorPdfThemesExtension(final Project project) {
        this.project = project
    }

    /** Adds a local PDF theme.
     *
     * By default the {@code styleName} will match that of the theme.
     *
     * @param name Name of theme
     * @param localConfig Configures an instance of {@link LocalTheme}.
     */
    void local(final String name, @DelegatesTo(LocalTheme) Closure localConfig) {
        LocalTheme theme = new LocalTheme()
        theme.styleName = name
        configureObject(theme, localConfig)

        this.themes[name] = convertible(theme)
    }

    /** Adds a local PDF theme.
     *
     * By default the {@code styleName} will match that of the theme.
     * If the name ends with {@code .yml}, it’s assumed to be the complete name of a file.
     * Otherwise, {@code -theme.yml} is appended to the name to make the file name (i.e., {@code <name>-theme.yml}).
     *
     * @param name Name of theme
     * @param localConfig Configures an instance of {@link LocalTheme}.
     */
    void local(final String name, Action<LocalTheme> localConfig) {
        LocalTheme theme = new LocalTheme()
        theme.styleName = name
        localConfig.execute(theme)

        this.themes[name] = convertible(theme)
    }

    /** Use a GitHub repository as a theme.
     *
     * @param name Name of theme
     * @param githubConfig Closure to configure a {@link GitHubArchive}.
     */
    void github(final String name, @DelegatesTo(GitHubArchive) Closure githubConfig) {
        addCloudGitArchive(name, new GitHubArchive(), githubConfig)
    }

    /** Use a GitHub repository as a theme.
     *
     * @param name Name of theme
     * @param githubConfig Action to configure a {@link GitHubArchive}.
     */
    void github(final String name, Action<GitHubArchive> githubConfig) {
        addCloudGitArchive(name, new GitHubArchive(), githubConfig as Action<AbstractCloudGit>)
    }

    /** Use a GitLab repository as a theme.
     *
     * @param name Name of theme
     * @param githubConfig Closure to configure a {@link GitLabArchive}.
     */
    void gitlab(final String name, @DelegatesTo(GitLabArchive) Closure gitlabConfig) {
        addCloudGitArchive(name, new GitLabArchive(), gitlabConfig)
    }

    /** Use a GitLab repository as a theme.
     *
     * @param name Name of theme
     * @param githubConfig Action to configure a {@link GitLabArchive}.
     */
    void gitlab(final String name, Action<GitLabArchive> gitlabConfig) {
        addCloudGitArchive(name, new GitLabArchive(), gitlabConfig as Action<AbstractCloudGit>)
    }

    /** Retrieve a theme by name
     *
     * @param name Name of the theme.
     * @return Descriptor of the location of a theme.
     * @throw {@link UnknownDomainObjectException} is the theme has not been registered.
     */
    ThemeDescriptor getByName(final String name) {
        if (themes.containsKey(name)) {
            themes[name].call()
        } else {
            throw new UnknownDomainObjectException("Theme with name '${name}' was not registered")
        }
    }

    private void addCloudGitArchive(
        final String name, final AbstractCloudGit archive, @DelegatesTo(AbstractCloudGit) Closure config
    ) {
        configureObject(archive, config)
        this.themes[name] = convertible(name, archive)
    }

    private void addCloudGitArchive(
        final String name, final AbstractCloudGit archive, Action<AbstractCloudGit> config
    ) {
        config.execute(archive)
        this.themes[name] = convertible(name, archive)
    }

    private Closure convertible(LocalTheme theme) {
        return { ->
            new ThemeDescriptor(StringUtils.stringize(theme.styleName), project.file(theme.styleDir))
        }
    }

    private Closure convertible(final String name, AbstractCloudGit theme) {
        final GitRepoArchiveDownloader downloader = new GitRepoArchiveDownloader(theme,project)
        final String relativePath = getRelativePathInsideArchive(theme)
        return { ->
            downloader.downloadRoot = project.buildDir
            File root = downloader.archiveRoot

            new ThemeDescriptor(name, relativePath ? new File(root,relativePath) : root)
        }
    }

    @CompileDynamic
    private String getRelativePathInsideArchive(AbstractCloudGit theme) {
        theme.relativePath
    }

    @CompileDynamic
    private void configureObject(final Object obj, final Closure cfg) {
        Closure config = (Closure) (cfg.clone())
        config.delegate = obj
        config.resolveStrategy = DELEGATE_FIRST
        config()
    }

    private final Map<String, Closure> themes = [:]
    private final Project project
}
