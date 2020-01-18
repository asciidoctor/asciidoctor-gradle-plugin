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
package org.asciidoctor.gradle.slides.export.base

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.asciidoctor.gradle.base.AbstractAsciidoctorBaseTask
import org.asciidoctor.gradle.base.AsciidoctorUtils
import org.asciidoctor.gradle.base.Transform
import org.asciidoctor.gradle.base.internal.slides.ProfileUtils
import org.asciidoctor.gradle.base.slides.Profile
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.options.Option
import org.gradle.util.GradleVersion

import java.util.concurrent.Callable

import static org.gradle.api.tasks.PathSensitivity.RELATIVE

/** Base task for all HTML slide export tasks.
 *
 */
@CompileStatic
abstract class AbstractExportBaseTask extends DefaultTask {
    private final static boolean GRADLE_GE_4_8 = GradleVersion.current() >= GradleVersion.version('4.8')
    private final static String HTML_EXT = '.html'
    private final List<Object> slideInputFiles = []
    private Object outputDir
    private Callable<Profile> profileProvider
//    private Provider<Map<String, Object>> parametersProvider
    private Integer height
    private Integer width

    /** Set slide export/viewport height
     *
     * Can also be set via command-line using {@code --height}.
     *
     * @param height Integer value
     */
    @Option(description = 'Slide export height', option = 'height')
    void setHeight(String height) {
        this.height = height.toInteger()
    }

    /** Set slide export/viewport height
     *
     * Can also be set via command-line using {@code --height}.
     *
     * @param height Integer value
     */
    void setHeight(Integer height) {
        this.height = height
    }

    /** Slide export/viewport height.
     *
     * @return Height. Can be @{code null}.
     */
    @Optional
    @Input
    Integer getHeight() {
        this.height
    }

    /** Set slide export/viewport width
     *
     * Can also be set via command-line using {@code --width}.
     *
     * @param height Integer value
     */
    @Option(description = 'Slide export width', option = 'width')
    void setWidth(String width) {
        this.width = width.toInteger()
    }

    /** Set slide export/viewport width
     *
     * @param height Integer value
     */
    void setWidth(Integer width) {
        this.width = width
    }

    /** Slide export/viewport width.
     *
     * @return Width. Can be {@code null}.
     */
    @Optional
    @Input
    Integer getWidth() {
        this.width
    }

    /** Set output directory
     *
     * @param out Any resolvable via {@link org.gradle.api.Project#file}.
     */
    void setOutputDir(Object out) {
        this.outputDir = out
    }

    /** Directory where output will be written to.
     *
     * @return Outpur directory
     */
    @OutputDirectory
    @PathSensitive(RELATIVE)
    File getOutputDir() {
        project.file(this.outputDir)
    }

    /** Set the slides that needs converting.
     *
     * Override any previous settings.
     *
     * @param files Anything that can be converted using {@link org.gradle.api.Project#files}.
     *    Most convenient is to use an Asciidoctor task that provide HTML slides.
     */
    void setSlides(Iterable<Object> files) {
        this.slideInputFiles.clear()
        this.slideInputFiles.addAll(files)
        checkTaskDependencies(files)
    }

    /** Adds more slide index files that need converting.
     *
     * @param files Anything that can be converted using {@link org.gradle.api.Project#files}.
     */
    void slides(Object... files) {
        this.slideInputFiles.addAll(files)
        checkTaskDependencies(files as List)
    }

    /** Adds more slide index files that need converting.
     *
     * @param files Anything that can be converted using {@link org.gradle.api.Project#files}.
     */
    void slides(Iterable<Object> files) {
        this.slideInputFiles.addAll(files)
        checkTaskDependencies(files)
    }

    /** Slide files that will be converted.
     *
     * @return List of Slide index files.
     */
    @InputFiles
    @SkipWhenEmpty
    @PathSensitive(RELATIVE)
    Provider<Set<File>> getSlides() {
        if (slideInputFiles.empty) {
            project.providers.provider { [] as Set<File> }
        } else {
            project.providers.provider {
                List<Object> resolvedInputs = Transform.toList(slideInputFiles) { input ->
                    input instanceof Provider ? ((Provider) input).get() : input
                }

                List asciidoctorTasks = resolvedInputs.findAll { input ->
                    input instanceof AbstractAsciidoctorBaseTask
                } as List<AbstractAsciidoctorBaseTask>

                List<File> taskFiles = asciidoctorTasks.collectMany { inputTask ->
                    File taskOutputDir = inputTask.outputDir
                    File sourceDir = inputTask.sourceDir
                    (Collection) Transform.toList(inputTask.sourceFileTree.files) { File input ->
                        String path = AsciidoctorUtils.getRelativePath(input, sourceDir)
                        String outputPath = path.lastIndexOf('.').with { it == -1 ? path : path[0..<it]  } + HTML_EXT
                        new File(taskOutputDir, outputPath)
                    }
                }
                FileCollection otherFc = project.files(resolvedInputs - asciidoctorTasks).filter { File target ->
                    target.name.toLowerCase(Locale.US).endsWith(HTML_EXT)
                }

                taskFiles.toSet() + otherFc.files
            }
        }
    }

    /** Set the slide profile to be used.
     *
     * @param profile {@link Profile}
     */
    void setProfile(Profile profile) {
        this.profileProvider = { profile } as Callable<Profile>
    }

    /** Set the slide profile to be used via a lazy-evaluated callback.
     *
     * @param profile A {@link Callable} that should return a {@link Profile}
     */
    void setProfile(Callable<Profile> profile) {
        this.profileProvider = profile
    }

    /** Set the slide profile to be used as a string
     *
     * @param profileName
     *
     * @see {@link ProfileUtils#findMatch} for algorithm.
     */
    void setProfile(String profileName) {
        profile = ProfileUtils.findMatch(profileName)
    }

    /** The resolved profile
     *
     * @return Resovled profile or {@code null} if no profile was set
     */
    @Internal
    Profile getProfile() {
        Profile resolvedProfile = profileProvider ? profileProvider.call() : null
        if (resolvedProfile) {
            if (resolvedProfile in supportedProfiles) {
                resolvedProfile
            } else {
                throw new ProfileNotSupportedException("${resolvedProfile.name} is not supported by ${name}")
            }
        } else {
            null
        }
    }

    /** The list of profiles supported by this conversion task.
     *
     * @return List of profiles. Never {@code null} or empty.
     */
    @Internal
    abstract protected List<Profile> getSupportedProfiles()

    /** Provides the outputfile name given an input file name
     *
     * @param input Input file
     * @return Formatted output file name
     */
    abstract protected String outputFileNameFromInput(File input)

    @CompileDynamic
    private void checkTaskDependencies(Iterable<Object> tasks) {
        dependsOn tasks.findAll {
            if (it instanceof Task) {
                true
            } else if (GRADLE_GE_4_8) {
                it instanceof org.gradle.api.tasks.TaskProvider
            } else {
                false
            }
        }.toSet()
    }
}
