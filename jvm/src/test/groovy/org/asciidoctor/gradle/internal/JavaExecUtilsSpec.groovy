/*
 * Copyright 2013-2021 the original author or authors.
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
import spock.lang.Specification
import spock.lang.TempDir

import static org.asciidoctor.gradle.internal.JavaExecUtils.getInternalGuavaLocation

class JavaExecUtilsSpec extends Specification {

    @TempDir
    File temporaryFolder

    void 'Throw exception if internal Guava cannot be found'() {
        setup:
        def gradle = Stub(Gradle)
        gradle.gradleHomeDir >> temporaryFolder

        when:
        getInternalGuavaLocation(gradle)

        then:
        def e = thrown(JavaExecUtils.InternalGuavaLocationException)
        e.message.contains('Cannot locate a Guava JAR in the Gradle distribution')
    }

    void 'Throw exception if multiple internal Guava JARs are found'() {
        setup:
        def gradle = Stub(Gradle)
        gradle.gradleHomeDir >> temporaryFolder
        new File(temporaryFolder, 'lib').mkdirs()
        new File(temporaryFolder, 'lib/guava-0.0-android.jar').text = ''
        new File(temporaryFolder, 'lib/guava-0.1-android.jar').text = ''

        when:
        getInternalGuavaLocation(gradle)

        then:
        def e = thrown(JavaExecUtils.InternalGuavaLocationException)
        e.message.contains('Found more than one Guava JAR in the Gradle distribution')
    }

    void 'detect jre variant of guava'() {
        setup:
        def gradle = Stub(Gradle)
        gradle.gradleHomeDir >> temporaryFolder
        new File(temporaryFolder, 'lib').mkdirs()
        new File(temporaryFolder, 'lib/guavasomething.jar').text = ''
        def guavaJar = new File(temporaryFolder, 'lib/guava-30.0-jre.jar')
        guavaJar.text = ''

        when:
        def location = JavaExecUtils.getInternalGuavaLocation(gradle)

        then:
        location == guavaJar
    }
}
