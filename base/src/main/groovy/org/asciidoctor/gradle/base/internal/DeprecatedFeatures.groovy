/*
 * Copyright 2013-2020 the original author or authors.
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
package org.asciidoctor.gradle.base.internal

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.util.GradleVersion

/** A simplified way of grouping deprecation messages.
 *
 * @author Schalk W. Cronjé
 *
 * @since 2.4.0
 */
@CompileStatic
@SuppressWarnings(['LineLength'])
class DeprecatedFeatures implements Plugin<Project> {

    private static final String EXTENSION_NAME = '$$asciidoctor-deprecated-features$$'
    private static final String BASE_MESSAGE = 'You are using one or more deprecated Asciidoctor Gradle plugin features.'
    private static final boolean GRADLE_4_5_OR_LATER = GradleVersion.current() >= GradleVersion.version('4.5')
    private static final String COMMAND_LINE = GRADLE_4_5_OR_LATER ? '--warning-mode=all' : '-i or --info'

    static void addDeprecationMessage(Project project, String identifier, String message) {
        try {
            NamedDomainObjectContainer<Messages> msgContainer =
                (NamedDomainObjectContainer<Messages>) project.extensions.extraProperties.get(EXTENSION_NAME)
            Messages msgs = msgContainer.findByName(identifier)

            if (msgs) {
                msgs.add(message)
            } else {
                msgContainer.create(identifier).add(message)
            }
        } catch (ExtraPropertiesExtension.UnknownPropertyException e) {
        }
    }

    @Override
    void apply(Project project) {
        NamedDomainObjectContainer<Messages> msgContainer = project.container(Messages)
        project.extensions.extraProperties.set(EXTENSION_NAME, msgContainer)

        project.afterEvaluate {
            if (!msgContainer.empty) {
                switch (getWarningMode(project)) {
                    case 'none':
                        break
                    case 'all':
                        project.logger.lifecycle(createOutputMessage(msgContainer))
                        break
                    default:
                        project.logger.lifecycle(
                            "${BASE_MESSAGE} To help with migration run with ${COMMAND_LINE}."
                        )
                }
            }
        }
    }

    @CompileDynamic
    private static String getWarningMode(Project project) {
        if (GRADLE_4_5_OR_LATER) {
            project.gradle.startParameter.warningMode.toString().toLowerCase()
        } else {
            switch (project.gradle.startParameter.logLevel) {
                case LogLevel.QUIET:
                    return 'none'
                case LogLevel.INFO:
                    return 'all'
                default:
                    ''
            }
        }
    }

    private static String createOutputMessage(NamedDomainObjectContainer<Messages> msgContainer) {
        StringWriter output = new StringWriter()
        output.withCloseable {
            output.println BASE_MESSAGE
            output.println 'These will be removed in 4.0 of these plugins.'
            output.println 'To help you migrate we have compiled some tips for you based upon your current usage.'

            msgContainer.each { Messages msgs ->
                output.println "- ${msgs.name}:"
                msgs.messages.each { String line ->
                    output.println "  - ${line}"
                }
            }

            output.println()
            output.toString()
        }
    }

    static class Messages implements Set<String>, Named {
        final String name
        @Delegate
        final Set<String> messages = []

        Messages(String name) {
            this.name = name
        }
    }
}
