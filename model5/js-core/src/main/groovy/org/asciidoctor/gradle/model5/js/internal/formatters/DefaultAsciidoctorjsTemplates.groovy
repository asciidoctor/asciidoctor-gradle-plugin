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
package org.asciidoctor.gradle.model5.js.internal.formatters

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.ConversionTemplate
import org.asciidoctor.gradle.model5.core.errors.ConfigurationNotSupportedException
import org.asciidoctor.gradle.model5.core.internal.DefaultConversionTemplate
import org.asciidoctor.gradle.model5.js.formatters.HasAsciidoctorjsTemplates
import org.asciidoctor.gradle.model5.js.formatters.SupportedTemplateEngines
import org.asciidoctor.gradle.model5.js.toolchains.AsciidoctorjsToolchain
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations

import javax.inject.Inject
import java.util.function.Consumer

import static org.asciidoctor.gradle.model5.js.formatters.SupportedTemplateEngines.fromEngine
import static org.asciidoctor.gradle.model5.js.internal.PluginUtils.loadDefaultVersion
import static org.ysb33r.grolifant5.api.core.StringTools.COMMA_SPACE

/**
 * Internal template implementation.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class DefaultAsciidoctorjsTemplates implements HasAsciidoctorjsTemplates {

    public static final String BUILTIN_ENGINE = SupportedTemplateEngines.TEMPLATE_JS.engineName
    public static final List<String> SUPPORTED_TEMPLATE_ENGINES = SupportedTemplateEngines.values()*.engineName
        .asImmutable()

    final Provider<ConversionTemplate> conversionTemplate
    private final ListProperty<Directory> directories
    private final ConfigCacheSafeOperations ccso
    private final ObjectFactory objectFactory
    private final AsciidoctorjsToolchain toolchain
    private final Consumer<String> requires

    @Inject
    DefaultAsciidoctorjsTemplates(
        AsciidoctorjsToolchain tc,
        Consumer<String> addRequires,
        Project project
    ) {
        this.ccso = ConfigCacheSafeOperations.from(project)
        this.toolchain = tc
        this.requires = addRequires
        this.objectFactory = project.objects
        this.directories = objectFactory.listProperty(Directory)
        this.conversionTemplate = directories.map { dirs ->
            dirs.empty ? (ConversionTemplate) null : new DefaultConversionTemplate(dirs, [])
        }
    }

    @Override
    void templateDirs(Object... dirs) {
        dirs.each {
            directories.add(ccso.fsOperations().provideDirectory(it))
        }
    }

    @Override
    void templateDirs(Collection<Object> dirs) {
        dirs.each {
            directories.add(ccso.fsOperations().provideDirectory(it))
        }
    }

    @Override
    void useEngine(String engineName) {
        if (engineName in SUPPORTED_TEMPLATE_ENGINES) {
            if (engineName != BUILTIN_ENGINE) {
                addPackage(
                    fromEngine(engineName),
                    loadDefaultVersion("asciidoctorjs.${engineName}", ccso, this.class.classLoader)
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
        if (engineName in SUPPORTED_TEMPLATE_ENGINES) {
            if (engineName != BUILTIN_ENGINE) {
                addPackage(fromEngine(engineName), ccso.stringTools().provideString(version))
            }
        } else {
            throw new ConfigurationNotSupportedException(
                "Only one of ${SUPPORTED_TEMPLATE_ENGINES.join(COMMA_SPACE)} can be used with this setting"
            )
        }
    }

    private void addPackage(SupportedTemplateEngines engine, Provider<String> pkgVersion) {
        requires.accept(engine.requires)
        toolchain.usePackage(engine.packageScope, engine.packageName, pkgVersion)
    }
}
