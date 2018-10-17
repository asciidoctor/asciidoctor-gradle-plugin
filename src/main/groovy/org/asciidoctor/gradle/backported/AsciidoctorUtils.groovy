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
package org.asciidoctor.gradle.backported

import groovy.transform.CompileStatic

/**
 * @since 1.5.9 (Backported from 2.0)
 */
@CompileStatic
class AsciidoctorUtils {

    /** Returns the location of a class
     *
     * @param aClass Class to look for.
     * @return Location as a file on disk.
     */
    static File getClassLocation(Class aClass) {
        new File(aClass.protectionDomain.codeSource.location.toURI()).absoluteFile
    }

}
