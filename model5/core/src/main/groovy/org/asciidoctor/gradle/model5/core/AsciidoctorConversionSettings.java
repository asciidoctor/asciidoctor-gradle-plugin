package org.asciidoctor.gradle.model5.core;

import org.gradle.api.provider.Provider;

import java.io.File;
import java.util.Map;
import java.util.Set;

/**
 * Described the source documents, attributes etc required for a conversion.
 *
 * @author Schalk W. Cronjé
 * @since 5.0
 */
public interface AsciidoctorConversionSettings {
    /**
     * Source files.
     *
     * @return Set of files. Must be present and contain at least one document.
     */
    Provider<Set<File>> getSourceFiles();

    /**
     * Backend to use.
     *
     * @return Backend. Must be present.
     */
    Provider<AsciidoctorNamedBackend> getBackend();

    /**
     * Base directory.
     *
     * @return Base directory for all source files. Must be present.
     */
    Provider<File> getBaseDir();

    /**
     * Destination directory.
     *
     * @return Directory for output. Must be present.
     */
    Provider<File> getDestinationDir();

    /**
     * Document attributes.
     *
     * @return All attributes. Must be present, but can be an empty map.
     */
    Provider<Map<String, String>> getAttributes();

    /**
     * Document type.
     *
     * @return Optional document type. Provider does not need to be present.
     */
    Provider<DocType> getDocType();
}
