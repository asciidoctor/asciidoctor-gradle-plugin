package org.asciidoctor.gradle.model5.jvm.internal

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations

import static org.asciidoctor.gradle.model5.core.plugins.AsciidoctorCorePlugin.INTERMEDIATE_RESOURCE_PATH

/**
 * Utilities for use inside this set of {@code asciidoctorj} plugins
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class PluginUtils {
    static Provider<String> loadDefaultVersion(String entity, Project project, ClassLoader classLoader) {
        final props = ConfigCacheSafeOperations.from(project).fsOperations().loadPropertiesFromResource(
                "${INTERMEDIATE_RESOURCE_PATH}/asciidoctor5-jvm-core-plugin.properties",
                classLoader
        )
        project.provider { -> props[entity].toString() }
    }
}
