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
package org.asciidoctor.gradle.jvm.slides

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.base.AsciidoctorUtils
import org.asciidoctor.gradle.base.Transform
import org.asciidoctor.gradle.base.slides.Profile
import org.asciidoctor.gradle.base.slides.SlidesToExportAware
import org.asciidoctor.gradle.jvm.AbstractAsciidoctorTask
import org.asciidoctor.gradle.jvm.gems.AsciidoctorGemPrepare
import org.gradle.api.Action
import org.gradle.api.file.CopySpec

@java.lang.SuppressWarnings('NoWildcardImports')
import org.gradle.api.tasks.*
import org.gradle.workers.WorkerExecutor
import org.ysb33r.grolifant.api.core.Version

import javax.inject.Inject

import static org.asciidoctor.gradle.jvm.gems.AsciidoctorGemSupportPlugin.GEMPREP_TASK
import static org.asciidoctor.gradle.jvm.gems.AsciidoctorGemSupportPlugin.JAR_TASK
import static org.asciidoctor.gradle.jvm.slides.RevealJSExtension.FIRST_VERSION_WITH_PLUGIN_SUPPORT
import static org.gradle.api.tasks.PathSensitivity.RELATIVE

/**
 * @since 2.0
 */
@CacheableTask
@CompileStatic
class AsciidoctorJRevealJSTask extends AbstractAsciidoctorTask implements SlidesToExportAware {

    public final static String REVEALJS_GEM = 'asciidoctor-revealjs'

    private static final String BACKEND_NAME = 'revealjs'

    private Object templateRelativeDir = 'reveal.js'
    private String theme = 'white'
    private final Map<String, Boolean> builtinPlugins = [:]
    private final List<Object> requiredPlugins = []

    /** Injection constructor.
     *
     * @param we {@link WorkerExecutor}.
     */
    @Inject
    @SuppressWarnings('ClosureAsLastMethodParameter')
    AsciidoctorJRevealJSTask(WorkerExecutor we) {
        super(we)
        this.revealjsOptions = new RevealJSOptions(project)
        outputOptions.backends = [BACKEND_NAME]
        copyAllResources()
        AsciidoctorGemPrepare gemPrepare = project.tasks.named(GEMPREP_TASK, AsciidoctorGemPrepare).get()

        asciidoctorj.with {
            requires(REVEALJS_GEM)
            dependsOn(gemPrepare)
            withGemJar(JAR_TASK)
        }

        inputs.file( { RevealJSOptions opt -> opt.highlightJsThemeIfFile }.curry(this.revealjsOptions) ).optional()
        inputs.file( { RevealJSOptions opt -> opt.parallaxBackgroundImageIfFile }.
                curry(this.revealjsOptions) ).optional()
    }

    /** Options for Reveal.JS slides.
     *
     */
    @Nested
    final RevealJSOptions revealjsOptions

    /** Configures Reveal.js options via a Closure.
     *
     * @param configurator Configurating closure. Delegates to a RevealJSOptions object.
     */
    void revealjsOptions(@DelegatesTo(RevealJSOptions) Closure configurator) {
        AsciidoctorUtils.executeDelegatingClosure(this.revealjsOptions, configurator)
    }

    /** Configures Reveal.js options via an Action.
     *
     * @param configurator Configurating action. Will be passed a RevealJSOptions object.
     */
    void revealjsOptions(Action<RevealJSOptions> configurator) {
        configurator.execute(this.revealjsOptions)
    }

    /** The directory where template is read from.
     *
     * @return
     */
    @InputDirectory
    @PathSensitive(RELATIVE)
    File getTemplateSourceDir() {
        revealjsExtension.templateProvider.get()
    }

    /** Sets the location of the final template directory relative to the output directory.
     *
     * This is the equivalent of the {@code revealjsdir} attribute.
     * This is where template ends up after initial processing, but before asciidoctor conversion starts.
     *
     * @param f Anything convertible to a string.
     */
    void setTemplateRelativeDir(Object f) {
        this.templateRelativeDir = f
    }

    /** Obtains the relative location of the template directory.
     *
     * If this has not been set previously via {@link #setTemplateRelativeDir} the default is {@code reveal.js}.
     *
     * @return Relative location of template directory.
     */
    @Input
    String getTemplateRelativeDir() {
        projectOperations.stringTools.stringize(this.templateRelativeDir)
    }

    /** The physical location of the template directory
     *
     * @return
     */
    @OutputDirectory
    File getTemplateDir() {
        project.file("${getOutputDirFor(BACKEND_NAME)}/${getTemplateRelativeDir()}")
    }

    /** Get the reveal.js theme.
     *
     * This is the equivalent of {@code revealjs_theme}. The default is {@code white}.
     *
     * @return Theme that will used for slides.
     */
    @Input
    String getTheme() {
        this.theme
    }

    /** Select a reveal.js theme.
     *
     * Usually this is one on {@code beige , black, league, night, serif, simple, sky, solarized} OR {@code white},
     * but a custom template could have different themes.
     *
     * @param aTheme Theme to use. No validation is done on the theme.
     */
    void setTheme(String aTheme) {
        this.theme = aTheme
    }

    /** Name one or more reveal.js plugins to activate.
     *
     * Plugins must match bundle names registered in the {@code revealjsPlugins} docExtensions.
     * If selected plugins from a bundle is requires then they can be specified as {@code 'bundleName/pluginName'}.
     *
     * @param p List of plugins. Must be convertible to string.
     */
    void plugins(final Iterable<Object> p) {
        this.requiredPlugins.addAll(p)
    }

    /** Name one or more reveal.js plugins to activate.
     *
     * Plugins must match bundle names registered in the {@code revealjsPlugins} docExtensions.
     * If selected plugins from a bundle is requires then they can be specified as {@code 'bundleName/pluginName'}.
     *
     * @param p List of plugins. Must be convertible to string.
     */
    void plugins(Object... p) {
        this.requiredPlugins.addAll(p as List)
    }

    /** Set reveal.js plugins to activate.
     *
     * Plugins must match bundle names registered in the {@code revealjsPlugins} docExtensions.
     * If selected plugins from a bundle is requires then they can be specified as {@code 'bundleName/pluginName'}.
     *
     * @param p List of plugins. Must be convertible to string.
     */
    void setPlugins(final Iterable<Object> p) {
        this.requiredPlugins.clear()
        this.requiredPlugins.addAll(p)
    }

    /** Get list of activated plugins.
     *
     * @return List of plugins that will be copied to the template.
     */
    @Input
    List<String> getPlugins() {
        projectOperations.stringTools.stringize(this.requiredPlugins)
    }

    /** Toggle a built-in reveal.js plugin
     *
     * @param name Name of plugin
     * @param pluginState {@code true} enables plugin.
     */
    void toggleBuiltinPlugin(final String name, boolean pluginState) {
        this.builtinPlugins.put(name, pluginState)
    }

    @Internal
    @Override
    Profile getProfile() {
        Profile.REVEAL_JS
    }

    @Override
    void exec() {
        checkRevealJsVersion()
        processTemplateResources()
        super.exec()
    }

    /** A task may add some default attributes.
     *
     * If the user specifies any of these attributes, then those attributes will not be utilised.
     *
     * The default implementation will add {@code includedir}.
     *
     * @param workingSourceDir Directory where source files are located.
     *
     * @return A collection of default attributes.
     */
    @Override
    Map<String, ?> getTaskSpecificDefaultAttributes(File workingSourceDir) {
        Map<String, String> attrs = super.getTaskSpecificDefaultAttributes(workingSourceDir) as Map<String, String>

        attrs.putAll([revealjsdir: getTemplateRelativeDir(),
            revealjs_theme: getTheme(),
            'source-highlighter': 'highlightjs'
        ])

        attrs.putAll(revealjsOptions.asAttributeMap)

        if (pluginSupportAvailable) {
            this.builtinPlugins.each { String pluginName, Boolean state ->
                attrs.put(
                    "revealjs_plugin_${pluginName}".toString(),
                    (state ? 'enabled' : 'disabled')
                )
            }

            // 'revealjs_plugins not supported by asciidoctor-revealjs >= 5.0
        }

        attrs
    }

    /** Gets the CopySpec for additional resources
     * If {@code resources} was never called, it will return a default CopySpec otherwise it will return the
     * one built up via successive calls to {@code resources}
     *
     * @param lang Language to to apply to or empty for no-language support.
     * @return A{@link CopySpec}. Never {@code null}.
     */
    @Override
    @SuppressWarnings('UnnecessaryPackageReference')
    CopySpec getResourceCopySpec(java.util.Optional<String> lang) {
        CopySpec rcs = super.getResourceCopySpec(lang)
        revealjsOptions.enhanceCopySpec(rcs)
        rcs
    }

    /** Processes the RevealJS template.
     *
     * Copies from {@link #getTemplateSourceDir} to {@link #getTemplateDir}.
     *
     */
    protected void processTemplateResources() {
        final File fromSource = templateSourceDir
        final File target = templateDir
        final Set<ResolvedRevealJSPlugin> fromPlugins = resolvedPlugins

        project.copy(new Action<CopySpec>() {
            @Override
            void execute(CopySpec copySpec) {
                copySpec.into target

                copySpec.from fromSource, { CopySpec cs ->
                    cs.include 'js/**', 'css/**', 'dist/**', 'lib/**', 'plugin/**'
                }

                fromPlugins.each { ResolvedRevealJSPlugin plugin ->
                    copySpec.from plugin.location, { CopySpec cs ->
                        cs.into "plugin/${plugin.name}"
                    }
                }
            }
        })
    }

    @Internal
    protected RevealJSExtension getRevealjsExtension() {
        project.extensions.getByType(RevealJSExtension)
    }

    @Internal
    protected RevealJSPluginExtension getRevealjsPluginExtension() {
        project.extensions.getByType(RevealJSPluginExtension)
    }

    private void checkRevealJsVersion() {
        if (!pluginSupportAvailable) {
            project.logger.warn("You are using Reveal.Js converter version ${revealjsExtension.version}, " +
                'which does not support plugins. Any plugin settings will be ignored.')
        }
    }

    private boolean isPluginSupportAvailable() {
        Version.of(revealjsExtension.version) >= FIRST_VERSION_WITH_PLUGIN_SUPPORT
    }

    private Set<String> getPluginBundles() {
        Transform.toSet(plugins) {
            it.split('/', 2)[0]
        }
    }

    private Set<ResolvedRevealJSPlugin> getResolvedPlugins() {
        if (pluginSupportAvailable) {
            final RevealJSPluginExtension pluginExtension = revealjsPluginExtension
            Transform.toSet(pluginBundles) {
                pluginExtension.getByName(it)
            }
        } else {
            [].toSet() as Set<ResolvedRevealJSPlugin>
        }
    }
}
