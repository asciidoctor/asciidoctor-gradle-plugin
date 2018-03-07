/*
 * Copyright 2013-2018 the original author or authors.
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
package org.asciidoctor.gradle

import org.gradle.api.Project

/**
 * @author Andres Almiray
 */
class AsciidoctorExtension {
    String version = '1.5.6'

    String groovyDslVersion = '1.0.0.Alpha2'

    /**
     * By default the plugin will try to add a default repository to find AsciidoctorJ.
     * For certain cases this approach is not acceptable, the behaviour can be turned off
     * by setting this value to {@code true}
     *
     * @since 1.5.3
     */
    boolean noDefaultRepositories = false

    final Project project

    AsciidoctorExtension(Project project) {
        this.project = project
    }
}
