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
package org.asciidoctor.gradle.model5.jvm.internal.formatters

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.ConversionTemplate
import org.asciidoctor.gradle.model5.core.errors.ConfigurationNotSupportedException
import org.asciidoctor.gradle.model5.core.internal.DefaultConversionTemplate
import org.asciidoctor.gradle.model5.jvm.formatters.HasAsciidoctorjTemplates
import org.asciidoctor.gradle.model5.jvm.internal.utils.DependencyUpdater
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations

import javax.inject.Inject
import java.util.concurrent.Callable

import static org.asciidoctor.gradle.model5.jvm.internal.PluginUtils.loadDefaultVersion
import static org.asciidoctor.gradle.model5.jvm.internal.gems.GemUtils.GEM_GROUP
import static org.asciidoctor.gradle.model5.jvm.internal.gems.GemUtils.GEM_TILT
import static org.asciidoctor.gradle.model5.jvm.plugins.AsciidoctorjGemsPlugin.PLUGIN_ID
import static org.ysb33r.grolifant5.api.core.StringTools.COMMA_SPACE

/**
 * Internal template implementation.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class DefaultAsciidoctorjTemplates implements HasAsciidoctorjTemplates {

    public static final String BUILTIN_ENGINE = 'erb'
    public static final List<String> SUPPORTED_TEMPLATE_ENGINES = [BUILTIN_ENGINE, 'haml', 'slim'].asImmutable()

    final Provider<ConversionTemplate> conversionTemplate
    private final ListProperty<Directory> directories
    private final ConfigCacheSafeOperations ccso
    private final Callable<Boolean> hasPlugin
    private final ObjectFactory objectFactory
    private final Property<String> tiltVersion
    private final Property<String> engine
    private final String configName
    private boolean tiltAdded = false

    @Inject
    DefaultAsciidoctorjTemplates(String gemConfigurationName, Project project) {
        this.ccso = ConfigCacheSafeOperations.from(project)
        this.objectFactory = project.objects
        this.directories = objectFactory.listProperty(Directory)
        this.hasPlugin = { -> project.pluginManager.hasPlugin(PLUGIN_ID) }
        this.configName = gemConfigurationName
        this.engine = objectFactory.property(String)
        this.tiltVersion = objectFactory.property(String).convention(
            loadDefaultVersion('gem.tilt', project, this.class.classLoader)
        )

        this.conversionTemplate = directories.zip(this.engine) { dirs, engine ->
            dirs.empty ? (ConversionTemplate) null : new DefaultConversionTemplate(dirs, [engine])
        }
    }

    @Override
    void templateDirs(Object... dirs) {
        checkForGemsPlugin()
        dirs.each {
            directories.add(ccso.fsOperations().provideDirectory(it))
        }
        if (dirs.size() > 0) {
            addTilt()
        }
    }

    @Override
    void templateDirs(Collection<Object> dirs) {
        checkForGemsPlugin()
        dirs.each {
            directories.add(ccso.fsOperations().provideDirectory(it))
        }
        if (dirs.size() > 0) {
            addTilt()
        }
    }

    @Override
    void setForceEngine(String engineName) {
        this.engine.set(engineName)
    }

    @Override
    void useEngine(String engineName) {
        if (engineName in SUPPORTED_TEMPLATE_ENGINES) {
            if (engineName != BUILTIN_ENGINE) {
                addGem(
                    engineName,
                    loadDefaultVersion(
                        "gem.${engineName}",
                        ccso.fsOperations(),
                        ccso.providerTools(),
                        this.class.classLoader
                    )
                )
            }
        } else {
            throw new ConfigurationNotSupportedException(
                "Only one of ${SUPPORTED_TEMPLATE_ENGINES.join(COMMA_SPACE)} can be used with this setting"
            )
        }
    }

    @Override
    void useEngine(String engineName, Object version) {
        addGem(engineName, ccso.stringTools().provideString(version))
    }

    private void addTilt() {
        if (!tiltAdded) {
            final deps = objectFactory.newInstance(DependencyUpdater)
            deps.add(configName, tiltVersion.map { "${GEM_GROUP}:${GEM_TILT}:${it}".toString() })

            tiltAdded = true
        }
    }

    private void addGem(String gemName, Provider<String> gemVersion) {
        objectFactory.newInstance(DependencyUpdater).add(
            configName,
            gemVersion.map { "${GEM_GROUP}:${gemName}:${it}".toString() }
        )
    }

    private void checkForGemsPlugin() {
        if (!hasPlugin.call()) {
            throw new GradleException("Templates cannot be configured if '${PLUGIN_ID}' has not been applied")
        }
    }
}
