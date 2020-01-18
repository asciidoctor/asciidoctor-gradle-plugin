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
package org.asciidoctor.gradle.base

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class AsciidoctorBasePluginSpec extends Specification {
    Project project = ProjectBuilder.builder().build()

    void 'Applying the plugin will add a report task rule'() {
        given:
        project.allprojects {
            apply plugin: 'org.asciidoctor.base'
            tasks.create 'foo', TestTask
        }

        when:
        project.tasks.getByName('fooDependencies')

        then:
        noExceptionThrown()
    }

    static class TestTask extends AbstractAsciidoctorBaseTask {
        Map<String, Object> attributes
        List<AsciidoctorAttributeProvider> attributeProviders
        Set<Configuration> reportableConfigurations = []

        @Override
        void attributes(Map<String, Object> m) {
        }

        @Override
        protected String getEngineName() {
            null
        }
    }
}