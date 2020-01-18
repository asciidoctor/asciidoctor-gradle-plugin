/*
 * Copyright 2013-2020 the original author or authors.
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

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileTree
import org.gradle.api.file.FileVisitDetails
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.specs.Spec
import org.gradle.api.tasks.util.PatternSet
import org.gradle.util.GradleVersion
import org.ysb33r.grolifant.api.OperatingSystem

import java.nio.file.Path

import static groovy.lang.Closure.DELEGATE_FIRST

/** Utility methods used internally by Asciidoctor plugins.
 *
 * @author Schalk W. Cronjé
 * @author Gary Hale
 *
 * @since 2.0.0
 */
@CompileStatic
class AsciidoctorUtils {

    public static final OperatingSystem OS = OperatingSystem.current()
    public static final String UNDERSCORE_LED_FILES = '**/_*'
    public static final PatternSet UNDERSCORE_LED_PATTERN = new PatternSet().include(UNDERSCORE_LED_FILES)
    public static final boolean GRADLE_LT_5_0 = GradleVersion.current() < GradleVersion.version('5.0')
    public static final boolean GRADLE_LT_5_1 = GradleVersion.current() < GradleVersion.version('5.1')

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
     * @return A collection of suitable files.
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

    /** Apply convention in way that is backward-comatible to Gradle 4.3, but utilises features
     * when available in later Gradle releases.
     *
     * @param project Current project context.
     * @param property Directory property to which convention should be applied
     * @param value Default value of directory.
     *
     * @since 3.0
     */
    static void setConvention(Project project, Property<Directory> property, Directory value) {
        Property<Directory> defaultProvider
        // BuildLayout.directoryProperty was replaced with ObjectFactory.directoryProperty() in Gradle 5.0
        defaultProvider = createDirectoryProperty(project)
        defaultProvider.set(value)
        setConvention(property, defaultProvider)
    }

    /** Sets a property convention using a provider in a way that is compatible back to Gradle 4.3.
     *
     * @param property Property to set
     * @param value Provider to use
     *
     * @since 3.0
     */
    static <T> void setConvention(Property<T> property, Provider<T> value) {
        if (GRADLE_LT_5_1) {
            doSetPropertyConventionPre51(property, value)
        } else {
            doSetPropertyConvention(property, value)
        }
    }

    /** Maps a file object to a directory provider
     *
     * @param project Project context
     * @param value Anything convertible with {@code project.file}
     * @return {@link Provider} of a {@link Directory}.
     *
     * @since 3.0
     */
    static Provider<Directory> mapToDirectoryProvider(Project project, Object value) {
        // There's no good way to construct a Directory from a File in Gradle before 6.0
        // In 6.0, we can use ProjectLayout.dir(Provider<File>) instead.
        project.providers.provider {
            DirectoryProperty dir = createDirectoryProperty(project)
            dir.set(project.file(value))
            dir.get()
        }
    }

    /** Creates a {@link DirectoryProperty} instance in a way that is backwards-comaptible to
     * Gradle 4.3.
     *
     * @param project Current project context
     * @return {@code DirectoryProperty} instance
     *
     * @since 3.0
     */
    static DirectoryProperty createDirectoryProperty(Project project) {
        if (GRADLE_LT_5_0) {
            doCreateDirectoryPropertyPre50(project)
        } else {
            doCreateDirectoryProperty(project)
        }
    }

    @CompileDynamic
    private static DirectoryProperty doCreateDirectoryPropertyPre50(Project project) {
        project.layout.directoryProperty()
    }

    @CompileDynamic
    private static DirectoryProperty doCreateDirectoryProperty(Project project) {
        project.objects.directoryProperty()
    }

    @CompileDynamic
    private static <T> void doSetPropertyConventionPre51(Property<T> property, Provider<T> value) {
        if (!property.isPresent()) {
            property.set(value)
        }
    }

    @CompileDynamic
    private static <T> void doSetPropertyConvention(Property<T> property, Provider<T> value) {
        property.convention(value)
    }
}
