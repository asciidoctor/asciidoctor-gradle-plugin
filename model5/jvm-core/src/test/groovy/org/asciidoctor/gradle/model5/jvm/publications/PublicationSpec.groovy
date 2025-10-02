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
package org.asciidoctor.gradle.model5.jvm.publications

import org.asciidoctor.gradle.model5.core.AsciidoctorModelExtension
import org.asciidoctor.gradle.model5.core.internal.publications.PublicationUtils
import org.asciidoctor.gradle.model5.jvm.plugins.AsciidoctorjPlugin
import org.asciidoctor.gradle.testfixtures.model5.UnitTestSpecification

import static org.asciidoctor.gradle.model5.core.internal.publications.PublicationUtils.DEFAULT_PUBLICATION
import static org.asciidoctor.gradle.model5.jvm.plugins.AsciidoctorjPlugin.DEFAULT_TOOLCHAIN

class PublicationSpec extends UnitTestSpecification {

    AsciidoctorModelExtension asciidoc

    void setup() {
        project.pluginManager.apply(AsciidoctorjPlugin)
        asciidoc = project.extensions.getByType(AsciidoctorModelExtension)
    }

    void 'When a publication and an output is registered, a task is added'() {
        when:
        asciidoc.publications.getByName(DEFAULT_PUBLICATION).output(DEFAULT_TOOLCHAIN,'html')

        then:
        project.tasks.getByName(PublicationUtils.conversionTaskName(DEFAULT_PUBLICATION,'html'))
    }
}
