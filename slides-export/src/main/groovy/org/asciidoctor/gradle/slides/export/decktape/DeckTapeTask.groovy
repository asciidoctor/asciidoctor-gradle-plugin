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
package org.asciidoctor.gradle.slides.export.decktape

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.base.AsciidoctorUtils
import org.asciidoctor.gradle.base.Transform
import org.asciidoctor.gradle.base.slides.Profile
import org.asciidoctor.gradle.js.nodejs.AsciidoctorJSNodeExtension
import org.asciidoctor.gradle.js.nodejs.AsciidoctorJSNpmExtension
import org.asciidoctor.gradle.slides.export.base.AbstractExportBaseTask
import org.gradle.api.Action
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.process.ExecSpec
import org.ysb33r.gradle.nodejs.utils.NodeJSExecutor

import static org.asciidoctor.gradle.base.slides.Profile.BESPOKE
import static org.asciidoctor.gradle.base.slides.Profile.DECK_JS
import static org.asciidoctor.gradle.base.slides.Profile.DZ
import static org.asciidoctor.gradle.base.slides.Profile.FLOWTIME_JS
import static org.asciidoctor.gradle.base.slides.Profile.GENERIC
import static org.asciidoctor.gradle.base.slides.Profile.IMPRESS_JS
import static org.asciidoctor.gradle.base.slides.Profile.REMARK_JS
import static org.asciidoctor.gradle.base.slides.Profile.REVEAL_JS
import static org.asciidoctor.gradle.js.nodejs.core.NodeJSUtils.initPackageJson
import static org.ysb33r.grolifant.api.ClosureUtils.configureItem

/** Conversion task that will convert from a set of
 * HTML slides to PDF using <a href="https://github.com/astefanutti/decktape">decktape</a>.
 *
 * @author Schalk W. Cronjé
 *
 * @since 3.0
 */
@CompileStatic
@CacheableTask
class DeckTapeTask extends AbstractExportBaseTask {

    private static final List<Profile> PROFILES = [
            BESPOKE, DECK_JS, DZ, REMARK_JS, FLOWTIME_JS, REVEAL_JS, IMPRESS_JS, GENERIC
            // csss, slidy, shower, webslides
    ]

    public static final String NONE = null
    public static final String JPG = 'jpg'
    public static final String PNG = 'png'

    @Nested
    final ScreenShots screenshots = new ScreenShots()

    private String genericKeyStroke = 'ArrowRight'
    private final List<String> chromeArgs = []
    private final DeckTapeExtension decktape
    private String range
    private Integer interSlidePause
    private Integer loadPause

    static class ScreenShots {

        private String format = NONE
        private Integer height
        private Integer width

        @Input
        @Optional
        String getFormat() {
            this.format
        }

        /** Screen export image format
         *
         * @param form {@link DeckTapeTask#NONE}, {@link DeckTapeTask#JPG} or {@@link DeckTapeTask#PNG}.
         * {@code jpg} or {@code png} is also acceptable
         */
        void setFormat(String form) {
            if (form) {
                String lcForm = form.toLowerCase(Locale.US)
                if (lcForm != JPG && lcForm != PNG) {
                    throw new IllegalArgumentException('Export format should be png or jpg')
                }
                this.format = lcForm
            } else {
                this.format = NONE
            }
        }

        /** Slide export image width
         *
         * @return Height as an integer
         */
        @Input
        @Optional
        Integer getHeight() {
            this.height
        }

        /** Set slide export image height.
         *
         * @param h Height
         */
        void setHeight(String h) {
            this.height = h.toInteger()
        }

        /** Set slide export image height.
         *
         * @param h Height
         */
        void setHeight(Integer h) {
            this.height = h
        }

        /** Slide export image width
         *
         * @return Width as an integer
         */
        @Input
        @Optional
        Integer getWidth() {
            this.width
        }

        /** Set slide export image width.
         *
         * @param h Width
         */
        void setWidth(String h) {
            this.width = h.toInteger()
        }

        /** Set slide export image width.
         *
         * @param h Width
         */
        void setWidth(Integer h) {
            this.width = h
        }
    }

    DeckTapeTask() {
        super()
        decktape = project.extensions.getByType(DeckTapeExtension)
    }

    /** Use a generic profile.
     *
     * @param keystroke Keystroke to use
     *   See <a href="https://www.w3.org/TR/uievents-key/">UI Events KeyboardEvent key Values</a>.
     *   If not specified, it defaults to <a href="https://www.w3.org/TR/uievents-key/#keys-navigation">ArrowRight</a>.
     */
    void useGenericProfile(String keystroke = 'ArrowRight') {
        profile = GENERIC
        genericKeyStroke = keystroke
    }

    /** The name that decktape uses to identify a profile.
     *
     * @return
     */
    @Input
    String getDeckTapeProfileName() {
        profile?.deckTapeShortName
    }

    /** Set range of slides to be exported.
     *
     * Can also be set from the command-line  using {@code --range <range>}
     * .
     * @param rangeFormat E.g. {@code 1-3,5,8}
     */
    @Option(option = 'range', description = "Range of slides to be exported e.g. '1-3,5,8'")
    void setRange(String rangeFormat) {
        this.range = rangeFormat
    }

    /** Range of slides to export.
     *
     * @return Range. Can be {@code null} whrn all slides are to be exported.
     */
    @Input
    @Optional
    String getRange() {
        this.range
    }

    /** Delay between each slide that is exported.
     *
     * Can also be set form the command-line using {@code --pause <mss>}
     *
     * @param ms Delay in milliseconds
     */
    @Option(option = 'pause', description = 'Delay in milliseconds between each slide being exported')
    void setInterSlidePause(String ms) {
        this.interSlidePause = ms.toInteger()
    }

    /** Delay between each slide that is exported.
     *
     * @param ms Delay in milliseconds
     */
    void setInterSlidePause(Integer ms) {
        this.interSlidePause = ms
    }

    /** Delay between slides being exported.
     *
     * @return Delay in milliseconds. Can be {@code null}.
     */
    @Internal
    Integer getInterSlidePause() {
        this.interSlidePause
    }

    /** Set delay between the page having loaded and the start of the slide export.
     *
     * Can also be set from the command-line via {@code --load-pause <delay>}.
     *
     * @param ms Delay in milliseconds.
     */
    @Option(option = 'load-pause',
            description = 'Delay in milliseconds between the page has loaded and starting to export slides')
    void setLoadPause(String ms) {
        this.loadPause = ms.toInteger()
    }

    /** Set delay between the page having loaded and the start of the slide export.
     *
     * @param ms Delay in milliseconds.
     */
    void setLoadPause(Integer ms) {
        this.loadPause = ms
    }

    /** Delay between the page having loaded and the start of the slide export.
     *
     * @return Delay in milliseconds. Can be {@code null}
     */
    @Internal
    Integer getLoadPause() {
        this.loadPause
    }

    /** Configures screenshots.
     *
     * @param cfg Configurating closure.
     */
    void screenshots(@DelegatesTo(ScreenShots) Closure cfg) {
        configureItem(this.screenshots, cfg)
    }

    /** Configures screenshots.
     *
     * @param cfg Configurating action
     */
    void screenshots(Action<ScreenShots> cfg) {
        cfg.execute(this.screenshots)
    }

    /** Add additional arguments to pass to Chrome/Chromium instance.
     *
     * @param args Chrome/Chromium arguments
     *
     * @see https://peter.sh/experiments/chromium-command-line-switches
     */
    void chromeArgs(String... args) {
        this.chromeArgs.addAll(args as List)
    }

    /** Replace existing Chrome/Chromium arguments with a new set.
     *
     * @param args Chrome/Chromium arguments
     *
     * @see https://peter.sh/experiments/chromium-command-line-switches
     */
    void setChromeArgs(List<String> args) {
        this.chromeArgs.clear()
        this.chromeArgs.addAll(args)
    }

    /** List of Chrome/Chromium arguments
     *
     * @return Additional arguments. Empty by default. Never {@code null}.
     */
    @Internal
    List<String> getChromeArgs() {
        this.chromeArgs
    }

    @SuppressWarnings('UnnecessaryGetter')
    @TaskAction
    void exec() {
        List<String> resolvedArgs = buildOptions()
        File outputDirCached = getOutputDir()

        List<String> profileToUse = [deckTapeProfileName]
        if (deckTapeProfileName == 'generic') {
            profileToUse.addAll('--key', genericKeyStroke)
        }

        File home = decktape.toolingWorkDir
        File decktapeExecutable = new File(home, 'node_modules/decktape/decktape.js')
        File nodejs = project.extensions.getByType(AsciidoctorJSNodeExtension).resolvableNodeExecutable.executable

        Closure configurator = { File sourceFile, File destFile, ExecSpec spec ->
            spec.with {
                environment = NodeJSExecutor.defaultEnvironment
                workingDir = home
                executable nodejs
                args(decktapeExecutable.absolutePath)
                args(resolvedArgs)
                args(profileToUse)
                args(sourceFile.absoluteFile.toURI())
                args(destFile.absolutePath)
            }
        }

        initPackageJson(
                home,
                "${project.name}-${name}",
                project,
                project.extensions.getByType(AsciidoctorJSNodeExtension),
                project.extensions.getByType(AsciidoctorJSNpmExtension)
        )

        decktape.configuration.resolve()
        Set<File> convertibles = slides.get()
        convertibles.each { File source ->
            File destination = new File(outputDirCached, outputFileNameFromInput(source))
            project.exec((Closure) configurator.curry(source, destination))
        }
    }

    @Override
    @Internal
    protected List<Profile> getSupportedProfiles() {
        PROFILES
    }

    /** Provides the outputfile name
     *
     * @param input Input file
     * @return Formatted output file name
     */
    @Override
    protected String outputFileNameFromInput(File input) {
        "${input.name.replaceFirst(~/\.html$/, '')}.pdf"
    }

    /** Build decktape command-line options excluding profile, source and destination file
     *
     * @return List of option
     */
    @SuppressWarnings('DuplicateStringLiteral')
    private List<String> buildOptions() {
        if (height && !width || !height && width) {
            throw new IllegalArgumentException('Must specify both height and width, not just one.')
        }

        List<String> args = []
        if (range) {
            args.addAll('--slides', range)
        }
        if (height) {
            args.addAll(['--size', "${width}x${height}".toString()])
        }
        if (loadPause) {
            args.addAll('--load-pause', loadPause.toString())
        }
        if (interSlidePause) {
            args.addAll('--pause', interSlidePause.toString())
        }
        if (screenshots.format != NONE) {
            args.addAll(
                    '--screenshots',
                    '--screenshots-directory', AsciidoctorUtils.getRelativePathToFsRoot(decktape.toolingWorkDir),
                    '--screenshots-format', screenshots.format
            )

            if (screenshots.height && !screenshots.width || !screenshots.height && screenshots.width) {
                throw new IllegalArgumentException('Must specify both height and width for screenshots, not just one.')
            }

            if (screenshots.height) {
                args.addAll(['--screenshots-size', "${screenshots.width}x${screenshots.height}".toString()])
            } else if (height) {
                args.addAll(['--screenshots-size', "${width}x${height}".toString()])
            }
        }

        if (this.chromeArgs) {
            args.addAll(Transform.toList(this.chromeArgs) { "--chrome-arg=${it}".toString() })
        }

        args
    }
}
