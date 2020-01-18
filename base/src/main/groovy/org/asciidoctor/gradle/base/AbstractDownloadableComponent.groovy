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
package org.asciidoctor.gradle.base

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.UnknownDomainObjectException
import org.ysb33r.grolifant.api.git.AbstractCloudGit
import org.ysb33r.grolifant.api.git.GitRepoArchiveDownloader

import static org.asciidoctor.gradle.base.AsciidoctorUtils.executeDelegatingClosure

/** Base class for building extension which can allows styles and themes to be added to various
 * Asciidoctor backends that may require them.
 *
 * @since 2.0
 */
@CompileStatic
abstract class AbstractDownloadableComponent<ComponentSrc, ResolvedComponent> {

    private final Map<String, Closure> components = [:]
    protected final Project project

    /** Adds a component source that is available on the local filesystem.
     *
     * By default the {@code styleName} will match that of the theme.
     *
     * @param name Name of theme
     * @param localConfig Configures an instance of {@link ComponentSrc}.
     */
    void local(final String name, @DelegatesTo(ComponentSrc) Closure localConfig) {
        ComponentSrc component = instantiateComponentSource(name)
        executeDelegatingClosure(component, localConfig)
        this.components[name] = convertible(component)
    }

    /** Adds a component source that is available on the local filesystem.
     *
     * By default the {@code styleName} will match that of the theme.
     * If the name ends with {@code .yml}, it’s assumed to be the complete name of a file.
     * Otherwise, {@code -theme.yml} is appended to the name to make the file name (i.e., {@code <name>-theme.yml}).
     *
     * @param name Name of theme
     * @param localConfig Configures an instance of {@link ComponentSrc}.
     */
    void local(final String name, Action<ComponentSrc> localConfig) {
        ComponentSrc component = instantiateComponentSource(name)
        localConfig.execute(component)
        this.components[name] = convertible(component)
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

    /** Retrieve a component by name.
     *
     * If the component has not already been resolved, it will be resolved by this call.
     *
     * @param name Name of the component.
     * @return Resolved component.
     * @throw {@link org.gradle.api.UnknownDomainObjectException} is the component has not been registered.
     */
    ResolvedComponent getByName(final String name) {
        if (components.containsKey(name)) {
            components[name].call()
        } else {
            throw new UnknownDomainObjectException("Theme with name '${name}' was not registered")
        }
    }

    protected AbstractDownloadableComponent(Project project) {
        this.project = project
    }

    /** Creates a closure that can convert from a GitLab/GitHub repository to a local cached file.
     *
     * @param name Name or componenet
     * @param component Details of component in remote repository.
     * @return Closure that will resolve an archive from a remote reposiotry and store it locally.
     */
    protected Closure convertible(final String name, AbstractCloudGit component) {
        final GitRepoArchiveDownloader downloader = new GitRepoArchiveDownloader(component, project)
        final String relativePath = getRelativePathInsideArchive(component)
        return { ->
            downloader.downloadRoot = project.buildDir
            File root = downloader.archiveRoot

            instantiateResolvedComponent(name, relativePath ? new File(root, relativePath) : root)
        }
    }

    /** Instantiates a component of type {@code ComponentSrc}.
     *
     * @param name Name of componenet
     * @return New component source description
     */
    abstract protected ComponentSrc instantiateComponentSource(final String name)

    /** Create a closure that will convert an instance of a {@code ComponentSrc} into a {@code ResolvedComponent}.
     *
     * @param component Component to be converted.
     * @return Converting closure.
     */
    abstract protected Closure convertible(@DelegatesTo.Target ComponentSrc component)

    /** Instantiates a resolved component.
     *
     * @param name Name of component
     * @param path Path to where compoenet is located on local filesystem.
     * @return Instantiated component.
     */
    abstract protected ResolvedComponent instantiateResolvedComponent(final String name, final File path)

    private void addCloudGitArchive(
        final String name, final AbstractCloudGit archive, @DelegatesTo(AbstractCloudGit) Closure config
    ) {
        executeDelegatingClosure(archive, config)
        this.components[name] = convertible(name, archive)
    }

    private void addCloudGitArchive(
        final String name, final AbstractCloudGit archive, Action<AbstractCloudGit> config
    ) {
        config.execute(archive)
        this.components[name] = convertible(name, archive)
    }

    @CompileDynamic
    private String getRelativePathInsideArchive(AbstractCloudGit theme) {
        theme.relativePath
    }

}
