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

/**
 * @author Schalk W. Cronjé
 *
 * @since 2.0.0
*/
@CompileStatic
class JRubyTestVersions {

    // These lines are read by the build script.
    // AJ20: AciidoctorJ 2.0.x
    // ABSOLUTE_MINIMUM The lowest JRuby to test against (compatibility not guaranteed)
    // ABSOLUTE_MINIMUM The highest JRuby to test against (compatibility not guaranteed)
    // SAFE_MINIMUM The lowest known compatible version of JRuby
    // SAFE_MAXIMUM The highest known compatible version of JRuby
    // ----------------------------------------------------------
    public final static String AJ20_ABSOLUTE_MINIMUM = '9.2.5.0'
    public final static String AJ20_SAFE_MINIMUM = '9.2.5.0'
    public final static String AJ20_SAFE_MAXIMUM = '9.2.7.0'
    public final static String AJ20_ABSOLUTE_MAXIMUM = '9.2.9.0'
    // ----------------------------------------------------------
}
