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
package org.asciidoctor.gradle.model5.jvm.internal.extensions

import org.asciidoctor.gradle.model5.core.ScriptCollection
import org.asciidoctor.gradle.model5.core.internal.DefaultScriptCollection
import org.asciidoctor.gradle.model5.jvm.JvmModel
import org.asciidoctor.gradle.model5.jvm.extensions.AsciidoctorjGroovyDslExtension
import org.asciidoctor.gradle.model5.jvm.internal.PluginUtils
import org.asciidoctor.gradle.model5.jvm.toolchains.AsciidoctorjToolchain
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Property
import org.ysb33r.grolifant5.api.core.ProjectOperations

import javax.inject.Inject

import static org.asciidoctor.gradle.model5.jvm.JvmModel.ASCIIDOCTORJ_GROOVY_DSL_DEPENDENCY
import static org.asciidoctor.gradle.model5.jvm.internal.engines.DefaultLauncher.SCRIPTS_GROOVY

/**
 * Implementation of the {@link AsciidoctorjGroovyDslExtension} extension.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
class DefaultAsciidoctorjGroovyDslExtension extends AbstractAsciidoctorjExtension
    implements AsciidoctorjGroovyDslExtension {

    public static final String DEFAULT_NAME = 'groovyDsl'

    static class Factory extends AbstractFactory implements NamedDomainObjectFactory<AsciidoctorjGroovyDslExtension> {
        @Inject
        Factory(AsciidoctorjToolchain toolchain, Project project) {
            super(toolchain, project)
        }

        /**
         * Creates a new object with the given name.
         *
         * @param name The name
         * @return The object.
         */
        @Override
        AsciidoctorjGroovyDslExtension create(String name) {
            objectFactory.newInstance(DefaultAsciidoctorjGroovyDslExtension, name, toolchain)
        }
    }

    private final DefaultScriptCollection scriptCollection
    private final Property<String> version

    @Inject
    DefaultAsciidoctorjGroovyDslExtension(String name, AsciidoctorjToolchain tc, Project project) {
        super(name, tc, project)

        this.scriptCollection = project.objects.newInstance(DefaultScriptCollection, SCRIPTS_GROOVY)
        this.version = project.objects.property(String).convention(PluginUtils.loadDefaultVersion(
            'asciidoctorj.groovydsl',
            project,
            tc.class.classLoader
        ))

        final configurationName = JvmModel.nameForExtensionConfiguration(tc.name, name)
        extensionClasspath.from(registerConfiguration(configurationName, tc, project))
        project.dependencies.add(
            configurationName,
            this.version.map { "${ASCIIDOCTORJ_GROOVY_DSL_DEPENDENCY}:${it}" }
        )
    }

    @Override
    ScriptCollection getScriptedExtensions() {
        this.scriptCollection
    }

    @Override
    void clearScripts() {
        this.scriptCollection.clear()
    }

    @Override
    void fromString(Object ext) {
        this.scriptCollection.addScript(ext)
    }

    @Override
    void fromFile(Object ext) {
        this.scriptCollection.addScriptFile(ext)
    }

    @Override
    void useVersion(Object ver) {
        ccso.stringTools().updateStringProperty(this.version, ver)
    }

    @Override
    protected Class<?> getDslType() {
        AsciidoctorjGroovyDslExtension
    }

    private FileCollection registerConfiguration(
        String configurationName,
        AsciidoctorjToolchain tc,
        Project tempProjectReference
    ) {
        final runtime = JvmModel.nameForExtensionConfigurationResolvable(tc.name, name)
        final configTools = ProjectOperations.find(tempProjectReference).configurations
        configTools.createLocalRoleFocusedConfiguration(configurationName, runtime)
        tempProjectReference.configurations.getByName(runtime)
    }
}
