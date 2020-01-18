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
package org.asciidoctor.gradle.testfixtures

import groovy.transform.CompileStatic

import static org.asciidoctor.gradle.testfixtures.internal.TestFixtureVersionLoader.VERSIONS

/**
 * @author Schalk W. Cronjé
 *
 * @since 2.0.0
 */
@CompileStatic
class AsciidoctorjTestVersions {

    // These lines are read by the build script.
    public final static String SERIES_20 = VERSIONS['asciidoctorj']
    public final static String GROOVYDSL_SERIES_20 = VERSIONS['asciidoctorj.groovydsl']
    public final static String DIAGRAM_SERIES_20 = VERSIONS['asciidoctorj.diagram']
}
