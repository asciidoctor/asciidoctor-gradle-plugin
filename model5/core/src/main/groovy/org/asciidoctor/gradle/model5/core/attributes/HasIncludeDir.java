/**
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
package org.asciidoctor.gradle.model5.core.attributes;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;
import org.ysb33r.grolifant5.api.core.ClosureUtils;

/**
 * Manages include dir for Asciidoc processing.
 * @author Schalk W. Cronjé
 * @since 5.1
 */
public interface HasIncludeDir {
    /**
     * Configures the include directory  for this publication
     *
     * @param configurator Configurator
     */
    default void includeDir(Action<IncludeDir> configurator) {
        configurator.execute(getIncludeDir());
    }

    /**
     * Configures the include directory for this publication
     *
     * @param configurator Configurator
     */
    default void includeDir(@DelegatesTo(IncludeDir.class) Closure configurator) {
        ClosureUtils.configureItem(getIncludeDir(), configurator);
    }

    /**
     * Direct access to the management of include directory processing.
     * @return An instance that implements {@link IncludeDir}
     */
    IncludeDir getIncludeDir();
}
