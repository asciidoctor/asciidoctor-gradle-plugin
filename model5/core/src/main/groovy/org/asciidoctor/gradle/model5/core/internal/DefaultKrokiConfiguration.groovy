/*
 * Copyright 2013 - 2026 the original author or authors.
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
package org.asciidoctor.gradle.model5.core.internal

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.errors.ConfigurationNotSupportedException
import org.asciidoctor.gradle.model5.core.kroki.KrokiConfiguration
import org.asciidoctor.gradle.model5.core.revealjs.FileOrUri
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.ysb33r.grolifant5.api.core.ClosureUtils
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations
import org.ysb33r.grolifant5.api.core.OperatingSystem
import org.ysb33r.grolifant5.api.core.StringTools

import javax.inject.Inject

/**
 * Implementation of {@link KrokiConfiguration}
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class DefaultKrokiConfiguration implements KrokiConfiguration {

    private static final List<String> METHODS = ['get', 'post', 'adaptive'].asImmutable()
    private static final String PATH_SEPARATOR = OperatingSystem.current().pathSeparator
    private final ConfigCacheSafeOperations ccso
    private final Property<URI> serverUri
    private final Property<Boolean> fetch
    private final Property<String> method
    private final Property<Integer> maxlen
    private final ListProperty<File> searchPaths
    private final MapProperty<String, Object> attributes
    private final FileOrUri plantUmlPath

    @Inject
    DefaultKrokiConfiguration(Project project) {
        this.ccso = ConfigCacheSafeOperations.from(project)
        this.serverUri = project.objects.property(URI)
        this.fetch = project.objects.property(Boolean)
        this.method = project.objects.property(String)
        this.maxlen = project.objects.property(Integer)
        this.plantUmlPath = project.objects.newInstance(FileOrUri, 'kroki-plantuml-include')
        this.attributes = project.objects.mapProperty(String, Object)
        this.searchPaths = project.objects.listProperty(File)

        this.attributes.putAll(plantUmlPath.attributeProvider)
        this.attributes.putAll(ccso.stringTools().provideValuesDropNull([
            'kroki-server-url'            : serverUri,
            'kroki-fetch-diagram'         : fetch,
            'kroki-http-method'           : method,
            'kroki-max-uri-length'        : maxlen,
            'kroki-plantuml-include-paths': searchPaths.map { it*.absolutePath.join(PATH_SEPARATOR) }
        ]))
    }

    /**
     * Indicates that something can provide unresolved attributes.
     *
     * @return Provider to a map of unresolved attributes.
     */
    @Override
    Provider<Map<String, Object>> getAttributeProvider() {
        this.attributes
    }

    /**
     * The URL of the Kroki server.
     *
     * <p>If not set, the implementation will use the one at {@code kroki.io}.</p>
     *
     * @param uri Anything convertible to a URI.
     */
    @Override
    void setServerUri(Object uri) {
        this.serverUri.set(ccso.stringTools().provideUri(uri))
    }

    /**
     * Define if the images from the Kroki server should be stored to disk.
     *
     * @param flag {@code true} to download images.
     */
    @Override
    void setFetchDiagrams(boolean flag) {
        this.fetch.set(flag)
    }

    /**
     * Define how we should get the image from the Kroki server.
     *
     * <p>
     *     If {@code adaptive} is used, {@code get} will be used unless the URI length is longer than what was set by
     * {@link #setMaxUriLength(int)}.
     * </p>
     * @param method {@code get}, {@code post}, or {@code adaptive}
     */
    @Override
    void setKrokiMethod(String method) {
        final m = method.toLowerCase(Locale.US)
        if (m in METHODS) {
            this.method.set(m)
        } else {
            throw new ConfigurationNotSupportedException(
                "Supplied method '${method}' must be one of ${METHODS.join(StringTools.COMMA_SPACE)}."
            )
        }
    }

    /**
     * Define the max URI length before using a POST request when using adaptive HTTP method.
     *
     * <p>If not set, an internal default will be used.</p>
     *
     * @param length Max length.
     */
    @Override
    void setMaxUriLength(int length) {
        this.maxlen.set(length)
    }

    /**
     * A file that will be included at the top of all PlantUML diagrams as if {@code !include} file was used.
     * This can be useful when you want to define a common skin for all your diagrams. The value can be a path or a URL.
     *
     * @param path Configure a file, a URI, or a relative path.
     */
    @Override
    void plantUmlIncludePath(Action<FileOrUri> path) {
        path.execute(this.plantUmlPath)
    }

    /**
     * A file that will be included at the top of all PlantUML diagrams as if {@code !include} file was used.
     * This can be useful when you want to define a common skin for all your diagrams. The value can be a path or a URL.
     *
     * @param path Configure a file, a URI, or a relative path.
     */
    @Override
    void plantUmlIncludePath(@DelegatesTo(FileOrUri) Closure<?> path) {
        ClosureUtils.configureItem(this.plantUmlPath, path)
    }

    /**
     * Search path(s) that will be used to resolve {@code !include} file additionally to current diagram directory,
     *     similar to PlantUML property plantuml.include.path.
     *
     * <p>
     *     The implementation will take care to present the correctly formatted value to the engine as required
     *     by the operating system conventions.
     * </p>
     * @param paths One or more search paths. Anything convertible to a file will do.
     */
    @Override
    void plantUmlSearchPaths(Object... paths) {
        paths.each {
            searchPaths.add(ccso.fsOperations().provideFile(it))
        }
    }
}
