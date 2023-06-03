/*
 * Copyright 2013-2024 the original author or authors.
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
package org.asciidoctor.gradle.jvm;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.asciidoctor.gradle.base.AsciidoctorModuleDefinition;
import org.gradle.api.Action;

/**
 * Define versions for standard AsciidoctorJ modules.
 *
 * @since 4.0
 *
 * @author Schalk W. Cronjé
 */
public interface AsciidoctorJModules {

    /**
     *
     * @param cfg
     */
    default void pdf(Action<AsciidoctorModuleDefinition> cfg) {
        cfg.execute(getPdf());
    }

    /**
     *
     * @param cfg
     */
    void pdf(@DelegatesTo(AsciidoctorModuleDefinition.class) Closure cfg);

    /**
     *
     * @return
     */
    AsciidoctorModuleDefinition getPdf();

    /**
     *
     * @param cfg
     */
    default void epub(Action<AsciidoctorModuleDefinition> cfg) {
        cfg.execute(getEpub());
    }

    /**
     *
     * @param cfg
     */
    void epub(@DelegatesTo(AsciidoctorModuleDefinition.class) Closure cfg);

    /**
     *
     * @return
     */
    AsciidoctorModuleDefinition getEpub();

    /**
     *
     * @param cfg
     */
    default void leanpub(Action<AsciidoctorModuleDefinition> cfg) {
        cfg.execute(getLeanpub());
    }

    /**
     *
     * @param cfg
     */
    void leanpub(@DelegatesTo(AsciidoctorModuleDefinition.class) Closure cfg);

    /**
     *
     * @return
     */
    AsciidoctorModuleDefinition getLeanpub();

    /**
     *
     * @param cfg
     */
    default void diagram(Action<AsciidoctorModuleDefinition> cfg) {
        cfg.execute(getDiagram());
    }

    /**
     *
     * @param cfg
     */
    void diagram(@DelegatesTo(AsciidoctorModuleDefinition.class) Closure cfg);

    /**
     *
     * @return
     */
    AsciidoctorModuleDefinition getDiagram();

    /**
     *
     * @param cfg
     */
    default void groovyDsl(Action<AsciidoctorModuleDefinition> cfg) {
        cfg.execute(getGroovyDsl());
    }

    /**
     *
     * @param cfg
     */
    void groovyDsl(@DelegatesTo(AsciidoctorModuleDefinition.class) Closure cfg);

    /**
     *
     * @return
     */
    AsciidoctorModuleDefinition getGroovyDsl();
}
