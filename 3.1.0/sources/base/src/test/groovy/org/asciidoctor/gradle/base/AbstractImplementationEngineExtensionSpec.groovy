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
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class AbstractImplementationEngineExtensionSpec extends Specification {
    public static final String EXTNAME = 'sample'
    public static final String EN = 'en'
    public static final String ES = 'es'

    Project project = ProjectBuilder.builder().build()
    TestExtension projectExtension
    TestExtension taskExtension
    Task proxyTask

    void setup() {
        projectExtension = project.extensions.create(EXTNAME, TestExtension, project)
        proxyTask = project.tasks.create('proxyTask')
        taskExtension = proxyTask.extensions.create(EXTNAME, TestExtension, proxyTask, EXTNAME)
    }

    void 'Can set language-specific attributes at project level'() {
        when:
        projectExtension.attributesForLang EN, foo: 'bar'

        then:
        projectExtension.getAttributesForLang(ES).isEmpty()
        projectExtension.getAttributesForLang(EN).foo == 'bar'
    }

    void 'Can augment language-specific attributes at task level'() {
        when:
        projectExtension.attributesForLang EN, foo: 'bar'
        taskExtension.attributesForLang EN, foo2: 'bar2'

        then:
        taskExtension.getAttributesForLang(ES).isEmpty()
        taskExtension.getAttributesForLang(EN).foo == 'bar'
        taskExtension.getAttributesForLang(EN).foo2 == 'bar2'
        !projectExtension.getAttributesForLang(EN).foo2
    }

    void 'Can reset language-specific attributes at task level'() {
        when:
        projectExtension.attributesForLang EN, foo: 'bar'
        taskExtension.resetAttributesForLang EN, foo2: 'bar2'

        then:
        taskExtension.getAttributesForLang(ES).isEmpty()
        !taskExtension.getAttributesForLang(EN).foo
        taskExtension.getAttributesForLang(EN).foo2 == 'bar2'
        !projectExtension.getAttributesForLang(EN).foo2
    }

    void 'Set single attribute'() {
        when:
        projectExtension.attribute('name', 'value')

        then:
        projectExtension.attributes['name'] == 'value'
    }

    static class TestExtension extends AbstractImplementationEngineExtension {
        TestExtension(Project project) {
            super(project, 'org.asciidoctor.gradle.base.test')
        }

        TestExtension(Task task, String name) {
            super(task, name)
        }
    }
}