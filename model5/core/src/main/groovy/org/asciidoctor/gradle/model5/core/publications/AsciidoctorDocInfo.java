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
package org.asciidoctor.gradle.model5.core.publications;

import org.asciidoctor.gradle.model5.core.CanConfigureTaskInputs;
import org.asciidoctor.gradle.model5.core.attributes.HasAttributeProvider;

/**
 * Methods for configuring {@code docinfo}
 *
 * @author Schalk W. Cronjé
 * @since 5.0
 */
public interface AsciidoctorDocInfo extends HasAttributeProvider, CanConfigureTaskInputs {

    /**
     * Setting this will apply to all source files in the source set.
     * It can still be overridden in a file.
     *
     * @param flag {@code true} to have private docinfo files.
     */
    void setHeadIsPrivate(boolean flag);

    /**
     * Setting this will apply to all source files in the source set.
     * It can still be overridden in a file.
     *
     * @param flag {@code true} to have private docinfo files.
     */
    void setHeaderIsPrivate(boolean flag);

    /**
     * Setting this will apply to all source files in the source set.
     * It can still be overridden in a file.
     *
     * @param flag {@code true} to have private docinfo files.
     */
    void setFooterIsPrivate(boolean flag);

    /**
     * Fix a specific directory to contain docinfo content.
     *
     * <p>
     *     Normally base dir determines docinfo discovery.
     *     If this is set, the base dir will be ignored.
     * </p>
     *
     * <p>
     *     Changes to any docinfo file in this directory will cause all conversion tasks related to the source set to
     *     be out-of-date. Currently, this includes even the tasks that are not docinfo-aware.
     *     Be wary of this constraint when using a common directory.
     * </p>
     *
     * @param dir Anything lazy-evaluatable to a directory.
     */
    void setDocInfoDir(Object dir);
}
