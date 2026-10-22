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
package org.asciidoctor.gradle.model5.jrubytools.engines;

import org.ysb33r.gradle.jruby.api.core.JRubyEnvironmentInheritance;

/**
 * Options for configuring any document processing JRuby tool that does not use the Asciidoctor engine.
 *
 * @author Schalk W. Cronjé
 * @since 5.1
 */
public interface JrubyEngineOptions {
    /**
     * Configure how JRuby inherits the environment.
     *
     * @param mode Case-insensitive string that is one of {@code ESSENTIAL_ONLY}, {@code FULL},
     *             or {@code EXCLUDE_JRUBY}.
     */
    void setInheritRubyEnv(String mode);

    /**
     * Configure how JRuby inherits the environment.
     * @param mode Any value from {@link JRubyEnvironmentInheritance}.
     */
    void setInheritRubyEnv(JRubyEnvironmentInheritance mode);

    /**
     * Add the system path to the JRuby environment.
     */
    void withSystemPath();

    /**
     * Open known modules for execution.
     */
    void withModuleOpens();
}
