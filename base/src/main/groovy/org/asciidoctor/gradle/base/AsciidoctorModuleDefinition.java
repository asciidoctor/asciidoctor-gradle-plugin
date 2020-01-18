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
package org.asciidoctor.gradle.base;

import org.gradle.api.Named;

/** Describes a converter.
 *
 * @since 2.2.0
 */
public interface AsciidoctorModuleDefinition extends Named {
    /** Use the default version of this component
     *
     */
    void use();

    /** Set the version of the component to use.
     *
     * @param o Any object can can be converted to a String using
     *          {@code org.ysb33r.grolifant.api.StringUtils.stringize}
     */
    void setVersion(Object o);

    /** Declarative way in DSL to set the version of the component to use.
     *
     * @param o Any object can can be converted to a String using
     *          {@code org.ysb33r.grolifant.api.StringUtils.stringize}
     */
    void version(Object o);

    /** Version of the component
     *
     * @return Returns version to use, or {@code null} if no version was set.
     *   The latter usually implies that the specific component is not needed.
     */
    String getVersion();

    /** Whether the component has been allocated a version.
     *
     * @return {@code true} if the component has been defined
     */
    boolean isDefined();
}
