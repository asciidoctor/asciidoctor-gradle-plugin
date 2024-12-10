package org.asciidoctor.gradle.model5.core;

import org.gradle.api.provider.Provider;

/**
 * Definitions of publication processing options.
 *
 * @since 5.0
 *
 * @author Schalk W. Cronjé
 */
public interface ProcessingOptions {
   /* -------------------------
   tag::extension-property[]
   safeMode:: Asciidoctor safe mode.
     Set the Safe mode as either `UNSAFE`, `SAFE`, `SERVER`, `SECURE`.
     Can be a number (0, 1, 10, 20), a string, or the entity name
   end::extension-property[]
   ------------------------- */

    /**
     * Returns the Asciidoctor SafeMode under which a conversion will be run.
     *
     * @return Asciidoctor Safe Mode
     */
    Provider<SafeMode> getSafeMode();

    /**
     * Set Asciidoctor safe mode.
     *
     * @param mode An instance of Asciidoctor SafeMode.
     */
    void setSafeMode(SafeMode mode);

    /**
     * Set Asciidoctor safe mode.
     *
     * @param mode A valid integer representing a Safe Mode
     */
    void setSafeMode(int mode);

    /**
     * Set Asciidoctor safe mode.
     *
     * @param mode A valid string representing a Safe Mode
     */
    void setSafeMode(String mode);

    /**
     * Set Asciidoctor safe mode.
     *
     * @param mode A provider representing a Safe Mode
     */
    void setSafeMode(Provider<SafeMode> mode);

    /**
     * Whether documents should be logged as they are processed.
     *
     * @param logDocuments {@code true} when documents should be logged.
     */
    void setLogDocuments(boolean logDocuments);

    /**
     * Whether document should be logged
     *
     * @return {@code true} when documents should be logged.
     */
    Provider<Boolean> isLogDocuments();
}
