package org.asciidoctor.gradle.model5.core;

import org.gradle.api.provider.Provider;

/**
 * Describes execution options for running a launcher.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
public interface AsciidoctorExecutionsSettings {
    /**
     * Safe mode
     *
     * @return Execution safe mode. Cannot be null.
     */
    Provider<SafeMode> getSafeMode();
}
