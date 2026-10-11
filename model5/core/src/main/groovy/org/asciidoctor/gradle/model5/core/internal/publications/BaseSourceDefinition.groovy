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
package org.asciidoctor.gradle.model5.core.internal.publications

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.publications.HasResources
import org.asciidoctor.gradle.model5.core.publications.HasSourceFiles
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.util.PatternFilterable
import org.gradle.api.tasks.util.PatternSet
import org.ysb33r.grolifant5.api.core.ClosureUtils
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations

/**
 * Base class for a minimal definitions of source files.
 * @author Schalk W. Cronjé
 *
 * @since 5.1
 */
@CompileStatic
class BaseSourceDefinition implements HasSourceFiles, HasResources {
    protected final DirectoryProperty srcDir
    protected final ConfigCacheSafeOperations ccso
    protected final PatternSet sourceDocumentPattern
    protected final Provider<PatternFilterable> sourcePatternProvider
    protected final PatternSet resourcePatterns
    protected final Provider<PatternFilterable> resourcePatternProvider

    /**
     * Adds these patterns that are relative to the source directory or the intermediate source directory.
     *
     * @param cfg {@link org.gradle.api.tasks.util.PatternFilterable} instance that can be configured.
     */
    @Override
    void resources(Action<? super PatternFilterable> cfg) {
        cfg.execute(this.resourcePatterns)
    }

    /**
     * Adds these patterns that are relative to the source directory or the intermediate source directory.
     *
     * @param cfg A closure that can configure a {@link PatternFilterable} instance.
     */
    @Override
    void resources(@DelegatesTo(PatternFilterable) Closure<?> cfg) {
        ClosureUtils.configureItem(this.resourcePatterns, cfg)
    }

    /**
     * Patterns that can be added to a copy spec for copying resources to a target directory.
     *
     * @return Copy specification
     */
    Provider<PatternFilterable> getResourcesPatterns() {
        this.resourcePatternProvider
    }

    /**
     * The top directory for asciidoc sources for this publication.
     *
     * @return Provider to location.
     */
    @Override
    Provider<Directory> getSourceDir() {
        this.srcDir
    }

    /**
     * The directory where the root of the Asciidoc documents will be found.
     *
     * <p>
     *   Usually this is {@code src/docs/asciidoc} or {@code src/docs/asciidoc<PUBLICATION>}.
     * </p>
     * @param srcDir New source directory
     */
    @Override
    void setSourceDir(Object srcDir) {
        ccso.fsOperations().updateDirectoryProperty(this.srcDir, srcDir)
    }

    /**
     * Configures sources.
     *
     * @param cfg Configuration closure. Is passed a {@link org.gradle.api.tasks.util.PatternSet}.
     */
    @Override
    void sources(@DelegatesTo(PatternSet) Closure<?> cfg) {
        ClosureUtils.configureItem(this.sourceDocumentPattern, cfg)
    }

    /**
     * Configures sources.
     *
     * @param cfg Configuration {@link org.gradle.api.Action}. Is passed a {@link PatternSet}.
     */
    @Override
    void sources(Action<? super PatternSet> cfg) {
        cfg.execute(this.sourceDocumentPattern)
    }

    /**
     * Include source patterns.
     *
     * @param includePatterns ANT-style patterns for sources to include
     */
    @Override
    void sources(String... includePatterns) {
        this.sourceDocumentPattern.include(includePatterns)
    }

    /**
     * Clears existing sources patterns.
     */
    @Override
    void clearSources() {
        sourceDocumentPattern.copyFrom(new PatternSet())
    }

    /**
     * A provider of source patterns.
     *
     * @return Patterns of files to include for Asciidoc sources.
     */
    @Override
    Provider<PatternFilterable> getSourcePatterns() {
        this.sourcePatternProvider
    }

    protected BaseSourceDefinition(
        Project tempProjectReference,
        List<String> defaultExcludePatterns,
        List<String> defaultIncludePatterns
    ) {
        this.ccso = ConfigCacheSafeOperations.from(tempProjectReference)
        this.srcDir = tempProjectReference.objects.directoryProperty()

        this.sourceDocumentPattern = new PatternSet().exclude(defaultExcludePatterns)
        this.resourcePatterns = new PatternSet()

        this.sourcePatternProvider = ccso.providerTools().provider { ->
            final ret = owner.sourceDocumentPattern.includes.empty ?
                new PatternSet().copyFrom(owner.sourceDocumentPattern).include(defaultIncludePatterns) :
                owner.sourceDocumentPattern
            (PatternFilterable) ret
        }

        this.resourcePatternProvider = ccso.providerTools().provider { ->
            (PatternFilterable) (owner.resourcePatterns.includes.empty ? null : owner.resourcePatterns)
        }
    }
}
