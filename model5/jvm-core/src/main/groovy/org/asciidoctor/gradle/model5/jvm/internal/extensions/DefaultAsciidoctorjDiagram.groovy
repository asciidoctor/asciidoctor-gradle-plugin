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

import groovy.transform.CompileStatic
import groovy.transform.Synchronized
import org.asciidoctor.gradle.model5.jvm.JvmModel
import org.asciidoctor.gradle.model5.jvm.extensions.AsciidoctorjDiagram
import org.asciidoctor.gradle.model5.jvm.internal.utils.DependencyUpdater
import org.asciidoctor.gradle.model5.jvm.toolchains.AsciidoctorjToolchain
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.ysb33r.grolifant5.api.core.ProjectOperations

import javax.inject.Inject
import java.util.function.Function

import static java.util.Collections.EMPTY_MAP
import static java.util.Collections.EMPTY_SET
import static org.asciidoctor.gradle.model5.core.internal.publications.PublicationUtils.CACHE_SUBDIR_BASE
import static org.asciidoctor.gradle.model5.jvm.internal.PluginUtils.loadDefaultVersion
import static org.ysb33r.grolifant5.api.core.StringTools.EMPTY

/**
 * Implementation of {@link AsciidoctorjDiagram}.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class DefaultAsciidoctorjDiagram extends AbstractAsciidoctorjExtension implements AsciidoctorjDiagram {

    static class Factory extends AbstractFactory implements NamedDomainObjectFactory<AsciidoctorjDiagram> {

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
        AsciidoctorjDiagram create(String name) {
            objectFactory.newInstance(DefaultAsciidoctorjDiagram, name, toolchain)
        }
    }

    public static final String DEFAULT_NAME = 'diagram'
    private static final String PROP_PREFIX = 'asciidoctorj.diagram'

    final Provider<Set<String>> requires
    final Provider<Map<String, Object>> attributeProvider
    final FileCollection classpath

    private final Property<String> diagramVersion
    private final Property<String> batikVersion
    private final Property<String> ditaaVersion
    private final Property<String> jsyntraxVersion
    private final Property<String> plantumlVersion
    private final String configurationName
    private boolean diagramRegistered = false

    @Inject
    DefaultAsciidoctorjDiagram(String name, AsciidoctorjToolchain tc, Project tempProjectReference) {
        super(name, tc, tempProjectReference)

        final Function<String, Provider<String>> helper = { String entity ->
            final prop = entity ? "${PROP_PREFIX}.${entity}".toString() : PROP_PREFIX
            loadDefaultVersion(prop, tempProjectReference, tc.class.classLoader)
        }

        this.diagramVersion = objectFactory.property(String).convention(helper.apply(EMPTY))
        this.batikVersion = objectFactory.property(String).convention(helper.apply('batik'))
        this.ditaaVersion = objectFactory.property(String).convention(helper.apply('ditaa'))
        this.jsyntraxVersion = objectFactory.property(String).convention(helper.apply('jsyntrax'))
        this.plantumlVersion = objectFactory.property(String).convention(helper.apply('plantuml'))

        this.configurationName = JvmModel.nameForExtensionConfiguration(tc.name, name)
        this.classpath = registerConfiguration(tc, tempProjectReference)

        this.requires = ccso.providerTools().provider { ->
            diagramRegistered ? ['asciidoctor-diagram'].toSet() : EMPTY_SET
        }

        final cache = ccso.fsOperations().toSafeFileName("${tc.name}-${name}")
        this.attributeProvider = ccso.fsOperations().buildDirDescendant("${CACHE_SUBDIR_BASE}/${cache}").map {
            diagramRegistered ? ['diagram-cachedir': it.absolutePath] : EMPTY_MAP
        }
    }

    /**
     * Use Diagram with default version.
     */
    @Override
    @Synchronized
    void useDiagram() {
        if (!diagramRegistered) {
            addToClasspath(JvmModel.ASCIIDOCTORJ_DIAGRAM_DEPENDENCY, diagramVersion)
            diagramRegistered = true
        }
    }

    /**
     * Use Diagram and override the default version.
     *
     * @param ver New version
     */
    @Override
    void useDiagram(Object ver) {
        ccso.stringTools().updateStringProperty(diagramVersion, ver)
        useDiagram()
    }

    /**
     * Use Ditaa.
     */
    @Override
    void useDitaa() {
        useDiagram()
        addToClasspath(JvmModel.ASCIIDOCTORJ_DIAGRAM_DITAA_DEPENDENCY, ditaaVersion)
    }

    /**
     * Use Ditaa and override the default version.
     *
     * @param ver New version
     */
    @Override
    void useDitaa(Object ver) {
        ccso.stringTools().updateStringProperty(ditaaVersion, ver)
        useDitaa()
    }

    /**
     * Use PlantUML.
     */
    @Override
    void usePlantUml() {
        useDiagram()
        addToClasspath(JvmModel.ASCIIDOCTORJ_DIAGRAM_PLANTUML_DEPENDENCY, plantumlVersion)
    }

    /**
     * Use PlantUML and override the default version.
     *
     * @param ver New version
     */
    @Override
    void usePlantUml(Object ver) {
        ccso.stringTools().updateStringProperty(plantumlVersion, ver)
        usePlantUml()
    }

    /**
     * Use Batik
     */
    @Override
    void useBatik() {
        useDiagram()
        addToClasspath(JvmModel.ASCIIDOCTORJ_DIAGRAM_BATIK_DEPENDENCY, batikVersion)
    }

    /**
     * Use Batik and override the default version.
     *
     * @param ver New version
     */
    @Override
    void useBatik(Object ver) {
        ccso.stringTools().updateStringProperty(batikVersion, ver)
        useBatik()
    }

    /**
     * Use JSyntrax,
     */
    @Override
    void useSyntrax() {
        useDiagram()
        addToClasspath(JvmModel.ASCIIDOCTORJ_DIAGRAM_JSYNTRAX_DEPENDENCY, jsyntraxVersion)
    }

    /**
     * Use JSyntrax and override the default version.
     *
     * @param ver New version
     */
    @Override
    void useSyntrax(Object ver) {
        ccso.stringTools().updateStringProperty(jsyntraxVersion, ver)
        useSyntrax()
    }

    /**
     * The type that this implements and which should be displayed.
     *
     * @return A type that needs to be displayed.
     */
    @Override
    protected Class<?> getDslType() {
        AsciidoctorjDiagram
    }

    private FileCollection registerConfiguration(AsciidoctorjToolchain tc, Project tempProjectReference) {
        final runtime = JvmModel.nameForExtensionConfigurationResolvable(tc.name, name)
        final configTools = ProjectOperations.find(tempProjectReference).configurations
        configTools.createLocalRoleFocusedConfiguration(configurationName, runtime)
        tc.classpathExtendsFrom(configurationName)
        tempProjectReference.configurations.getByName(runtime)
    }

    private void addToClasspath(String module, Provider<String> ver) {
        objectFactory.newInstance(DependencyUpdater).add(configurationName, module, ver)
    }
}
