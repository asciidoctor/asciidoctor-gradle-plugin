/*
 * Copyright 2013-2019 the original author or authors.
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
import org.gradle.api.GradleException

/**
 * @since 3.0 (Moved from org.asciidoctor.gradle.jvm)
 * @author Schalk W. Cronjé
 */
@CompileStatic
class AsciidoctorExecutionException extends GradleException {

    AsciidoctorExecutionException(final String msg) {
        super(msg)
    }

    AsciidoctorExecutionException(final String msg, Throwable t) {
        super(msg, t)
    }
}
