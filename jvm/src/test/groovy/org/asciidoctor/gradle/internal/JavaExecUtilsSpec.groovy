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
package org.asciidoctor.gradle.internal

import org.gradle.api.invocation.Gradle
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.asciidoctor.gradle.internal.JavaExecUtils.getInternalGuavaLocation

class JavaExecUtilsSpec extends Specification {

    @Rule
    TemporaryFolder temporaryFolder

    void 'Throw exception if internal Guava cannot be found'() {
        setup:
        def gradle = Stub(Gradle)
        gradle.gradleHomeDir >> temporaryFolder.root

        when:
        getInternalGuavaLocation(gradle)

        then:
        def e = thrown(JavaExecUtils.InternalGuavaLocationException)
        e.message.contains('Cannot locate a Guava JAR in the Gradle distribution')
    }

    void 'Throw exception if multiple internal Guava JARs are found'() {
        setup:
        def gradle = Stub(Gradle)
        gradle.gradleHomeDir >> temporaryFolder.root
        new File(temporaryFolder.root, 'lib').mkdirs()
        new File(temporaryFolder.root, 'lib/guava-0.0-android.jar').text = ''
        new File(temporaryFolder.root, 'lib/guava-0.1-android.jar').text = ''

        when:
        getInternalGuavaLocation(gradle)

        then:
        def e = thrown(JavaExecUtils.InternalGuavaLocationException)
        e.message.contains('Found more than one Guava JAR in the Gradle distribution')
    }
}