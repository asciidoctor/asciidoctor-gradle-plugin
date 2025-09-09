/*
 * Copyright 2013 - 2025 the original author or authors.
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
package org.asciidoctor.gradle.model5.jvm.internal.extensions

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.jvm.extensions.AsciidoctorjExtension

/**
 * Base class for implementing {@code asciidoctorj} extensions.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
abstract class AbstractAsciidoctorjExtension implements AsciidoctorjExtension {
    /**
     * A string representing the class name as it should be used in the DSL.
     *
     * @return Display type for report.
     */
    @Override
    String getDisplayType() {
        dslType.canonicalName
    }

    /**
     * The type that this implements and which should be displayed.
     *
     * @return A type that needs to be displayed.
     */
    abstract protected Class<?> getDslType()
}
