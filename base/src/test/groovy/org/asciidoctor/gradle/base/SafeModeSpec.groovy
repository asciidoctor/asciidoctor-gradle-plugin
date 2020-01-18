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
package org.asciidoctor.gradle.base

import spock.lang.Specification

import static org.asciidoctor.gradle.base.SafeMode.SAFE
import static org.asciidoctor.gradle.base.SafeMode.SECURE
import static org.asciidoctor.gradle.base.SafeMode.SERVER
import static org.asciidoctor.gradle.base.SafeMode.UNSAFE
import static org.asciidoctor.gradle.base.SafeMode.safeMode

class SafeModeSpec extends Specification {

    void 'Integers map to correct safe modes'() {
        expect:
        safeMode(0) == UNSAFE
        safeMode(1) == SAFE
        safeMode(10) == SERVER
        safeMode(20) == SECURE
        safeMode(35) == SECURE

        SAFE.level == 1
    }
}