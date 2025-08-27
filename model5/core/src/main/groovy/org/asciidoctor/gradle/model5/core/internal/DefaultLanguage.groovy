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
package org.asciidoctor.gradle.model5.core.internal

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.waitingroom.Attributes
import org.asciidoctor.gradle.model5.core.waitingroom.Language
import org.asciidoctor.gradle.model5.core.internal.attributes.DefaultAttributes
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.CopySpec
import org.ysb33r.grolifant5.api.core.ClosureUtils
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations

import javax.inject.Inject

/**
 * Language option implementation.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class DefaultLanguage implements Language {

    final String name
    private final Attributes attributes
    private final CopySpec resourcesCopySpec
    private final ConfigCacheSafeOperations ccso

    @Inject
    DefaultLanguage(String name, Project project) {
        this.name = name
        this.ccso = ConfigCacheSafeOperations.from(project)
        this.attributes = project.objects.newInstance(DefaultAttributes)
        this.resourcesCopySpec = ccso.fsOperations().copySpec()
    }

    /**
     * Configures the attributes for this publication
     *
     * @param configurator Configurator
     */
    @Override
    void attributes(Action<Attributes> configurator) {
        configurator.execute(this.attributes)
    }

    /**
     * Configures the attributes for this publication
     *
     * @param configurator Configurator
     */
    @Override
    void attributes(@DelegatesTo(Attributes.class) Closure<?> configurator) {
        ClosureUtils.configureItem(this.attributes, configurator)
    }

    /**
     * Direct access to the attributes for this publication.
     *
     * @return Attributes
     */
    @Override
    Attributes getAttributes() {
        this.attributes
    }

    /**
     *  Add to the CopySpec for extra files.
     *
     * The destination of these files will always have a parent directory
     * of {@code outputDir} or {@code outputDir + backend}
     *
     * @param cfg {@link CopySpec} runConfiguration {@link Action}
     */
    @Override
    void resources(Action<? super CopySpec> cfg) {
        final childSpec = ccso.fsOperations().copySpec()
        cfg.execute(childSpec)
        this.resourcesCopySpec.with(childSpec)
    }

    /**
     *  Add to the CopySpec for extra files.
     *
     * The destination of these files will always have a parent directory
     * of {@code outputDir} or {@code outputDir + backend}
     *
     * @param cfg {@link CopySpec} runConfiguration {@link Action}
     */
    @Override
    void resources(@DelegatesTo(CopySpec) Closure<?> cfg) {
        final childSpec = ccso.fsOperations().copySpec()
        ClosureUtils.configureItem(childSpec, cfg)
        this.resourcesCopySpec.with(childSpec)
    }
}
