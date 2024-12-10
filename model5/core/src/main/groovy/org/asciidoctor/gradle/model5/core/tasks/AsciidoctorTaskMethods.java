package org.asciidoctor.gradle.model5.core.tasks;

import org.asciidoctor.gradle.model5.core.BaseDirStrategy;
import org.asciidoctor.gradle.model5.core.SafeMode;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.util.PatternFilterable;

import java.io.File;
import java.util.Map;

public interface AsciidoctorTaskMethods {

    /**
     * THe attributes the task will use.
     *
     * @param attrs Provider of attributes.
     */
    void setAttributes(Provider<Map<String, String>> attrs);

    /**
     * The {@link SafeMode} to use for processing.
     *
     * @param safeMode Safety mode.
     */
    void setSafeMode(Provider<SafeMode> safeMode);

    /**
     * Whether document names should be logged prior to processing.
     *
     * @param flag Provider of the setting.
     */
    void setLogDocuments(Provider<Boolean> flag);

    /**
     * Set the base directory, if it is required.
     *
     * @param dir Provider of directory.
     */
    void setBaseDirStrategy(Provider<BaseDirStrategy> dir);

    /**
     * The top directory where sources for this task will be located.
     *
     * @param dir Provider to the source directory.
     */
    void setSourceDir(Provider<File> dir);

    /**
     * The source patterns to look for in the source directory.
     *
     * @param patterns Provider of patterns
     */
    void setSourcePatterns(Provider<PatternFilterable> patterns);

    /**
     * The secondary source patterns to look for in the source directory.
     *
     * @param patterns Provider of patterns
     */
     void setSecondarySourcePatterns(Provider<PatternFilterable> patterns);
}
