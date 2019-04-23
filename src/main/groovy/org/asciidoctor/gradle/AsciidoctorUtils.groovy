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

import groovy.transform.CompileStatic
import org.ysb33r.grolifant.api.FileUtils

import java.util.regex.Pattern

class AsciidoctorUtils {

    /**
     * Based on
     * http://stackoverflow.com/questions/204784/how-to-construct-a-relative-path-in-java-from-two-absolute-paths-or-urls/1290311#1290311
     *
     * Returns the path of one File relative to another.
     *
     * @param target the target directory
     * @param base the base directory
     * @return target's path relative to the base directory
     * @throws IOException if an error occurs while resolving the files' canonical names
     */
    static String getRelativePath(File target, File base) throws IOException {
        String[] baseComponents = base.canonicalPath.split(Pattern.quote(File.separator))
        String[] targetComponents = target.canonicalPath.split(Pattern.quote(File.separator))

        // skip common components
        int index = 0
        for (; index < targetComponents.length && index < baseComponents.length; ++index) {
            if (!targetComponents[index].equals(baseComponents[index])) {
                break
            }
        }

        StringBuilder result = new StringBuilder()
        if (index != baseComponents.length) {
            // backtrack to base directory
            for (int i = index; i < baseComponents.length; ++i) {
                if (i != index) {
                    result.append(File.separator)
                }
                result.append('..')
            }
        }
        for (int i = index; i < targetComponents.length; ++i) {
            if (i != index) {
                result.append(File.separator)
            }
            result.append(targetComponents[i])
        }

        result.toString()
    }

    @SuppressWarnings('Instanceof')
    @CompileStatic
    static List<File> getContextClasspath() {
        ClassLoader cl = Thread.currentThread().contextClassLoader
        if (cl instanceof URLClassLoader) {
            ((URLClassLoader) cl).URLs.findAll { URL u ->
                u.toURI().scheme == 'file'
            } as List<File>
        } else {
            []
        }
    }

    @CompileStatic
    static File getClassLocation(Class aClass) {
        FileUtils.resolveClassLocation(aClass).file
    }

    /** Returns the location of the local Groovy Jar that is used by Gradle.
     *
     * @return Location on filesysetm where the Groovy Jar is located.
     *
     * @since 1.5.12 (backported form 2.1.0)
     */
    static File getLocalGroovy() {
        getClassLocation(GroovyObject)
    }

}
