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
package org.asciidoctor.gradle.model5.reducer.engines;

import org.asciidoctor.gradle.model5.jrubytools.engines.JrubyEngineOptions;
import org.ysb33r.gradle.jruby.api.core.JRubyEnvironmentInheritance;

/**
 * OPtions for configuring the {@code Reducer} engine.
 *
 * @author Schalk W. Cronjé
 * @since 5.1
 */
public interface EngineOptions extends JrubyEngineOptions {
    /**
     * Whether to trace the cause of application errors.
     * @param flag {@code true} when tracing should be enabled.
     *                           Default is {@code false}.
     */
    void setTrace(boolean flag);
}
