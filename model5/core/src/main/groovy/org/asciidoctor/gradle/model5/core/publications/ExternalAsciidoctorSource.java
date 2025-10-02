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
package org.asciidoctor.gradle.model5.core.publications;

import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Provider;

import java.util.List;

/**
 * For defining external sources.
 *
 * @author Schalk W. Cronjé
 * @since 5.0
 */
public interface ExternalAsciidoctorSource extends HasAsciidoctorSource, HasAsciidoctorResources {
    /**
     * Tasks that are responsible for building providing external sources and resources.
     *
     * @param tasks Evaluated as per normal {@code dependsOn} rules.
     */
    void builtBy(Object... tasks);

    /**
     * Tasks that build this external source.
     *
     * @return Provider to the list of tasks. Can be empty, but not {@code null}.
     */
    Provider<List<Object>> getBuiltBy();

    /**
     * Instead of a source directory, you can provide a file collection. This is useful when taking a configuration
     * from another subproject.
     *
     * @param fc Collection of files.
     */
    void from(FileCollection fc);

    /**
     * If the eternal source needs to be placed in a subfolder, then specify that here.
     *
     * @param path sub path in intermediate workdir and destination dir.
     */
    void into(Object path);
}
