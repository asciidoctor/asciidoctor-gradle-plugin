/*
 * Copyright 2013-2024 the original author or authors.
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
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.CopySpec
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.util.PatternSet
import org.gradle.testfixtures.ProjectBuilder
import org.ysb33r.grolifant.api.core.ProjectOperations
import spock.lang.Specification

/**
 * @author Lari Hotari
 */
class BaseTaskPatternSpec extends Specification {
    Project project = ProjectBuilder.builder().build()

    void setup() {
        ProjectOperations.maybeCreateExtension(project)
    }

    void "Should include patterns passed to sources method"() {
        when:
        def task1 = createTask('task') {
            sources('myfile.adoc', 'otherfile.adoc')
        }
        then:
        task1.internalSourceDocumentPattern.includes == ['myfile.adoc', 'otherfile.adoc'] as Set
    }

    void "Should support clearing configured patterns"() {
        given:
        def task = createTask('task') {
            sources('myfile.adoc', 'otherfile.adoc')
        }
        when:
        task.clearSources()
        then:
        task.internalSourceDocumentPattern == null
    }

    void "Should support replacing the configured patterns"() {
        given:
        def task = createTask('task') {
            sources('myfile.adoc', 'otherfile.adoc')
        }
        when:
        task.clearSources()
        task.sources('myfile2.adoc', 'myfile3.adoc')
        then:
        task.internalSourceDocumentPattern.includes == ['myfile2.adoc', 'myfile3.adoc'] as Set
    }

    private Task createTask(String name, Closure cfg) {
        project.tasks.create(name: name, type: PatternSpecAsciidoctorTask).configure cfg
    }

    @SuppressWarnings(['GetterMethodCouldBeProperty'])
    static class PatternSpecAsciidoctorTask extends AbstractAsciidoctorBaseTask {
        // method for accessing internal field "sourceDocumentPattern"
        PatternSet getInternalSourceDocumentPattern() {
            AbstractAsciidoctorBaseTask.metaClass.getProperty(this, 'sourceDocumentPattern')
        }

        @Override
        Map<String, Object> getAttributes() {
            [:]
        }

        @Override
        void setAttributes(Map<String, Object> m) { }

        @Override
        void attributes(Map<String, Object> m) { }

        @Override
        List<AsciidoctorAttributeProvider> getAttributeProviders() {
            []
        }

        @Override
        Set<Configuration> getReportableConfigurations() {
            [] as Set
        }

        @Override
        String getEngineName() {
            null
        }

        @Override
        Provider<PatternSet> getIntermediateArtifactPatternProvider() {
            null
        }

        @Override
        CopySpec getLanguageResourceCopySpec(String lang) {
            null
        }

        @Override
        boolean hasIntermediateWorkDir() {
            false
        }

        @Override
        Provider<File> getIntermediateWorkDirProvider() {
            null
        }
    }

}
