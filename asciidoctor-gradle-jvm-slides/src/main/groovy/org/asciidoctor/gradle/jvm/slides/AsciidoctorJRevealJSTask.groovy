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
package org.asciidoctor.gradle.jvm.slides

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.base.AsciidoctorUtils
import org.asciidoctor.gradle.base.Transform
import org.asciidoctor.gradle.jvm.AbstractAsciidoctorTask
import org.gradle.api.Action
import org.gradle.api.file.CopySpec
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.workers.WorkerExecutor
import org.ysb33r.grolifant.api.StringUtils
import org.ysb33r.grolifant.api.Version

import javax.inject.Inject

import static org.asciidoctor.gradle.jvm.slides.RevealJSExtension.FIRST_VERSION_WITH_PLUGIN_SUPPORT

/**
 * @since 2.0
 */
@CompileStatic
class AsciidoctorJRevealJSTask extends AbstractAsciidoctorTask {

    static final String PLUGIN_CONFIGURATION_FILENAME = 'revealjs-plugin-configuration.js'
    static final String PLUGIN_LIST_FILENAME = 'revealjs-plugins.js'

    private static final String BACKEND_NAME = 'revealjs'

    private Object templateRelativeDir = 'reveal.js'
    private String theme = 'white'
    private Object pluginConfigurationFile
    private final Map<String, Boolean> builtinPlugins = [:]
    private final List<Object> requiredPlugins = []

    /**
     *
     * @param we {@link WorkerExecutor}.
     */
    @Inject
    AsciidoctorJRevealJSTask(WorkerExecutor we) {
        super(we)
        this.revealjsOptions = new RevealJSOptions(project)
        configuredOutputOptions.backends = [BACKEND_NAME]
        copyAllResources()

        inputs.file({ revealjsOptions.highlightJsThemeIfFile }).optional()
        inputs.file({ revealjsOptions.parallaxBackgroundImageIfFile }).optional()
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
    @SuppressWarnings('ConfusingMethodName')
    void revealjsOptions(@DelegatesTo(RevealJSOptions) Closure configurator) {
        AsciidoctorUtils.executeDelegatingClosure(this.revealjsOptions, configurator)
    }

    /** Configures Reveal.js options via an Action.
     *
     * @param configurator Configurating action. Will be passed a RevealJSOptions object.
     */
    @SuppressWarnings('ConfusingMethodName')
    void revealjsOptions(Action<RevealJSOptions> configurator) {
        configurator.execute(this.revealjsOptions)
    }

    /** The directory where template is read from.
     *
     * @return
     */
    @InputDirectory
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
        StringUtils.stringize(this.templateRelativeDir)
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
     * Plugins must match bundle names registered in the {@code revealjsPlugins} extensions.
     * If selected plugins from a bundle is requires then they can be specified as {@code 'bundleName/pluginName'}.
     *
     * @param p List of plugins. Must be convertible to string.
     */
    void plugins(final Iterable<Object> p) {
        this.requiredPlugins.addAll(p)
    }

    /** Name one or more reveal.js plugins to activate.
     *
     * Plugins must match bundle names registered in the {@code revealjsPlugins} extensions.
     * If selected plugins from a bundle is requires then they can be specified as {@code 'bundleName/pluginName'}.
     *
     * @param p List of plugins. Must be convertible to string.
     */
    void plugins(Object... p) {
        this.requiredPlugins.addAll(p as List)
    }

    /** Set reveal.js plugins to activate.
     *
     * Plugins must match bundle names registered in the {@code revealjsPlugins} extensions.
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
        StringUtils.stringize(this.requiredPlugins)
    }

    /** Toggle a built-in reveal.js plugin
     *
     * @param name Name of plugin
     * @param pluginState {@code true} enables plugin.
     */
    void toggleBuiltinPlugin(final String name, boolean pluginState) {
        this.builtinPlugins.put(name, pluginState)
    }

    /** Returns the location of the file for configuring external plugins.
     *
     * @return Location of file. Can be {@code null}
     */
    @InputFile
    @Optional
    File getPluginConfigurationFile() {
        this.pluginConfigurationFile ? project.file(this.pluginConfigurationFile) : null
    }

    /** Sets the location of the project configuration file.
     *
     * @param f Location of the configuration file
     */
    void setPluginConfigurationFile(Object f) {
        this.pluginConfigurationFile = f
    }

    @Override
    void processAsciidocSources() {
        checkRevealJsVersion()
        processTemplateResources()
        super.processAsciidocSources()
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
    protected Map<String, Object> getTaskSpecificDefaultAttributes(File workingSourceDir) {
        Map<String, Object> attrs = super.getTaskSpecificDefaultAttributes(workingSourceDir)

        attrs.putAll revealjsdir: getTemplateRelativeDir(),
            revealjs_theme: getTheme()

        attrs.putAll revealjsOptions.asAttributeMap

        if(pluginSupportAvailable) {
            this.builtinPlugins.each { String pluginName, Boolean state ->
                attrs.put "revealjs_plugins_${pluginName}".toString(), state.toString()
            }

            if (!requiredPlugins.empty) {
                attrs.put 'revealjs_plugins', pluginListLocation

                if (getPluginConfigurationFile() != null) {
                    attrs.put 'revealjs_plugins_configuration', pluginConfigurationLocation
                }
            }
        }

        attrs.put 'source-highlighter@', 'highlightjs'
        attrs
    }

    /** Gets the CopySpec for additional resources
     * Enhances the default operation from {@link AbstractAsciidoctorTask#getResourceCopySpec}
     * to also include any custom background images and themes.
     *
     * @return A {@link CopySpec}. Never {@code null}.
     */
    @Override
    protected CopySpec getResourceCopySpec() {
        CopySpec rcs = super.resourceCopySpec
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
        final File configLocation = getPluginConfigurationFile()

        project.copy(new Action<CopySpec>() {
            @Override
            void execute(CopySpec copySpec) {
                copySpec.into target

                copySpec.from fromSource, { CopySpec cs ->
                    cs.include 'js/**', 'css/**', 'lib/**', 'plugin/**'
                }

                fromPlugins.each { ResolvedRevealJSPlugin plugin ->
                    copySpec.from plugin.location, { CopySpec cs ->
                        cs.into "plugin/${plugin.name}"
                    }
                }

                if(!fromPlugins.empty && configLocation != null) {
                    copySpec.from configLocation, { CopySpec cs ->
                        cs.rename( configLocation.name, PLUGIN_CONFIGURATION_FILENAME )
                    }
                }
            }
        })

        if(!fromPlugins.empty) {
            final String relativePath = pluginListLocation
            generatePluginList(new File(target,relativePath),relativePath)
        }
    }

    @Internal
    protected RevealJSExtension getRevealjsExtension() {
        project.extensions.getByType(RevealJSExtension)
    }

    @Internal
    protected RevealJSPluginExtension getRevealsjsPluginExtension() {
        project.extensions.getByType(RevealJSPluginExtension)
    }

    private void generatePluginList(File targetFile,String relativePath) {
        targetFile.parentFile.mkdirs()

        String pluginList = Transform.toList(plugins,{ String fullName ->
            "{ src: '${relativePath}/${fullName}' }"
        }).join(',\n')

        targetFile.withWriter { Writer w ->
            w.println pluginList
        }
    }

    private void checkRevealJsVersion() {
        if(!pluginSupportAvailable) {
            project.logger.warn("You are using Reveal.Js converter version ${revealjsExtension.version}, which does not support plugins. Any plugin settings will be ignored.")
        }
    }

    private boolean isPluginSupportAvailable() {
        Version.of(revealjsExtension.version) >= FIRST_VERSION_WITH_PLUGIN_SUPPORT
    }

    private String getPluginListLocation() {
        "${templateRelativeDir}/${PLUGIN_LIST_FILENAME}"
    }

    private String getPluginConfigurationLocation() {
        "${templateRelativeDir}/${PLUGIN_CONFIGURATION_FILENAME}"
    }

    private Set<String> getPluginBundles() {
        Transform.toSet(plugins) {
            it.split('/', 2)[0]
        }
    }

    private Set<ResolvedRevealJSPlugin> getResolvedPlugins() {
        if(isPluginSupportAvailable()) {
            final RevealJSPluginExtension pluginExtension = revealsjsPluginExtension
            pluginBundles.collect {
                pluginExtension.getByName(it)
            } as Set
        } else {
            [] as Set
        }
    }
}
