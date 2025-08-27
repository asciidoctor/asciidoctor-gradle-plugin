/*
 * Copyright ${year} the original author or authors.
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
package org.asciidoctor.gradle.model5.core.waitingroom

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.toolchains.AsciidoctorToolchain
import org.asciidoctor.gradle.model5.toolchains.ToolchainUtils
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory

@CompileStatic
abstract class AbstractOutputFormatter implements AsciidoctorOutputFormatter {

    final String name
    protected final ObjectFactory objectFactory

    @Override
    String getAsciidoctorTaskName(AsciidoctorPublication publication) {
        ToolchainUtils.asciidoctorTaskName(asciidoctorToolchain, this, publication)
    }

    protected AbstractOutputFormatter(String name, Project tempProjectReference) {
        this.name = name
        this.objectFactory = tempProjectReference.objects
    }

    /**
     * Obtain the tool chain that this formatter is attached to.
     *
     * @return Toolchain instance.
     */
    protected abstract AsciidoctorToolchain getAsciidoctorToolchain()
}
