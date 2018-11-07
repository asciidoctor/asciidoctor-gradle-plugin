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
class JRubyTestVersions {

    // These lines are read by the build script.
    // AJ15: AciidoctorJ 1.5.x
    // ABSOLUTE_MINIMUM The lowest JRuby to test against (compatibility not guaranteed)
    // ABSOLUTE_MINIMUM The highest JRuby to test against (compatibility not guaranteed)
    // SAFE_MINIMUM The lowest known compatible version of JRuby
    // SAFE_MAXIMUM The highest known compatible version of JRuby
    // ----------------------------------------------------------
    final static String AJ15_ABSOLUTE_MINIMUM = '1.7.27'
    final static String AJ15_SAFE_MINIMUM = '9.0.5.0'
    final static String AJ15_SAFE_MAXIMUM = '9.1.17.0'
    final static String AJ15_ABSOLUTE_MAXIMUM = '9.2.0.0'

    final static String AJ16_ABSOLUTE_MINIMUM = '1.7.27'
    final static String AJ16_SAFE_MINIMUM = '9.1.0.0'
    final static String AJ16_SAFE_MAXIMUM = '9.2.0.0'
    final static String AJ16_ABSOLUTE_MAXIMUM = '9.2.1.0'
    // ----------------------------------------------------------

}
