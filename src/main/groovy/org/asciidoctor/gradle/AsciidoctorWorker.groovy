package org.asciidoctor.gradle

/**
 * Asciidoctor worker interface.
 *
 * @author Benjamin Muschko
 */
interface AsciidoctorWorker {
    void execute(File sourceDir, File outputDir, String backend)
}