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
package org.asciidoctor.gradle.jvm

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

/**
 * @author Schalk W. Cronjé
 */

class AsciidoctorJExtensionSpec extends Specification {

    Project project = ProjectBuilder.builder().withName('test').build()
    AsciidoctorJExtension asciidoctorj

    void setup() {
        project.allprojects {
            apply plugin: 'org.asciidoctor.jvm.base'
        }

        asciidoctorj = project.extensions.getByType(AsciidoctorJExtension)
    }

    void 'Add a callback to configuration'() {
        setup:
        boolean callbackCalled = false
        asciidoctorj.onConfiguration { Configuration cfg ->
            callbackCalled = true
        }

        when:
        asciidoctorj.configuration

        then:
        callbackCalled
    }

    void 'Configure a repository for callback'() {
        project.allprojects {
            // tag::restrict-repository[]
            repositories {
                maven {
                    name = 'asciidoctorj'
                    url = 'https://some.repo.example'
                }
            }

            asciidoctorj {
                onConfiguration { cfg ->
                    repositories.getByName('asciidoctorj').mavenContent { descriptor ->
                        descriptor.onlyForConfigurations(cfg.name)
                    }
                }
            }
            // end::restrict-repository[]
        }

        when:
        project.evaluate()
        asciidoctorj.configuration

        then:
        noExceptionThrown()
    }
}