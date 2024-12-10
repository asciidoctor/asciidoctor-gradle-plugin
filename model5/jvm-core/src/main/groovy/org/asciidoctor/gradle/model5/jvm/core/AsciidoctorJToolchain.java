package org.asciidoctor.gradle.model5.jvm.core;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.asciidoctor.gradle.model5.core.AsciidoctorToolchain;
import org.gradle.api.Action;
import org.gradle.api.artifacts.ResolutionStrategy;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;

import java.util.Map;
import java.util.Optional;

import static groovy.lang.Closure.DELEGATE_FIRST;

/**
 * A toolchain for running asciidoctorj.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
public interface AsciidoctorJToolchain extends AsciidoctorToolchain {

        /* -------------------------
       tag::extension-property[]
       version:: {asciidoctorj-name} version. If not specified a sane default version will be used.
       end::extension-property[]
       ------------------------- */

    /**
     * Version of AsciidoctorJ that should be used.
     *
     * @return Asciidoctor version
     */
    Provider<String> getAsciidoctorJVersion();

    /** Set a new version to use.
     *
     * @param v New version to be used. Can be of anything that be be resolved by
     * {@link org.ysb33r.grolifant5.api.core.StringTools#stringize ( Object o )}
     */
    void setAsciidoctorJVersion(Object v);

     /* -------------------------
       tag::extension-property[]
       jrubyVersion:: Minimum version of JRuby to be used.
         The exact version that will be used could be higher due to {asciidoctorj-name} having a transitive dependency
         that is newer.
       end::extension-property[]
       ------------------------- */

    /** The version of JRuby to use.
     *
     * If no version of JRuby is specified the one that is linked to AsciidoctorJ
     * will be used.
     *
     * @return Version of JRuby to use or {@code null} to use the JRuby version that is
     * linked to the specified version of AsciidoctorJ.
     *
     */
    Provider<String> getJrubyVersion();

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
    void setJrubyVersion(Object v);

    /* -------------------------
       tag::extension-property[]
        resolutionStrategy:: Strategies for resolving Asciidoctorj-related dependencies.
        {asciidoctorj-name} dependencies are held in a detached configuration.
        If for some special reason, you need to modify the way the dependency set is resolved, you can modify the
        behaviour by adding one or more strategies.
       end::extension-property[]
   ------------------------- */

    /**
     * Adds rules to the resolution strategy for resolving asciidoctorj related dependencies
     *
     * @param strategy Additional resolution strategy. Takes a {@link ResolutionStrategy} as parameter.
     */
    void resolutionStrategy(Action<ResolutionStrategy> strategy);

    /**
     * Adds rules to the resolution strategy for resolving asciidoctorj related dependencies
     *
     * @param strategy Additional resolution strategy. Takes a {@link ResolutionStrategy} as parameter.
     */
    void resolutionStrategy(@DelegatesTo(ResolutionStrategy.class) Closure<?> strategy);

    /**
     * The level at which the AsciidoctorJ process should be logging.
     *
     * @return The currently configured log level. By default, this is {@code project.logging.level}.
     */
    LogLevel getLogLevel();

    /**
     * Set the level at which the AsciidoctorJ process should be logging.
     *
     * @param logLevel LogLevel to use
     */
    void setLogLevel(LogLevel logLevel);

    /**
     * Set the level at which the AsciidoctorJ process should be logging.
     *
     * @param logLevel LogLevel to use
     */
    void setLogLevel(String logLevel);

        /* -------------------------
       tag::extension-property[]
        options:: Options for running the AsciidoctorJ engine.
   ------------------------- */
    /** Returns the Asciidoctor options.
     *
     * @return Resolved options.
     */
    Provider<Map<String, String>> getOptions();

    /**
     * Apply a new set of Asciidoctor options, clearing any options previously set.
     *
     * @param m Map with new options
     */
    void setOptions(Map<String,?> m);

    /**
     * Add additional asciidoctorj options
     *
     * @param m Map with new options
     */
    void options(Map<String,?> m);
}
