/*
 * Copyright 2013 - 2026 the original author or authors.
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
package org.asciidoctor.gradle.model5.core.internal.engines

import spock.lang.Issue
import spock.lang.Specification

import static org.asciidoctor.gradle.model5.core.internal.engines.EngineUtils.RELATIVE_SRCDIR_ATTRIBUTE

@Issue('https://github.com/asciidoctor/asciidoctor-gradle-plugin/issues/803')
class EngineUtilsSpec extends Specification {

    void "The relative source directory for '#relPath' is '#expected'"() {
        expect:
        EngineUtils.relativeSrcDir(relPath) == expected

        where:
        relPath    | expected
        ''         | '.'
        'sub'      | '..'
        'sub/dir'  | '../..'
        'sub\\dir' | '../..'
        'a/b/c/'   | '../../..'
    }

    void 'gradle-relative-srcdir is added to the attributes'() {
        expect:
        EngineUtils.withRelativeSrcDir([toc: 'left'], 'sub') == [toc: 'left', (RELATIVE_SRCDIR_ATTRIBUTE): '..']
    }

    void 'gradle-relative-srcdir that is already set is kept'() {
        expect:
        EngineUtils.withRelativeSrcDir([(RELATIVE_SRCDIR_ATTRIBUTE): 'custom'], 'sub') ==
            [(RELATIVE_SRCDIR_ATTRIBUTE): 'custom']
    }
}
