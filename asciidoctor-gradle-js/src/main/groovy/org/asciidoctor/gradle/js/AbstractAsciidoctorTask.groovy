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
package org.asciidoctor.gradle.js

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.base.AbstractAsciidoctorBaseTask
import org.asciidoctor.gradle.base.internal.Workspace
import org.asciidoctor.gradle.js.internal.AsciidoctorJSResolver
import org.asciidoctor.gradle.js.internal.AsciidoctorJSRunner
import org.gradle.api.file.CopySpec
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkerExecutor
import org.ysb33r.gradle.nodejs.NodeJSExtension
import org.ysb33r.gradle.nodejs.NpmExtension
import org.ysb33r.grolifant.api.MapUtils
import org.ysb33r.grolifant.api.StringUtils

/** Base class for all Asciidoctor tasks using Asciidoctor.js as rendering engine.
 *
 * @since 3.0
 */
@CompileStatic
class AbstractAsciidoctorTask extends AbstractAsciidoctorBaseTask {

    private final WorkerExecutor worker
    private final AsciidoctorJSExtension asciidoctorjs
    private final AsciidoctorJSResolver asciidoctorJSResolver
    private final NodeJSExtension nodejs
    private final NpmExtension npm

    @TaskAction
    void processAsciidocSources() {
        validateConditions()
        asciidoctorjs.configuration.resolve()
        Workspace workspace = prepareWorkspace()
        Set<File> sourceFiles = workspace.sourceTree.files

        runWithSubprocess(workspace.workingSourceDir, sourceFiles)

    }

    /** Initialises the core an Asciidoctor task
     *
     * @param we {@link WorkerExecutor}. This is usually injected into the
     *   constructor of the subclass.
     */
    protected AbstractAsciidoctorTask(WorkerExecutor we) {
        this.worker = we
        this.asciidoctorjs = this.extensions.create(AsciidoctorJSExtension.NAME, AsciidoctorJSExtension, this)
        this.nodejs = project.extensions.getByType(AsciidoctorJSNodeExtension)
        this.npm = project.extensions.getByType(AsciidoctorJSNpmExtension)
        this.asciidoctorJSResolver = new AsciidoctorJSResolver(project, nodejs, npm)
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

    private AsciidoctorJSRunner getAsciidoctorJSRunnerFor(File asciidoctorjsExe, final String backend, Map<String, String> attributes) {
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

    private File resolveAsciidoctorjsExe() {
        asciidoctorJSResolver.getExecutable(asciidoctorjs.version).executable
    }

    private void runWithSubprocess(final File workingSourceDir, final Set<File> sourceFiles) {
        logger.info "Running Asciidoctor.js with subprocess."

        Map<String, List<File>> conversionGroups = sourceFileGroupedByRelativePath
        Map<String, String> finalAttributes = prepareAttributes(workingSourceDir)
        File asciidoctorjsExe = resolveAsciidoctorjsExe()
        Optional<List<String>> copyResources = getCopyResourcesForBackends()
        CopySpec rcs = resourceCopySpec

        for (String backend : configuredOutputOptions.backends) {
            conversionGroups.each { String relativePath, List<File> sourceGroup ->
                getAsciidoctorJSRunnerFor(asciidoctorjsExe, backend, finalAttributes).convert(sourceGroup.toSet(), relativePath)
            }
            if (copyResources.present && (copyResources.get().empty || backend in copyResources.get())) {
                copyResourcesByBackend(backend, workingSourceDir, getOutputDirFor(backend))
            }
        }
    }
}