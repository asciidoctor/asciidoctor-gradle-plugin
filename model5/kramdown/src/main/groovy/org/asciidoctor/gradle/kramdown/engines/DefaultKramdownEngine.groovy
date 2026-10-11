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
package org.asciidoctor.gradle.kramdown.engines

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.kramdown.KramdownAsciidocModel
import org.asciidoctor.gradle.kramdown.KramdownLauncher
import org.asciidoctor.gradle.kramdown.internal.engines.DefaultLauncher
import org.asciidoctor.gradle.kramdown.internal.utils.PluginUtils
import org.asciidoctor.gradle.model5.jrubytools.engines.BaseJRubyEngine
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider

import javax.inject.Inject

/**
 * Implementation of an engine to run {@code karamdown-asciidoc} conversions.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.1
 */
@CompileStatic
class DefaultKramdownEngine extends BaseJRubyEngine implements KramdownEngine {
    public static final String KRAMDOWN_PROP = 'kramdown'
    public static final String KRAMDOWN_PACKAGE = 'kramdown-asciidoc'
    private final Property<String> kramdownVersion
    private final Property<DefaultLauncher> jvmLauncher

    @Inject
    DefaultKramdownEngine(String name, Project tempProjectReference) {
        super(
            name,
            KramdownAsciidocModel.nameForEngineConfiguration(name),
            KramdownAsciidocModel.nameForGemsConfiguration(name),
            tempProjectReference
        )

        this.kramdownVersion = ccso.providerTools().property(String).convention(
            PluginUtils.loadDefaultVersion(KRAMDOWN_PROP, tempProjectReference, this.class.classLoader)
        )

        tempProjectReference.dependencies.addProvider(gemsConfigurationName, kramdownVersion.map {
            "rubygems:${KRAMDOWN_PACKAGE}:${it}"
        })

        this.jvmLauncher = ccso.providerTools().property(DefaultLauncher)
            .value(createLauncher(tempProjectReference))

        this.jvmLauncher.finalizeValue()
    }

    @Override
    Provider<? extends KramdownLauncher> getLauncher() {
        this.jvmLauncher
    }

    /**
     * Override the default version of {@code kramdown-asciidoc}.
     * @param ver Anything lazy-convertible to a string.
     */
    void useKramdownAsciidoc(Object ver) {
        ccso.stringTools().updateStringProperty(this.kramdownVersion, ver)
    }

    private DefaultLauncher createLauncher(Project tempProjectReference) {
        final cp = this.classpath
        tempProjectReference.objects.newInstance(
            DefaultLauncher,
            this.forkOptions,
            this.configuredEngineOptions
        ).tap {
            classpath(cp)
            forkEvery = forkAfterDocCount
        }
    }
}
