/**
 * Copyright 2013 - 2025 the original author or authors.
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
package org.asciidoctor.gradle.model5.jvm.engines;

import java.util.Locale;

/**
 * Options for the setting eRuby engine.
 *
 * @author Schalk W. Cronjé
 * @since 5.0
 */
public enum ErbEngine {
    ERB, ERUBIS;

    public static ErbEngine from(String value) {
        return ErbEngine.valueOf(value.toUpperCase(Locale.US));
    }

    public String getOptionValue() {
        return name().toLowerCase(Locale.US);
    }
}
