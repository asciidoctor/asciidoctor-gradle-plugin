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
package org.asciidoctor.gradle.model5.jvm.engines;

/**
 * Additional options for the {@code asciidoctorj} engine, which is not already covered elsewhere.
 *
 * @author Schalk W. Cronjé
 * @since 5.0
 */
public interface EngineOptions {

    /**
     * Sets the eRuby engine.
     *
     * @param engine Engine value
     */
    void setEruby(ErbEngine engine);

    /**
     * Sets the eRuby engine from a string value.
     *
     * @param engine Case-insensitive string.
     */
    default void setEruby(String engine) {
        setEruby(ErbEngine.from(engine));
    }

    /**
     * Whether to capture images and links in the reference table.
     *
     * @param flag {@code true} to capture images and links.
     */
    void setCatalogAssets(boolean flag);

    /**
     * Whether to track file and line numbers for parsed blocks.
     *
     * @param flag {@code true} to perform tracking.
     */
    void setSourceMap(boolean flag);
}
