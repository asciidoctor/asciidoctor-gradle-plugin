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
package org.asciidoctor.gradle.testfixtures

import groovy.transform.CompileStatic
import org.gradle.util.GradleVersion

/**
 * Verisons of Gradle that can be used for testing with TestKit.
 *
 * @author Schalk W. Cronjé
 *
 * @since 4.0.0
 */
@CompileStatic
class GradleTestVersions {
    public final static String MIN_VERSION = '7.0.1'
    public final static String MAX_VERSION = '8.5'

    static String latestMinimumOrThis(final String ver) {
        [GradleVersion.version(MIN_VERSION), GradleVersion.version(ver)].max().version
    }
}
