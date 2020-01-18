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
package org.asciidoctor.gradle.jvm.slides

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.CopySpec
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.ysb33r.grolifant.api.StringUtils

import java.nio.file.FileSystems
import java.nio.file.Path

import static org.gradle.api.tasks.PathSensitivity.RELATIVE
import static org.ysb33r.grolifant.api.MapUtils.stringizeValues
import static org.ysb33r.grolifant.api.StringUtils.stringize

/** Options for Reveal.js slides.
 *
 * @since 2.0
 */
@CompileStatic
@SuppressWarnings('MethodCount')
class RevealJSOptions {

    private Optional<String> transition = Optional.empty()
    private Optional<String> backgroundTransition = Optional.empty()
    private Optional<String> transitionSpeed = Optional.empty()

    private Optional<Boolean> controls = Optional.empty()
    private Optional<Boolean> progressBar = Optional.empty()
    private Optional<String> slideNumber = Optional.empty()
    private Optional<Boolean> pushToHistory = Optional.empty()
    private Optional<Boolean> keyboardShortcuts = Optional.empty()
    private Optional<Boolean> overviewMode = Optional.empty()
    private Optional<Boolean> touchMode = Optional.empty()
    private Optional<Boolean> verticalCenter = Optional.empty()
    private Optional<Boolean> loop = Optional.empty()
    private Optional<Boolean> rightToLeft = Optional.empty()
    private Optional<Boolean> fragments = Optional.empty()
    private Optional<Boolean> flagEmbedded = Optional.empty()
    private Optional<Boolean> autoSlideStoppable = Optional.empty()
    private Optional<Boolean> mouseWheel = Optional.empty()
    private Optional<Boolean> hideAddressBarOnMobile = Optional.empty()
    private Optional<Boolean> previewLinks = Optional.empty()

    private Optional<Integer> autoSlideInterval = Optional.empty()
    private Optional<Integer> viewDistance = Optional.empty()

    private Object parallaxBackgroundSize
    private Object parallaxBackgroundImage
    private Object parallaxBackgroundImageRelativePath = 'images'
    private Object highlightJsTheme
    private Object highlightJsThemeRelativePath = 'css'
    private Object customTheme
    private Object customThemeRelativePath = 'style'

    final private Project project

    @SuppressWarnings('ClassName')
    enum Transition {
        NONE,
        FADE,
        SLIDE,
        CONVEX,
        CONCAVE,
        ZOOM
    }

    @SuppressWarnings('ClassName')
    enum TransitionSpeed {
        DEFAULT,
        FAST,
        SLOW
    }

    enum SlideNumber {
        /**
         * Hide the slide number.
         */
        NONE('false'),
        /**
         * h.v: horizontal . vertical slide number (default)
         */
        DEFAULT('h.v'),
        /**
         * h/v: horizontal / vertical slide number
         */
        HORIZONTAL_VERTICAL('h/v'),
        /**
         * c: flattened slide number
         */
        COUNT('c'),
        /**
         * c/t: flattened slide number / total slides
         */
        COUNT_TOTAL('c/t')

        final private String value

        private SlideNumber(String value) {
            this.value = value
        }

        static SlideNumber slideNumber(String value) {
            switch (value) {
                case 'h/v': return HORIZONTAL_VERTICAL
                case 'c': return COUNT
                case 'c/t': return COUNT_TOTAL
                case null:
                case 'false':
                case 'none': return NONE
                default: return DEFAULT
            }
        }

        static SlideNumber slideNumber(boolean value) {
            value ? DEFAULT : NONE
        }

        String getValue() {
            this.value
        }
    }

    RevealJSOptions(Project project) {
        this.project = project
    }

    /** Display controls in the bottom right corner.
     *
     */
    void setControls(Boolean b) {
        this.controls = Optional.ofNullable(b)
    }

    /** Display a presentation progress bar.
     *
     */
    void setProgressBar(Boolean b) {
        this.progressBar = Optional.ofNullable(b)
    }

    /** Display the slide number of the current slide.
     *
     */
    void setSlideNumber(boolean b) {
        this.slideNumber = Optional.of(b.toString())
    }

    /** Display the slide number of the current slide.
     *
     */
    void setSlideNumber(SlideNumber slideNumber) {
        this.slideNumber = Optional.of(slideNumber.value)
    }

    /**
     * Display the slider number of the current slide. <br/>
     * The String "true" will display the slide number with default formatting. <br/>
     * Additional formatting is available:
     * <ul>
     *     <li>h.v: horizontal . vertical slide number (default)</li>
     *     <li>h/v: horizontal / vertical slide number</li>
     *     <li>c: flattened slide number</li>
     *     <li>c/t: flattened slide number / total slides</li>
     * </ul>
     */
    void setSlideNumber(String b) {
        this.slideNumber = Optional.ofNullable(b)
    }

    /** Push each slide change to the browser history.
     *
     */
    void setPushToHistory(Boolean b) {
        this.pushToHistory = Optional.ofNullable(b)
    }

    /** Enable keyboard shortcuts for navigation.
     *
     */
    void setKeyboardShortcuts(Boolean b) {
        this.keyboardShortcuts = Optional.ofNullable(b)
    }

    /** Enable the slide overview mode.
     *
     */
    void setOverviewMode(Boolean b) {
        this.overviewMode = Optional.ofNullable(b)
    }

    /** Enables touch navigation on devices with touch input.
     *
     */
    void setTouchMode(Boolean b) {
        this.touchMode = Optional.ofNullable(b)
    }

    /** Vertical centering of slides.
     *
     */
    void setVerticalCenter(Boolean b) {
        this.verticalCenter = Optional.ofNullable(b)
    }

    /** Loop the presentation..
     *
     */
    void setLoop(Boolean b) {
        this.loop = Optional.ofNullable(b)
    }

    /** Change the presentation direction to be RTL.
     *
     */
    void setRightToLeft(Boolean b) {
        this.rightToLeft = Optional.ofNullable(b)
    }

    /** Use fragments globally.
     *
     */
    void setFragments(Boolean b) {
        this.fragments = Optional.ofNullable(b)
    }

    /** Flags if the presentation is running in an embedded mode ( contained within a limited portion of the screen ).
     *
     */
    void setFlagEmbedded(Boolean b) {
        this.flagEmbedded = Optional.ofNullable(b)
    }

    /** Delay in milliseconds between automatically proceeding to the next slide.
     *
     * Disabled when set to {@code 0}(the default). This value can still be overwritten
     * on a per-slide basis by setting a {@code data-autoslide} attribute on a slide.
     *
     */
    void setAutoSlideInterval(Integer ms) {
        if (ms != null && ms < 0) {
            throw new GradleException('Slide interval cannot be less than 0')
        }
        this.autoSlideInterval = Optional.ofNullable(ms)
    }

    /** Stop auto-sliding after user input
     *
     */
    void setAutoSlideStoppable(Boolean b) {
        this.autoSlideStoppable = Optional.ofNullable(b)
    }

    /** Enable slide navigation via mouse wheel.
     *
     */
    void setMouseWheel(Boolean b) {
        this.mouseWheel = Optional.ofNullable(b)
    }

    /** Hides the address bar on mobile devices.
     *
     */
    void setHideAddressBarOnMobile(Boolean b) {
        this.hideAddressBarOnMobile = Optional.ofNullable(b)
    }

    /* Opens links in an iframe preview overlay.
     *
     */

    void setPreviewLinks(Boolean b) {
        this.previewLinks = Optional.ofNullable(b)
    }

    /** Number of slides away from the current that are visible.
     *
     * If not set, Revels.JS will use an internal value of 3.
     */
    void setViewDistance(Integer numSlides) {
        if (numSlides != null && numSlides < 0) {
            throw new GradleException('viewDistance cannot be less than 0')
        }
        this.viewDistance = Optional.ofNullable(numSlides)
    }

    /** Transition style.
     *
     * If not set, the Reveal.JS will use an internal default of {@code SLIDE}.
     */
    void setTransition(final String s) {
        this.transition = Optional.ofNullable(s?.toLowerCase())
    }

    /** Transition style.
     *
     * If not set, the Reveal.js will use an internal default of {@code SLIDE}.
     */
    void setTransition(RevealJSOptions.Transition tr) {
        this.transition = Optional.ofNullable(tr?.toString()?.toLowerCase())
    }

    /** Transition style for full page slide backgrounds..
     *
     * One of {@code none , fade, slide, convex, concave, zoom}.
     *
     * If not set then Reveal.js will use an internal default of {@code FADE}.
     */
    void setBackgroundTransition(final String s) {
        this.backgroundTransition = Optional.ofNullable(s?.toLowerCase())
    }

    /** Transition style for full page slide backgrounds..
     *
     * One of {@code none , fade, slide, convex, concave, zoom}.
     *
     * If not set then Reveal.js will use an interna default of {@code FADE}.
     */
    void setBackgroundTransition(RevealJSOptions.Transition tr) {
        this.backgroundTransition = Optional.ofNullable(tr?.toString()?.toLowerCase())
    }

    /** Slide transition speed
     *
     * One of {@code default , fast, slow}.
     *
     * If not provided, Reveal.js wil use an internal default value of {@code DEFAULT}.
     */
    void setTransitionSpeed(final String s) {
        this.transitionSpeed = Optional.ofNullable(s?.toLowerCase())
    }

    /** Slide transition speed
     *
     * One of {@code default , fast, slow}.
     *
     * If not provided, Reveal.js wil use an internal default value of {@code DEFAULT}.
     */
    void setTransitionSpeed(RevealJSOptions.TransitionSpeed tr) {
        this.transitionSpeed = Optional.ofNullable(tr?.toString()?.toLowerCase())
    }

    /** Parallax background size (accepts any CSS syntax).
     *
     * @return CSS syntax. Can be {@code null}.
     */
    @org.gradle.api.tasks.Optional
    @Input
    String getParallaxBackgroundSize() {
        this.parallaxBackgroundSize ? stringize(this.parallaxBackgroundSize) : null
    }

    /** Set the Parallax background size.
     *
     * @param css Anything that can be converted to a String via {@link org.ysb33r.grolifant.api.StringUtils#stringize}.
     *   Must be in CSS syntax.
     */
    void setParallaxBackgroundSize(Object css) {
        this.parallaxBackgroundSize = css
    }

    /** Set Parallax background image as file or URI.
     *
     * @param fileOrUri Object that can be converted to a file or a URI. If the initial object is not a URI,
     *   but it's string presentation starts with http(s) it will be created as a URI. Anything else is a path to a
     *   local file.
     */
    void setParallaxBackgroundImageLocation(Object fileOrUri) {
        this.parallaxBackgroundImage = fileOrUri
    }

    /** If the parallax image is set as a file, this will be the relative path to which it will be copied
     * when resources are copied.
     *
     * @param relPath Relative path can be anything convertible to a string using {@link StringUtils#stringize}.
     */
    void setParallaxBackgroundImageRelativePath(Object relPath) {
        this.parallaxBackgroundImageRelativePath = relPath
    }

    /** Relative path to folder where Parallax background image will be copied to.
     *
     * @return Relative path to folder where background image can be found after generation.
     */
    @Input
    String getParallaxBackgroundImageRelativePath() {
        StringUtils.stringize(this.parallaxBackgroundImageRelativePath)
    }

    /** Parallax background image if it is specified as a file.
     *
     * @return File or {@code null}.
     */
    @org.gradle.api.tasks.Optional
    @InputFile
    @PathSensitive(RELATIVE)
    File getParallaxBackgroundImageIfFile() {
        getIfFile(this.parallaxBackgroundImage)
    }

    /** Parallax background image if it is specified as URI which is not of type {@code file://}.
     *
     * @return URI or {@code null}.
     */
    @org.gradle.api.tasks.Optional
    @Input
    URI getParallaxBackgroundImageIfUri() {
        getIfUri(this.parallaxBackgroundImage)
    }

    /** Highlight.js theme location if specified as a file.
     *
     * @return File or {@code null}.
     */
    @org.gradle.api.tasks.Optional
    @InputFile
    @PathSensitive(RELATIVE)
    File getHighlightJsThemeIfFile() {
        getIfFile(this.highlightJsTheme)
    }

    /** Highlight.js theme if it is specified as URI which is not of type {@code file://}.
     *
     * @return URI or {@code null}.
     */
    @org.gradle.api.tasks.Optional
    @Input
    URI getHighlightJsThemeIfUri() {
        getIfUri(this.highlightJsTheme)
    }

    /** Overrides Highlight.js CSS style with given file or URL.
     *
     * Default is built-in {@code lib/css/zenburn.css}.
     *
     * @param fileOrUri Object that can be converted to a file or a URI. If the initial object is not a URI,
     *   but it's string presentation starts with http(s) it will be reated as a URI. Anything else is a path to a
     *   local file.
     * @sa https://highlightjs.org
     */
    void setHighlightJsThemeLocation(Object fileOrUri) {
        this.highlightJsTheme = fileOrUri
    }

    /** If Highlight.js theme is overriden and is a local file, this is the relative location
     * where it can be found after generation of the slide desk.
     *
     * @param relPath Relative path to Highlight.js theme.
     */
    void setHighlightJsThemeRelativePath(Object relPath) {
        this.highlightJsThemeRelativePath = relPath
    }

    /** Relative path to folder where Highlight.js theme will be copied to.
     *
     * @return Relative path to folder where Highlight.js theme can be found after generation.
     */
    @Input
    String getHighlightJsThemeRelativePath() {
        StringUtils.stringize(this.highlightJsThemeRelativePath)
    }

    /** Custom.js theme location if specified as a file.
     *
     * @return File or {@code null}.
     */
    @org.gradle.api.tasks.Optional
    @InputFile
    @PathSensitive(RELATIVE)
    File getCustomThemeIfFile() {
        getIfFile(this.customTheme)
    }

    /** Custom Reveal.js theme if it is specified as URI which is not of type {@code file://}.
     *
     * @return URI or {@code null}.
     */
    @org.gradle.api.tasks.Optional
    @Input
    URI getCustomThemeIfUri() {
        getIfUri(this.customTheme)
    }

    /** Provide a custom them as a file or URL.
     *
     * @param fileOrUri Object that can be converted to a file or a URI. If the initial object is not a URI,
     *   but it's string presentation starts with http(s) it will be treated as a URI. Anything else is a path to a
     *   local file.
     */
    void setCustomThemeLocation(Object fileOrUri) {
        this.customTheme = fileOrUri
    }

    /** If a custom Reveal.js theme is provided and is a local file, this is the relative location
     * where it can be found after generation of the slide desk.
     *
     * @param relPath Relative path to custom Reveal.js theme.
     */
    void setCustomThemeRelativePath(Object relPath) {
        this.customThemeRelativePath = relPath
    }

    /** Relative path to folder where custom Reveal.js theme will be copied to.
     *
     * @return Relative path to folder where custom Reveal.js theme can be found after generation.
     */
    @Input
    String getCustomThemeRelativePath() {
        StringUtils.stringize(this.customThemeRelativePath)
    }

    /** The Reveal.js settings as a map of Asciidoctor attributes.
     * The map will be empty if nothing was set.
     *
     * @return Map of attributes.
     */
    @Input
    Map<String, String> getAsAttributeMap() {
        Map<String, Optional> allAttrs = [
            revealjs_controls            : controls,
            revealjs_progress            : progressBar,
            revealjs_slideNumber         : slideNumber,
            revealjs_history             : pushToHistory,
            revealjs_keyboard            : keyboardShortcuts,
            revealjs_overview            : overviewMode,
            revealjs_touch               : touchMode,
            revealjs_center              : verticalCenter,
            revealjs_loop                : loop,
            revealjs_rtl                 : rightToLeft,
            revealjs_fragments           : fragments,
            revealjs_embedded            : flagEmbedded,
            revealjs_autoSlide           : autoSlideInterval,
            revealjs_autoSlideStoppable  : autoSlideStoppable,
            revealjs_mouseWheel          : mouseWheel,
            revealjs_hideAddressBar      : hideAddressBarOnMobile,
            revealjs_previewLinks        : previewLinks,
            revealjs_transition          : transition,
            revealjs_backgroundTransition: backgroundTransition,
            revealjs_transitionSpeed     : transitionSpeed,
            revealjs_viewDistance        : viewDistance
        ].findAll { String k, Optional v ->
            v.isPresent()
        } as Map<String, Optional>

        Map<String, String> attrs = stringizeValues(allAttrs.collectEntries { String k, Optional v ->
            [k, v.get()]
        } as Map<String, Object>)

        if (this.parallaxBackgroundSize) {
            attrs.put 'revealjs_parallaxBackgroundSize', getParallaxBackgroundSize()
        }

        putFileOrUri(
            attrs,
            'revealjs_parallaxBackgroundImage',
            this.parallaxBackgroundImage,
            getParallaxBackgroundImageRelativePath()
        )
        putFileOrUri(attrs, 'highlightjs-theme', this.highlightJsTheme, getHighlightJsThemeRelativePath())
        putFileOrUri(attrs, 'revealjs_customtheme', this.customTheme, getCustomThemeRelativePath())

        attrs
    }

    /** Adds files to a {@link CopySpec} for copying to final artifact.
     *
     * @param cs CopySpec to enhance.
     */
    void enhanceCopySpec(CopySpec cs) {
        copyActionFor(cs, parallaxBackgroundImageIfFile, getParallaxBackgroundImageRelativePath())
        copyActionFor(cs, highlightJsThemeIfFile, getHighlightJsThemeRelativePath())
        copyActionFor(cs, customThemeIfFile, getCustomThemeRelativePath())
    }

    /** Create a copy action for a specific customisation and attaches it to a parent.
     *
     * @param copySpec Parent {@link CopySpec}.
     * @param src Source file or directory. Can be {@code null} in which case this will be a NOOP.
     * @param relativePath Relative path in destination
     */
    private void copyActionFor(CopySpec copySpec, File src, String relativePath) {
        if (src != null) {
            CopySpec child = project.copySpec(new Action<CopySpec>() {
                @Override
                void execute(CopySpec cs) {
                    cs.from src.absoluteFile
                    cs.into relativePath
                }
            })
            copySpec.with(child)
        }
    }

    private File getIfFile(Object fileOrUri) {
        Object image = asFileOrWebUri(fileOrUri)
        image instanceof File ? (File) image : null
    }

    private URI getIfUri(Object fileOrUri) {
        Object image = asFileOrWebUri(fileOrUri)
        image instanceof URI ? (URI) image : null
    }

    private void putFileOrUri(
        final Map<String, String> attrs, final String attrName, final Object fileOrUri, final String relPath) {
        File pFile = getIfFile(fileOrUri)
        URI pUri = getIfUri(fileOrUri)
        if (pFile) {
            attrs.put(attrName, relPath ? "${relPath}${File.separator}${pFile.name}".toString() : pFile.name)
        } else if (pUri) {
            attrs.put attrName, pUri.toString()
        }
    }

    private Object asFileOrWebUri(Object candidate) {
        switch (candidate) {
            case null:
                null
                break
            case URI:
                candidate
                break
            case File:
                candidate
                break
            case Path:
                Path p = (Path) candidate
                if (p.fileSystem == FileSystems.default) {
                    p.toFile()
                } else {
                    p.toUri()
                }
                break
            case String:
                (((String) candidate) =~ /^(?i:https?):.+/) ? ((String) candidate).toURI() : project.file(candidate)
                break
            default:
                asFileOrWebUri(StringUtils.stringize(candidate))
        }
    }

}
