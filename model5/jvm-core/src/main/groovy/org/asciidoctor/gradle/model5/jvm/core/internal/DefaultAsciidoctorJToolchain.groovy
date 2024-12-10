package org.asciidoctor.gradle.model5.jvm.core.internal

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.AbstractAsciidoctorToolchain
import org.asciidoctor.gradle.model5.core.OutputFormatter
import org.asciidoctor.gradle.model5.jvm.core.AsciidoctorJDocbook
import org.asciidoctor.gradle.model5.jvm.core.AsciidoctorJHtml5
import org.asciidoctor.gradle.model5.jvm.core.AsciidoctorJToolchain
import org.asciidoctor.gradle.model5.jvm.core.internal.formatters.DefaultAsciidoctorJDocbook
import org.asciidoctor.gradle.model5.jvm.core.internal.formatters.DefaultAsciidoctorJHtml5
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.ResolutionStrategy
import org.gradle.api.logging.LogLevel
import org.gradle.api.provider.Provider

import javax.inject.Inject

@CompileStatic
class DefaultAsciidoctorJToolchain extends AbstractAsciidoctorToolchain implements AsciidoctorJToolchain {

    @Inject
    DefaultAsciidoctorJToolchain(String name, Project project) {
        super(name, project)
        final objectFactory = project.objects

        registeredOutputFormatters.registerFactory(AsciidoctorJHtml5) {
            objectFactory.newInstance(DefaultAsciidoctorJHtml5, it, owner)
        }
        registeredOutputFormatters.registerFactory(AsciidoctorJDocbook) {
            objectFactory.newInstance(DefaultAsciidoctorJDocbook, it, owner)
        }
        registeredOutputFormatters.create('html5')
//            create('html5', AsciidoctorJHtml5) {
//
//            }
//            create('docbook', AsciidoctorJDocbook)
    }

    /**
     * Version of AsciidoctorJ that should be used.
     *
     * @return Asciidoctor version
     */
    @Override
    Provider<String> getAsciidoctorJVersion() {
        return null
    }

    /** Set a new version to use.
     *
     * @param v New version to be used. Can be of anything that be be resolved by
     * {@link org.ysb33r.grolifant5.api.core.StringTools#stringize ( Object o )}
     */
    @Override
    void setAsciidoctorJVersion(Object v) {

    }

    /** The version of JRuby to use.
     *
     * If no version of JRuby is specified the one that is linked to AsciidoctorJ
     * will be used.
     *
     * @return Version of JRuby to use or {@code null} to use the JRuby version that is
     * linked to the specified version of AsciidoctorJ.
     *
     */
    @Override
    Provider<String> getJrubyVersion() {
        return null
    }

    /**
     * Set a version of JRuby to use.
     *
     * The version specified is not a guaranteed version, simply a minimum required version.
     * If the version of asciidoctorj is dependent on a version later than the one specified
     * here, then that would be used instead. In such cases if the exact version needs to be
     * forced then a resolution strategy needs to be provided via {@link #resolutionStrategy}.
     *
     * @param v JRuby version
     */
    @Override
    void setJrubyVersion(Object v) {

    }

    /**
     * Adds rules to the resolution strategy for resolving asciidoctorj related dependencies
     *
     * @param strategy Additional resolution strategy. Takes a {@link ResolutionStrategy} as parameter.
     */
    @Override
    void resolutionStrategy(Action<ResolutionStrategy> strategy) {

    }

    /**
     * Adds rules to the resolution strategy for resolving asciidoctorj related dependencies
     *
     * @param strategy Additional resolution strategy. Takes a {@link ResolutionStrategy} as parameter.
     */
    @Override
    void resolutionStrategy(@DelegatesTo(ResolutionStrategy.class) Closure<?> strategy) {

    }

    /**
     * The level at which the AsciidoctorJ process should be logging.
     *
     * @return The currently configured log level. By default this is {@code project.logging.level}.
     */
    @Override
    LogLevel getLogLevel() {
        return null
    }

    /**
     * Set the level at which the AsciidoctorJ process should be logging.
     *
     * @param logLevel LogLevel to use
     */
    @Override
    void setLogLevel(LogLevel logLevel) {

    }

    /**
     * Set the level at which the AsciidoctorJ process should be logging.
     *
     * @param logLevel LogLevel to use
     */
    @Override
    void setLogLevel(String logLevel) {

    }

    /**
     * A list of registered output formatters for this toolchain.
     *
     * @return List of registered output formats.
     */
    @Override
    List<OutputFormatter> getRegisteredOutputFormats() {
        return null
    }

    /**
     * Register a specific output formatter
     * @param formatter Instance of a
     */
    @Override
    void registerOutputFormat(OutputFormatter formatter) {

    }
}
