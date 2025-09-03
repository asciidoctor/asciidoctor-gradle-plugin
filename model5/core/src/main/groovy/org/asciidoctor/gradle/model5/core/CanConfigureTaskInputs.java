package org.asciidoctor.gradle.model5.core;

import org.asciidoctor.gradle.model5.core.attributes.HasAttributeProvider;
import org.asciidoctor.gradle.model5.core.formatters.AsciidoctorOutputFormatter;
import org.gradle.api.tasks.TaskInputs;

/**
 * INidactes that the entity can configure task inputs.
 *
 * @author Schalk W. Cronjé
 * @since 5.0
 */
public interface CanConfigureTaskInputs {
    /**
     * Primarily intended for indirect task creation and not usage in the DSL, this method will configure the inputs
     * of a given task to provide better up-to-date information.
     *
     * <p>
     *     This can be overridden by implementations as the default operation is a NOOP.
     *     Attributes i.e., {@link HasAttributeProvider#getAttributeProvider()}, should be used here, as attributes
     *     are dealt with correctly in another place.
     *     The output of {@link AsciidoctorOutputFormatter#getClasspath()} should not be used either as
     *     {@link org.asciidoctor.gradle.model5.core.tasks.AsciidoctorTask} can correctly deal with the classpath.
     *     The main intent here is for additional configuration directories etc. to be passed as inputs.
     * </p>
     */
    default void configureTaskInputs(TaskInputs taskInputs) {}
}
