/*
 * Copyright 2013 - 2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.asciidoctor.gradle.base

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations
import org.ysb33r.grolifant5.api.core.GrolifantProblemReporter

import static org.ysb33r.grolifant5.api.core.GrolifantProblemReporter.Severity.WARNING
import static org.ysb33r.grolifant5.api.core.StringTools.EMPTY

/**
 * For reporting problems relating to classic deprecations.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
@SuppressWarnings(['LineLength', 'DuplicateStringLiteral', 'ParameterCount'])
class ProblemReports {

    public static final String GROUP = 'Asciidoctor Classic'
    public static final String GROUP_TEXT = 'Deprecated Asciidoctor usage'
    public static final String TOOLCHAIN_J = 'asciidoctorj'
    public static final String TOOLCHAIN_J_CLASS = 'AsciidoctorjToolchain'
    public static final String TOOLCHAIN_JS = 'asciidoctorjs'
    public static final Boolean NO_WARNINGS = System.getProperty(
        'org.asciidoctor.gradle.class.deprecation.warning.disable', 'false'
    ).toBoolean()

    public static final GrolifantProblemReporter.ProblemId ASCIIDOCTOR_J_PROBLEM_ID = GrolifantProblemReporter.ProblemId.of(
        'asciidoctorj',
        'Deprecated Asciidoctorj task',
        GROUP,
        GROUP_TEXT
    )
    public static final GrolifantProblemReporter.ProblemId ASCIIDOCTOR_JS_PROBLEM_ID = GrolifantProblemReporter.ProblemId.of(
        'asciidoctorjs',
        'Deprecated Asciidoctor.js task',
        GROUP,
        GROUP_TEXT
    )
    public static final GrolifantProblemReporter.Severity PROBLEM_SEVERITY = WARNING

    public static final String MIGRATE = 'Migrate to the model5 DSL.'

    static void report(
        GrolifantProblemReporter problemReporter,
        GrolifantProblemReporter.ProblemId id,
        String details,
        String solution
    ) {
        if (!NO_WARNINGS) {
            problemReporter.report(id) { GrolifantProblemReporter.ProblemSpec spec ->
                spec.severity = PROBLEM_SEVERITY
                spec.details = details
                spec.solution = solution
            }
        }
    }

    static String taskProblemDetail(String taskName, String engine) {
        "You are using a deprecated feature - ${taskName} task on ${engine}. This will be removed in 6.0"
    }

    static String publicationName(String taskName) {
        taskName == 'asciidoctor' ? 'main' : taskName.replaceFirst(~/^asciidoctor/, EMPTY).uncapitalize()
    }

    static String toolchainFromClass(String canonicalName) {
        canonicalName.contains('.js') ? TOOLCHAIN_JS : TOOLCHAIN_J
    }

    static GrolifantProblemReporter.ProblemId problemIdFromClass(String canonicalName) {
        canonicalName.contains('.js') ? ASCIIDOCTOR_JS_PROBLEM_ID : ASCIIDOCTOR_J_PROBLEM_ID
    }

    static String replacePlugin(
        Project project,
        String taskName,
        String oldPlugin,
        String newPlugin,
        String toolchainName,
        String formatterName = 'html'
    ) {
        final pubName = publicationName(taskName)
        """
        ${MIGRATE}

        Remove the old plugin 'org.asciidoctor.${oldPlugin}' and replace it with

        plugins {
            id 'org.asciidoctor.${newPlugin}' version '${ProblemReports.getVersion(project)}'
        }

        asciidoc {
            publications {
                ${pubName} {
                    sourceSet {
                       // Configure source details here
                    }
                    // If you are not outputting to ${formatterName}, then replace the 2nd parameter
                    output( '${toolchainName}', '${formatterName}' )
                }
            }
        }
       """.stripIndent()
    }

    static String getVersion(Project project) {
        ConfigCacheSafeOperations.from(project).fsOperations().loadPropertiesFromResource(
            'META-INF/asciidoctor.gradle/asciidoctor-gradle-base.properties',
            ProblemReports.classLoader
        )['version']
    }
}
