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

import spock.lang.Specification

class AsciidoctorUtilsSpec extends Specification {

    static s = File.separator

    def 'finds relative paths'(String target, String base, String relative) {
        expect:
            AsciidoctorUtils.getRelativePath(new File(target), new File(base)) == relative
        where:
            target             | base               | relative
            'src/test/groovy'  | 'src'              | "test${s}groovy"
            'src/test/groovy/' | 'src/'             | "test${s}groovy"
            'src/test/groovy'  | 'src/'             | "test${s}groovy"
            'src/test/groovy/' | 'src'              | "test${s}groovy"
            'src'              | 'src/test/groovy'  | "..${s}.."
            'src/'             | 'src/test/groovy/' | "..${s}.."
            'src'              | 'src/test/groovy/' | "..${s}.."
            'src/'             | 'src/test/groovy'  | "..${s}.."
            'src/test'         | 'src/test'         | ''
            'src/test/'        | 'src/test/'        | ''
            'src/test'         | 'src/test/'        | ''
            'src/test/'        | 'src/test'         | ''
    }
}
