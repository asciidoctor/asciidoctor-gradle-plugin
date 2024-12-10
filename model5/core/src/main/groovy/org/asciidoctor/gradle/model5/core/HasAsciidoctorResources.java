package org.asciidoctor.gradle.model5.core;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;
import org.gradle.api.file.CopySpec;
import org.ysb33r.grolifant5.api.core.ClosureUtils;

/**
 * Configuration of Asciidoctor resource files.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
public interface HasAsciidoctorResources {
    /**
     *  Add to the CopySpec for extra files.
     *
     * The destination of these files will always have a parent directory
     * of {@code outputDir} or {@code outputDir + backend}
     *
     * @param cfg {@link CopySpec} runConfiguration {@link Action}
     */
    void resources(Action<? super CopySpec> cfg);

    /**
     *  Add to the CopySpec for extra files.
     *
     * The destination of these files will always have a parent directory
     * of {@code outputDir} or {@code outputDir + backend}
     *
     * @param cfg {@link CopySpec} runConfiguration {@link Action}
     */
    void resources(@DelegatesTo(CopySpec.class) Closure<?> cfg);
}
