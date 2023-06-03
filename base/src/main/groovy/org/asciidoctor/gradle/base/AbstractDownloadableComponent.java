/*
 * Copyright 2013-2024 the original author or authors.
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
package org.asciidoctor.gradle.base;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.UnknownDomainObjectException;
import org.gradle.api.provider.Provider;
import org.ysb33r.grolifant.api.core.ClosureUtils;
import org.ysb33r.grolifant.api.core.ProjectOperations;
import org.ysb33r.grolifant.api.core.git.AbstractCloudGit;
import org.ysb33r.grolifant.api.core.git.GitLabArchive;
import org.ysb33r.grolifant.api.core.git.GitRepoArchiveDownloader;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Base class for building extension which can allows styles and themes to be added to various
 * Asciidoctor backends that may require them.
 *
 * @param <ComponentSrc>      Source component type
 * @param <ResolvedComponent> Final comnponent type
 * @since 2.0
 */
public abstract class AbstractDownloadableComponent<ComponentSrc, ResolvedComponent> {

    protected final ProjectOperations projectOperations;
    private final Map<String, Callable<ResolvedComponent>> components =
            new LinkedHashMap<>();

    private final Provider<File> downloadRootProvider;

    /**
     * Adds a component source that is available on the local filesystem.
     * <p>
     * By default, the {@code styleName} will match that of the theme.
     * If the name ends with {@code .yml}, it’s assumed to be the complete name of a file.
     * Otherwise, {@code -theme.yml} is appended to the name to make the file name (i.e., {@code <name>-theme.yml}).
     *
     * @param name        Name of theme
     * @param localConfig Configures an instance of {@link ComponentSrc}.
     */
    public void local(final String name, Action<ComponentSrc> localConfig) {
        ComponentSrc component = instantiateComponentSource(name);
        localConfig.execute(component);
        this.components.put(name, convertible(component));
    }

    /**
     * Use a GitHub repository as a theme.
     *
     * @param name         Name of theme
     * @param githubConfig Action to configure a {@link AscGitHubArchive}.
     */
    public void github(final String name, Action<AscGitHubArchive> githubConfig) {
        AscGitHubArchive archive = new AscGitHubArchive(projectOperations);
        githubConfig.execute(archive);
        this.components.put(name, convertible(name, archive));
    }

    /**
     * Use a GitLab repository as a theme.
     *
     * @param name         Name of theme
     * @param gitlabConfig Closure to configure a {@link AscGitLabArchive}.
     */
    public void gitlab(final String name, @DelegatesTo(GitLabArchive.class) Closure<?> gitlabConfig) {
        AscGitLabArchive archive = new AscGitLabArchive(projectOperations);
        ClosureUtils.configureItem(archive,gitlabConfig);
        this.components.put(name, convertible(name, archive));
    }

    /**
     * Use a GitLab repository as a theme.
     *
     * @param name         Name of theme
     * @param gitlabConfig Action to configure a {@link AscGitLabArchive}.
     */
    public void gitlab(final String name, Action<AscGitLabArchive> gitlabConfig) {
        AscGitLabArchive archive = new AscGitLabArchive(projectOperations);
        gitlabConfig.execute(archive);
        this.components.put(name, convertible(name, archive));
    }

    /**
     * Retrieve a component by name.
     * <p>
     * If the component has not already been resolved, it will be resolved by this call.
     *
     * @param name Name of the component.
     * @return Resolved component.
     * @throw {@link UnknownDomainObjectException} is the component has not been registered.
     */
    public ResolvedComponent getByName(final String name) {
        try {
            if (components.containsKey(name)) {
                return components.get(name).call();
            }
        } catch (Exception e) {
            throw new UnknownDomainObjectException("Unexpected error when tryying to retrieve " + name, e);
        }

        throw new UnknownDomainObjectException("Theme with name '" + name + "' was not registered");
    }

    protected AbstractDownloadableComponent(Project project) {
        this.projectOperations = ProjectOperations.find(project);
        this.downloadRootProvider = projectOperations.buildDirDescendant("cloud-archives");
    }

    /**
     * Creates a closure that can convert from a GitLab/GitHub repository to a local cached file.
     *
     * @param name      Name or componenet
     * @param component Details of component in remote repository.
     * @return Closure that will resolve an archive from a remote repository and store it locally.
     */
    protected Callable<ResolvedComponent> convertible(final String name, AbstractCloudGit component) {
        final GitRepoArchiveDownloader downloader = new GitRepoArchiveDownloader(component, projectOperations);
        final String relativePath = getRelativePathInsideArchive(component);
        return () -> {
            downloader.setDownloadRoot(downloadRootProvider.get());
            File root = downloader.getArchiveRoot();
            return instantiateResolvedComponent(name, relativePath != null ? new File(root, relativePath) : root);
        };
    }

    /**
     * Instantiates a component of type {@code ComponentSrc}.
     *
     * @param name Name of componenet
     * @return New component source description
     */
    protected abstract ComponentSrc instantiateComponentSource(final String name);

    /**
     * Create a closure that will convert an instance of a {@code ComponentSrc} into a {@code ResolvedComponent}.
     *
     * @param component Component to be converted.
     * @return Converting closure.
     */
    protected abstract Callable<ResolvedComponent> convertible(ComponentSrc component);

    /**
     * Instantiates a resolved component.
     *
     * @param name Name of component
     * @param path Path to where compoenet is located on local filesystem.
     * @return Instantiated component.
     */
    protected abstract ResolvedComponent instantiateResolvedComponent(final String name, final File path);

//    private void addCloudGitArchive(
//            final String name,
//            final AbstractCloudGit archive,
//            @DelegatesTo(AbstractCloudGit.class)  Closure<?> config
//    ) {
//        AsciidoctorUtils.executeDelegatingClosure(archive, config);
//        this.components.put(name, convertible(name, archive));
//    }

    private void addCloudGitArchive(
            final String name,
            final AbstractCloudGit archive,
            Action<? super AbstractCloudGit> config
    ) {
        config.execute((AbstractCloudGit) archive);
        this.components.put(name, convertible(name, archive));
    }

    private String getRelativePathInsideArchive(AbstractCloudGit theme) {
        return ((String) (theme.getProperty("relativePath")));
    }
}
