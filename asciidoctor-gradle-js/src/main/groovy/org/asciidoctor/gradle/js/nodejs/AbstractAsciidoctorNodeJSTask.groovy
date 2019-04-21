/*
 * Copyright 2013-2019 the original author or authors.
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
package org.asciidoctor.gradle.js.nodejs

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.base.AsciidoctorAttributeProvider
import org.asciidoctor.gradle.base.internal.Workspace
import org.asciidoctor.gradle.js.base.AbstractAsciidoctorTask
import org.asciidoctor.gradle.js.nodejs.internal.AsciidoctorJSRunner
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkerExecutor
import org.ysb33r.gradle.nodejs.NodeJSExtension
import org.ysb33r.gradle.nodejs.NpmExtension
import org.ysb33r.gradle.nodejs.utils.npm.NpmExecutor
import org.ysb33r.grolifant.api.MapUtils
import org.ysb33r.grolifant.api.StringUtils

/** Base class for all Asciidoctor tasks using Asciidoctor.js as rendering engine.
 *
 * @author Schalk W. Cronjé
 *
 * @since 3.0
 */
@CompileStatic
class AbstractAsciidoctorNodeJSTask extends AbstractAsciidoctorTask {

    private final WorkerExecutor worker
    private final AsciidoctorJSExtension asciidoctorjs
    private final NodeJSExtension nodejs
    private final NpmExtension npm

    /** Returns all of the Asciidoctor options.
     *
     * This is equivalent of using {@code asciidoctorjs.getAttributes}
     *
     */
    @Input
    Map getAttributes() {
        asciidoctorjs.attributes
    }

    /** Apply a new set of Asciidoctor attributes, clearing any attributes previously set.
     *
     * If set here all global Asciidoctor options are ignored within this task.
     *
     * This is equivalent of using {@code asciidoctorjs.setAttributes}.
     *
     * @param m Map with new options
     */
    void setAttributes(Map m) {
        asciidoctorjs.attributes = m
    }

    /** Add additional asciidoctor attributes.
     *
     * If set here these options will be used in addition to any global Asciidoctor attributes.
     *
     * This is equivalent of using {@code asciidoctorjs.attributes}.
     *
     * @param m Map with new options
     */
    void attributes(Map m) {
        asciidoctorjs.attributes(m)
    }

    /** Shortcut method to access additional providers of attributes.
     *
     * @return List of attribute providers.
     */
    @Override
    @Internal
    List<AsciidoctorAttributeProvider> getAttributeProviders() {
        asciidoctorjs.attributeProviders
    }

    @TaskAction
    void processAsciidocSources() {
        validateConditions()
        Workspace workspace = prepareWorkspace()
        runWithSubprocess(workspace.workingSourceDir)
    }

    /** Initialises the core an Asciidoctor task
     *
     * @param we {@link WorkerExecutor}. This is usually injected into the
     *   constructor of the subclass.
     */
    @SuppressWarnings('ThisReferenceEscapesConstructor')
    protected AbstractAsciidoctorNodeJSTask(WorkerExecutor we) {
        this.worker = we
        this.asciidoctorjs = this.extensions.create(AsciidoctorJSExtension.NAME, AsciidoctorJSExtension, this)
        this.nodejs = project.extensions.getByType(AsciidoctorJSNodeExtension)
        this.npm = project.extensions.getByType(AsciidoctorJSNpmExtension)
    }

    @Override
    @Internal
    protected String getEngineName() {
        'Asciidoctor.js'
    }

    private Map<String, String> prepareAttributes(final File workingSourceDir) {
        Map<String, Object> attrs = [:]
        attrs.putAll(asciidoctorjs.attributes)
        asciidoctorjs.attributeProviders.each {
            attrs.putAll(it.attributes)
        }
        Set<String> userDefinedAttrKeys = attrs.keySet()

        Map<String, Object> defaultAttrs = getTaskSpecificDefaultAttributes(workingSourceDir).findAll { k, v ->
            !userDefinedAttrKeys.contains(k)
        }.collectEntries { k, v ->
            ["${k}@".toString(), v instanceof Serializable ? v : StringUtils.stringize(v)]
        } as Map<String, Object>

        attrs.putAll(defaultAttrs)
        MapUtils.stringizeValues(evaluateProviders(attrs))
    }

    private Map<String, Object> evaluateProviders(final Map<String, Object> initialMap) {
        initialMap.collectEntries { String k, Object v ->
            if (v instanceof Provider) {
                [k, v.get()]
            } else {
                [k, v]
            }
        } as Map<String, Object>
    }

    @SuppressWarnings('UnnecessaryGetter')
    private AsciidoctorJSRunner getAsciidoctorJSRunnerFor(
        final AsciidoctorJSRunner.FileLocations asciidoctorjsExe,
        final String backend,
        final Map<String, String> attributes
    ) {
        new AsciidoctorJSRunner(
            nodejs.resolvableNodeExecutable.executable,
            project,
            asciidoctorjsExe,
            backend,
            asciidoctorjs.safeMode,
            getBaseDir(),
            getOutputDirFor(backend),
            attributes,
            asciidoctorjs.requires,
            Optional.empty(),
            logDocuments
        )
    }

    private AsciidoctorJSRunner.FileLocations resolveAsciidoctorjsEnvironment() {
        File home = asciidoctorjs.toolingWorkDir
        File packageJson = new File(home, 'package.json')
        if (!packageJson.exists()) {
            home.mkdirs()
            NpmExecutor.initPkgJson(
                "${project.name}-${name}",
                project.version ? StringUtils.stringize(project.version) : 'UNDEFINED',
                project,
                nodejs,
                npm
            )
        }

        asciidoctorjs.configuration.resolve()
        new AsciidoctorJSRunner.FileLocations(
            executable: new File(home, 'node_modules/asciidoctor/bin/asciidoctor'),
            workingDir: home
        )
    }

    @SuppressWarnings('UnnecessaryGetter')
    private void runWithSubprocess(final File workingSourceDir) {
        logger.info 'Running Asciidoctor.js with subprocess.'

        Map<String, List<File>> conversionGroups = sourceFileGroupedByRelativePath
        Map<String, String> finalAttributes = prepareAttributes(workingSourceDir)
        AsciidoctorJSRunner.FileLocations asciidoctorjsEnv = resolveAsciidoctorjsEnvironment()
        Optional<List<String>> copyResources = getCopyResourcesForBackends()

        for (String backend : configuredOutputOptions.backends) {
            conversionGroups.each { String relativePath, List<File> sourceGroup ->
                getAsciidoctorJSRunnerFor(
                    asciidoctorjsEnv,
                    backend,
                    finalAttributes
                ).convert(sourceGroup.toSet(), relativePath)
            }
            if (copyResources.present && (copyResources.get().empty || backend in copyResources.get())) {
                copyResourcesByBackend(backend, workingSourceDir, getOutputDirFor(backend))
            }
        }
    }
}