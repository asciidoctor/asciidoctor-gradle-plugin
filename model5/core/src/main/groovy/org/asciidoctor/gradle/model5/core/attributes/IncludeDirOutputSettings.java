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

import org.asciidoctor.gradle.model5.core.AsciidoctorConversionSettings;
import org.asciidoctor.gradle.model5.core.basedir.BaseDirStrategy;
import org.gradle.api.provider.Provider;

/**
 * Used as part of {@link AsciidoctorConversionSettings}.
 *
 * @author Schalk W. Cronjé
 * @since 5.1
 */
public interface IncludeDirOutputSettings {
    /**
     * Name of the attribute used for include directory
     *
     * @return Attribute name. Always present.
     */
    Provider<String> getAttributeName();

    /**
     * Contains a value if a fixed value has been set
     *
     * @return Fixed value. Will be an empty provider if pathe was not configured.
     */
    Provider<String> getPath();

    /**
     * Whehter the source directory should be use an absolute path for the include directory
     *
     * @return {@code true} if the include directory should be the source directory.
     *   Never empty.
     */
    Provider<Boolean> getUseSourceDirOverPath();
}
