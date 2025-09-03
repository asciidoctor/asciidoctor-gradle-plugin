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
package org.asciidoctor.gradle.model5.core.revealjs

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.CanConfigureTaskInputs
import org.asciidoctor.gradle.model5.core.attributes.HasAttributeProvider
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskInputs
import org.ysb33r.grolifant5.api.core.ClosureUtils
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations
import org.ysb33r.grolifant5.api.core.StringTools

import javax.inject.Inject

/**
 * Options for Reveal.js slides.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
@SuppressWarnings('MethodCount')
class RevealjsOptions implements HasAttributeProvider, CanConfigureTaskInputs {

    final Provider<Map<String, Object>> attributeProvider

    /**
     * Configure Parallax settings.
     */
    final Parallax parallax

    /**
     * Configure highlight.js theme.
     */
    final FileOrUri highlightjsTheme

    private final MapProperty<String, Object> attrs
    private final Property<String> transition
    private final Property<String> backgroundTransition
    private final Property<String> transitionSpeed
    private final Property<String> slideNumber
    private final Property<Boolean> controls
    private final Property<Boolean> progressBar
    private final Property<Boolean> pushToHistory
    private final Property<Boolean> keyboardShortcuts
    private final Property<Boolean> overviewMode
    private final Property<Boolean> touchMode
    private final Property<Boolean> verticalCenter
    private final Property<Boolean> loop
    private final Property<Boolean> rightToLeft
    private final Property<Boolean> fragments
    private final Property<Boolean> flagEmbedded
    private final Property<Boolean> autoSlideStoppable
    private final Property<Boolean> mouseWheel
    private final Property<Boolean> hideAddressBarOnMobile
    private final Property<Boolean> previewLinks
    private final Property<Integer> autoSlideInterval
    private final Property<Integer> viewDistance
    private final Property<BuiltInThemes> builtinTheme
    private final FileOrUri customTheme
    private final StringTools stringTools

    enum Transition {
        NONE,
        FADE,
        SLIDE,
        CONVEX,
        CONCAVE,
        ZOOM
    }

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

    /**
     * Built-in Reveal.js themes.
     */
    enum BuiltInThemes {
        BEIGE,
        BLACK,
        LEAGUE,
        NIGHT,
        SERIF,
        SIMPLE,
        SKY,
        SOLARIZED,
        WHITE
    }

    @Inject
    RevealjsOptions(Project project) {
        transition = project.objects.property(String)
        backgroundTransition = project.objects.property(String)
        transitionSpeed = project.objects.property(String)
        slideNumber = project.objects.property(String)
        controls = project.objects.property(Boolean)
        progressBar = project.objects.property(Boolean)
        pushToHistory = project.objects.property(Boolean)
        keyboardShortcuts = project.objects.property(Boolean)
        overviewMode = project.objects.property(Boolean)
        touchMode = project.objects.property(Boolean)
        verticalCenter = project.objects.property(Boolean)
        loop = project.objects.property(Boolean)
        rightToLeft = project.objects.property(Boolean)
        fragments = project.objects.property(Boolean)
        flagEmbedded = project.objects.property(Boolean)
        autoSlideStoppable = project.objects.property(Boolean)
        mouseWheel = project.objects.property(Boolean)
        hideAddressBarOnMobile = project.objects.property(Boolean)
        previewLinks = project.objects.property(Boolean)

        this.autoSlideInterval = project.objects.property(Integer)
        this.viewDistance = project.objects.property(Integer)
        this.builtinTheme = project.objects.property(BuiltInThemes)
        this.customTheme = project.objects.newInstance(FileOrUri, 'revealjs_customtheme')
        this.highlightjsTheme = project.objects.newInstance(FileOrUri, 'highlightjs-theme')
        this.stringTools = ConfigCacheSafeOperations.from(project).stringTools()
        this.parallax = project.objects.newInstance(Parallax)
        this.attrs = project.objects.mapProperty(String, Object)

        this.attrs.putAll(
            stringTools.provideValuesDropNull([
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
            ])
        )

        this.attrs.putAll(parallax.attributeProvider)

        this.attrs.putAll(
                this.builtinTheme.map {
                    [revealjs_theme: it.name().toLowerCase(Locale.US)] as Map<String, Object>
                }.orElse(this.customTheme.attributeProvider)
        )

        this.attrs.putAll(this.highlightjsTheme.attributeProvider)
        this.attributeProvider = this.attrs
    }

    /**
     * Display controls in the bottom right corner.
     */
    void setControls(Boolean b) {
        this.controls.set(b)
    }

    /**
     * Display a presentation progress bar.
     */
    void setProgressBar(Boolean b) {
        this.progressBar.set(b)
    }

    /**
     * Display the slide number of the current slide.
     */
    void setSlideNumber(boolean b) {
        this.slideNumber.set(SlideNumber.slideNumber(b).value)
    }

    /**
     * Display the slide number of the current slide.
     */
    void setSlideNumber(SlideNumber slideNumber) {
        this.slideNumber.set(slideNumber.value)
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
        this.slideNumber.set(SlideNumber.valueOf(b).value)
    }

    /** Push each slide change to the browser history.
     *
     */
    void setPushToHistory(Boolean b) {
        this.pushToHistory.set(b)
    }

    /** Enable keyboard shortcuts for navigation.
     *
     */
    void setKeyboardShortcuts(Boolean b) {
        this.keyboardShortcuts.set(b)
    }

    /** Enable the slide overview mode.
     *
     */
    void setOverviewMode(Boolean b) {
        this.overviewMode.set(b)
    }

    /** Enables touch navigation on devices with touch input.
     *
     */
    void setTouchMode(Boolean b) {
        this.touchMode.set(b)
    }

    /** Vertical centering of slides.
     *
     */
    void setVerticalCenter(Boolean b) {
        this.verticalCenter.set(b)
    }

    /** Loop the presentation..
     *
     */
    void setLoop(Boolean b) {
        this.loop.set(b)
    }

    /** Change the presentation direction to be RTL.
     *
     */
    void setRightToLeft(Boolean b) {
        this.rightToLeft.set(b)
    }

    /** Use fragments globally.
     *
     */
    void setFragments(Boolean b) {
        this.fragments.set(b)
    }

    /** Flags if the presentation is running in an embedded mode ( contained within a limited portion of the screen ).
     *
     */
    void setFlagEmbedded(Boolean b) {
        this.flagEmbedded.set(b)
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
        this.autoSlideInterval.set(ms)
    }

    /** Stop auto-sliding after user input
     *
     */
    void setAutoSlideStoppable(Boolean b) {
        this.autoSlideStoppable.set(b)
    }

    /** Enable slide navigation via mouse wheel.
     *
     */
    void setMouseWheel(Boolean b) {
        this.mouseWheel.set(b)
    }

    /** Hides the address bar on mobile devices.
     *
     */
    void setHideAddressBarOnMobile(Boolean b) {
        this.hideAddressBarOnMobile.set(b)
    }

    /**
     * Opens links in an iframe preview overlay.
     *
     */
    void setPreviewLinks(Boolean b) {
        this.previewLinks.set(b)
    }

    /** Number of slides away from the current that are visible.
     *
     * If not set, Revels.JS will use an internal value of 3.
     */
    void setViewDistance(Integer numSlides) {
        if (numSlides != null && numSlides < 0) {
            throw new GradleException('viewDistance cannot be less than 0')
        }
        this.viewDistance.set(numSlides)
    }

    /**
     * Transition style.
     *
     * If not set, the Reveal.JS will use an internal default of {@code SLIDE}.
     */
    void setTransition(final String s) {
        this.transition.set(s?.toLowerCase(Locale.US))
    }

    /**
     * Transition style.
     *
     * If not set, the Reveal.js will use an internal default of {@code SLIDE}.
     */
    void setTransition(Transition tr) {
        this.transition.set(tr?.toString()?.toLowerCase())
    }

    /** Transition style for full page slide backgrounds..
     *
     * One of {@code none , fade, slide, convex, concave, zoom}.
     *
     * If not set then Reveal.js will use an internal default of {@code FADE}.
     */
    void setBackgroundTransition(final String s) {
        this.backgroundTransition.set(s?.toLowerCase(Locale.US))
    }

    /** Transition style for full page slide backgrounds..
     *
     * One of {@code none , fade, slide, convex, concave, zoom}.
     *
     * If not set then Reveal.js will use an interna default of {@code FADE}.
     */
    void setBackgroundTransition(Transition tr) {
        this.backgroundTransition.set(tr?.toString()?.toLowerCase(Locale.US))
    }

    /** Slide transition speed
     *
     * One of {@code default , fast, slow}.
     *
     * If not provided, Reveal.js wil use an internal default value of {@code DEFAULT}.
     */
    void setTransitionSpeed(final String s) {
        this.transitionSpeed.set(s?.toLowerCase(Locale.US))
    }

    /** Slide transition speed
     *
     * One of {@code default , fast, slow}.
     *
     * If not provided, Reveal.js wil use an internal default value of {@code DEFAULT}.
     */
    void setTransitionSpeed(TransitionSpeed tr) {
        this.transitionSpeed.set(tr?.toString()?.toLowerCase(Locale.US))
    }

    /**
     * Use a built-in theme. Unsets anything from {@link #customTheme(Action)}.
     *
     * @param theme Built-in theme.
     */
    void setBuiltinThemeName(BuiltInThemes theme) {
        this.builtinTheme.set(theme)
    }

    /**
     * Use a built-in theme. Unsets anything from {@link #customTheme(Action)}.
     *
     * @param theme Built-in theme.
     */
    void setBuiltinThemeName(String theme) {
        builtinThemeName = BuiltInThemes.valueOf(theme.toUpperCase(Locale.US))
    }

    /**
     * Configures a custom theme. Unsets anything from {@link #setBuiltinThemeName(String)}.
     *
     * @param configurator Configures a custom theme.
     */
    void customTheme(Action<FileOrUri> configurator) {
        configurator.execute(this.customTheme)
        this.builtinTheme.set((BuiltInThemes) null)
    }

    /**
     * Configures a custom theme. Unsets anything from {@link #setBuiltinThemeName(String)}.
     *
     * @param configurator Configures a custom theme.
     */
    void customTheme(@DelegatesTo(FileOrUri) Closure<?> configurator) {
        ClosureUtils.configureItem(this.customTheme, configurator)
        this.builtinTheme.set((BuiltInThemes) null)
    }

    @Override
    void configureTaskInputs(TaskInputs taskInputs) {
        parallax.configureTaskInputs(taskInputs)
        customTheme.configureTaskInputs(taskInputs)
        highlightjsTheme.configureTaskInputs(taskInputs)
    }
}
