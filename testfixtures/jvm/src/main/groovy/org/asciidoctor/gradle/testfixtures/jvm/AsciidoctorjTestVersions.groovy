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
package org.asciidoctor.gradle.testfixtures.jvm

import groovy.transform.CompileStatic

/**
 * @since 2.0.0
 * @author Schalk W. Cronjé
 */
@CompileStatic
class AsciidoctorjTestVersions {

    // These lines are read by the build script.
    final static String SERIES_16 = '1.6.0-RC.2'
    final static String SERIES_20 = '1.6.0-RC.2'
    final static String GROOVYDSL_SERIES_16 = '1.6.0-alpha.2'
    final static String GROOVYDSL_SERIES_20 = '1.6.0-alpha.2'
    final static String DIAGRAM_SERIES_16 = '1.5.8'
    final static String DIAGRAM_SERIES_20 = '1.5.8'
}
