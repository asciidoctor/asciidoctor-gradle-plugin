/*
 * Copyright 2013-2024 the original author or authors.
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

/**
 * Sets AsciidoctorJ runtime.
 *
 * @author Schalk W. Cronjé
 *
 * @since 4.0
 */
@CompileStatic
class AsciidoctorJSetup implements Serializable {
    public final static String ATTR_PROJECT_DIR = 'gradle-projectdir'
    public final static String ATTR_ROOT_DIR = 'gradle-rootdir'
    public final static String ATTR_REL_SRC_DIR = 'gradle-relative-srcdir'

    /**
     * Returns the path of one File relative to another.
     *
     * @param target the target directory
     * @param base the base directory
     * @return target's path relative to the base directory
     * @throws IOException if an error occurs while resolving the files' canonical names
     */
    String getRelativePath(File target, File base) throws IOException {
        base.toPath().relativize(target.toPath()).toFile().toString()
    }

    /**
     * Normalises Asciidoctor options for a given source file.
     *
     * Relativizes certain attributes and ensure specific options for backend, sage mode and output
     * directory are in place.
     *
     * @param file Source file to be converted
     * @param runConfiguration The current executor configuration
     * @return Asciidoctor options
     */
    @SuppressWarnings('DuplicateStringLiteral ')
    Map<String, Object> normalisedOptionsFor(final File file, ExecutorConfiguration runConfiguration) {
        Map<String, Object> mergedOptions = [:]

        runConfiguration.with {
            final String srcRelative = getRelativePath(file.parentFile, sourceDir)

            mergedOptions.putAll(options)
            mergedOptions.putAll([
                    (Options.BACKEND) : backendName,
                    (Options.IN_PLACE): false,
                    (Options.SAFE)    : safeModeLevel,
                    (Options.TO_DIR)  : (srcRelative.empty ? outputDir : new File(outputDir, srcRelative)).absolutePath,
                    (Options.MKDIRS)  : true
            ])

            mergedOptions[Options.BASEDIR] = (baseDir ?: file.parentFile).absolutePath

            if (mergedOptions.containsKey(Options.TO_FILE)) {
                Object toFileValue = mergedOptions[Options.TO_FILE]
                Object toDirValue = mergedOptions.remove(Options.TO_DIR)
                File toFile = toFileValue instanceof File ? (File) toFileValue : new File(toFileValue.toString())
                File toDir = toDirValue instanceof File ? (File) toDirValue : new File(toDirValue.toString())
                mergedOptions[Options.TO_FILE] = new File(toDir, toFile.name).absolutePath
            }

            Map<String, Object> newAttrs = [:]
            newAttrs.putAll(attributes)
            newAttrs[ATTR_PROJECT_DIR] = projectDir.absolutePath
            newAttrs[ATTR_ROOT_DIR] = rootDir.absolutePath
            newAttrs[ATTR_REL_SRC_DIR] = getRelativePath(sourceDir, file.parentFile) ?: '.'

            if (legacyAttributes) {
                newAttrs['projectdir'] = newAttrs[ATTR_PROJECT_DIR]
                newAttrs['rootdir'] = newAttrs[ATTR_ROOT_DIR]
            }

            mergedOptions[Options.ATTRIBUTES] = newAttrs
        }

        mergedOptions
    }

    /**
     * Rehydrates docExtensions that were serialised.
     *
     * @param registry Asciidoctor GroovyDSL registry instance.
     * @param exts List of docExtensions to rehydrate.
     * @return List of rehydrated extensions
     */
    List<Object> rehydrateExtensions(final Object registry, final List<Object> exts) {
        final List<Object> availableExtensions = []
        for (Object ext in exts) {
            switch (ext) {
                case Closure:
                    Closure rehydrated = ((Closure) ext).rehydrate(registry, null, null)
                    rehydrated.resolveStrategy = Closure.DELEGATE_ONLY
                    availableExtensions.add((Object) rehydrated)
                    break
                default:
                    availableExtensions.add(ext)
            }
        }
        availableExtensions
    }
}
