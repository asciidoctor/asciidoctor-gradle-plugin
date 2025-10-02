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
package org.asciidoctor.gradle.model5.core;

import org.gradle.api.provider.Provider;

import java.util.Collections;
import java.util.Set;

/**
 * Items such as output formatters and extensions may need to supply a list of required components to an
 * Asciidoctor engine.
 *
 *
 * @author Schalk W. Cronjé
 * @since 5.0
 */
public interface AsciidoctorRequires {
    /**
     * A list of {@code requires} that a component places on the associated toolchain.
     *
     * @return List of {@code requires}. Can be empty, but never {@code null}.
     */
    Provider<Set<String>> getRequires();
}
