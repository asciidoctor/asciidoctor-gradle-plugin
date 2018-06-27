/*
 * Copyright 2013-2018 the original author or authors.
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
package org.asciidoctor.gradle.remote

import groovy.transform.CompileStatic
import org.asciidoctor.Options
import org.asciidoctor.gradle.internal.ExecutorConfiguration
import org.asciidoctor.gradle.internal.ExecutorConfigurationContainer

import static groovy.lang.Closure.DELEGATE_ONLY

/** Base class for building claspath-isolated executors for Asciidoctor.
 *
 * @since 2.0.0
 * @author Schalk W. Cronjé
 */
@CompileStatic
class ExecutorBase {

    protected final List<ExecutorConfiguration> runConfigurations

    protected ExecutorBase(final ExecutorConfigurationContainer execConfig) {
        this.runConfigurations = execConfig.configurations
    }

    @SuppressWarnings('Instanceof')
    protected
    Map<String, Object> normalisedOptionsFor(final File file, ExecutorConfiguration runConfiguration) {

        Map<String, Object> mergedOptions = [:]

        runConfiguration.with {
            mergedOptions.putAll(options)
            mergedOptions.putAll([
                (Options.BACKEND) : backendName,
                (Options.IN_PLACE): false,
                (Options.SAFE)    : safeModeLevel,
                (Options.TO_DIR)  : outputDir.absolutePath
            ])

            mergedOptions[Options.BASEDIR] = (baseDir ?: file.parentFile).absolutePath

            if (mergedOptions.containsKey(Options.TO_FILE)) {
                Object toFileValue = mergedOptions[Options.TO_FILE]
                Object toDirValue = mergedOptions.remove(Options.TO_DIR)
                File toFile = toFileValue instanceof File ? (File) toFileValue : new File(toFileValue.toString())
                File toDir = toDirValue instanceof File ? (File) toDirValue : new File(toDirValue.toString())
                mergedOptions[Options.TO_FILE] = new File(toDir, toFile.name).absolutePath
            }

            // Note: Directories passed as relative to work around issue #83
            // Asciidoctor cannot handle absolute paths in Windows properly
            Map<String, Object> newAttrs = [:]
            newAttrs.putAll(attributes)
            newAttrs['gradle-projectdir'] = getRelativePath(projectDir, file.parentFile)
            newAttrs['gradle-rootdir'] = getRelativePath(rootDir, file.parentFile)
            mergedOptions[Options.ATTRIBUTES] = newAttrs
        }

        mergedOptions
    }

    /**
     * Returns the path of one File relative to another.
     *
     * @param target the target directory
     * @param base the base directory
     * @return target's path relative to the base directory
     * @throws IOException if an error occurs while resolving the files' canonical names
     */
    protected String getRelativePath(File target, File base) throws IOException {
        base.toPath().relativize(target.toPath()).toFile().toString()
    }

    protected List<Object> rehydrateExtensions(final Object registry, final List<Object> exts) {
        exts.collect {
            switch (it) {
                case Closure:
                    Closure rehydrated = ((Closure) it).rehydrate(registry, null, null)
                    rehydrated.resolveStrategy = DELEGATE_ONLY
                    (Object)rehydrated
                    break
                default:
                    it
            }
        } as List<Object>
    }
}
