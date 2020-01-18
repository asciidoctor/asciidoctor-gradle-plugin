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
package org.asciidoctor.gradle.testfixtures.generators

import groovy.transform.CompileStatic

/** Testfixture that generates process modes.
 *
 * @since 2.0.0
 */
@CompileStatic
@SuppressWarnings('DuplicateStringLiteral')
class ProcessGenerator {
    static List<String> get() {
        if (System.getenv('APPVEYOR')) {
            ['JAVA_EXEC']
        } else {
            ['IN_PROCESS', 'OUT_OF_PROCESS', 'JAVA_EXEC']
        }
    }
}
