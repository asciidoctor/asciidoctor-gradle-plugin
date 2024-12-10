package org.asciidoctor.gradle.model5.core;

import org.gradle.api.Named;

/**
 * Defines an output formatter.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
public interface AsciidoctorOutputFormatter extends Named {

    /**
     * The name of the task that will be run for the given publication.
     *
     * @param publication Asciidoctor publication.
     *
     * @return Task name
     */
    String getAsciidoctorTaskName(AsciidoctorPublication publication);

    /**
     * Registers the tasks associated with this given output formatter, its toolchain and the corresponding publication.
     *
     * @param publication Publication
     */
    void registerTasksIfAbsent(AsciidoctorPublication publication);

    /**
     * The interface this output formatter instance represents, not the actual instance itself.
     *
     * @return CLass type of the output formatter interface.
     */
    Class<?> getOutputFormatterClass();
}
