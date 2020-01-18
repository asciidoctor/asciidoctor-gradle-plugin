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

import groovy.transform.CompileStatic

/** Strategy to set where the base directory should be relative to
 * a project.
 *
 * @author Schalk W. Cronjé
 *
 * @since 2.2.0
 */
@CompileStatic
interface BaseDirStrategy {

    /** Base directory location.
     *
     * @return Base directory
     */
    File getBaseDir()

    /** Base directory location for a specific language.
     *
     * @param lang Source language
     * @return Base directory
     *
     * @since 3.0.0
     */
    File getBaseDir(String lang)
}
