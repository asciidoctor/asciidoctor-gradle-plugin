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
package org.asciidoctor.gradle.model5.jvm.extensions;

/**
 * @author Schalk W. Cronjé
 * @since 5.0
 */
public interface AsciidoctorjDiagram extends AsciidoctorjExtension {

    /**
     * Use Diagram with default version.
     */
    void useDiagram();

    /**
     * Use Diagram and override the default version.
     *
     * @param ver New version
     */
    void useDiagram(Object ver);

    /**
     * Use Ditaa.
     */
    void useDitaa();

    /**
     * Use Ditaa and override the default version.
     *
     * @param ver New version
     */
    void useDitaa(Object ver);

    /**
     * Use PlantUML.
     */
    void usePlantUml();

    /**
     * Use PlantUML and override the default version.
     *
     * @param ver New version
     */
    void usePlantUml(Object ver);

    /**
     * Use Batik
     */
    void useBatik();

    /**
     * Use Batik and override the default version.
     *
     * @param ver New version
     */
    void useBatik(Object ver);

    /**
     * Use JSyntrax,
     */
    void useJSyntrax();

    /**
     * Use JSyntrax and override the default version.
     *
     * @param ver New version
     */
    void useJSyntrax(Object ver);
}
