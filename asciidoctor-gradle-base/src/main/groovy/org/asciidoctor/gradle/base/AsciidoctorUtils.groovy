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
package org.asciidoctor.gradle.base

import groovy.transform.CompileStatic
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.FileTree
import org.gradle.api.file.FileVisitDetails
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.util.PatternSet
import org.ysb33r.grolifant.api.OperatingSystem

import java.nio.file.Path

import static groovy.lang.Closure.DELEGATE_FIRST

/** Utility methods used internally by Asciidoctor plugins.
 *
 * @author Schalk W. Cronjé
 *
 * @since 2.0.0
 */
@CompileStatic
class AsciidoctorUtils {

    public static final OperatingSystem OS = OperatingSystem.current()
    public static final String UNDERSCORE_LED_FILES = '**/_*.a*'

    static final Spec<? super File> ACCEPT_ONLY_FILES = new Spec<File>() {
        @Override
        boolean isSatisfiedBy(File element) {
            element.isFile()
        }
    }

    private static final String DOUBLE_BACKLASH = '\\\\'
    private static final String BACKLASH = '\\'

    /** Gets a fileTree that described an Asciidoctor set of source files.
     *
     * @param project Project to associate the file collection tp.
     * @param sourceDir Base directory for the sourcs.
     * @param filePatterns Patterns to use to identify suitable sources.
     * @return A colelction of suitable files.
     * @throw {@link GradleException} is files starting with undersocres are detected.
     */
    static FileTree getSourceFileTree(final Project project, final File sourceDir, final PatternSet filePatterns) {
        FileTree ft = project.fileTree(sourceDir).
            matching(filePatterns).filter(ACCEPT_ONLY_FILES).asFileTree

        ft.visit { FileVisitDetails it ->
            if (it.name.startsWith('_')) {
                throw new GradleException("Sources starting with '_' found. This is not allowed. " +
                    "Current sources are: ${ft.files}")
            }
        }

        ft
    }
    /*
     */
    /** Normalises slashes in a path.
     *
     * @param path
     * @return Slashes chanegs to backslahes no Windows, unahcnges otherwise.
     */
    static String normalizePath(String path) {
        if (OS.windows) {
            path.replace(DOUBLE_BACKLASH, BACKLASH).replace(BACKLASH, DOUBLE_BACKLASH)
        } else {
            path
        }
    }

    /**
     * Returns the path of one File relative to another.
     *
     * @param target the target directory
     * @param base the base directory
     * @return target's path relative to the base directory
     * @throws IOException if an error occurs while resolving the files' canonical names
     */
    static String getRelativePath(File target, File base) throws IOException {
        base.toPath().relativize(target.toPath()).toFile().toString()
    }

    /** Get relative path to the current filesystem root
     *
     * @param target The target directory
     * @return taget's pat relative to the filesystem root
     *
     * @since 3.0
     */
    static String getRelativePathToFsRoot(File target) {
        Path path = target.toPath()
        path.relativize(path.root).toString()
    }

    /** Executes a configuration closure.
     *
     * The closure will be cloned before execution.
     *
     * @param delegated Closure delegate
     * @param cfg Closure to execute
     */
    static void executeDelegatingClosure(Object delegated, Closure cfg) {
        Closure configuration = (Closure) cfg.clone()
        configuration.resolveStrategy = DELEGATE_FIRST
        configuration.delegate = delegated
        configuration.call(delegated)
    }

    /** Returns the location of a class
     *
     * @param aClass Class to look for.
     * @return Location as a file on disk.
     */
    static File getClassLocation(Class aClass) {
        new File(aClass.protectionDomain.codeSource.location.toURI()).absoluteFile
    }

}
