/*
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
package org.asciidoctor.gradle.model5.core.internal.attributes

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.attributes.IncludeDir
import org.asciidoctor.gradle.model5.core.attributes.IncludeDirOutputSettings
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations
import org.ysb33r.grolifant5.api.core.StringTools

import javax.inject.Inject

import static java.util.Collections.EMPTY_MAP

/**
 * Default implementation of {@link IncludeDir}
 * @author Schalk W. Cronjé
 *
 * @since 5.1
 */
@CompileStatic
class DefaultIncludeDir implements IncludeDir, IncludeDirOutputSettings {
    public static final String INCLUDEDIR = 'includedir'
    public static final String INCLUDEDIR_SOFT = 'includedir@'

    final Provider<String> attributeName
    final Provider<Map<String, Object>> attributeProvider

    private final Property<Boolean> soft
    private final Property<String> value
    private final Property<Boolean> pathIsSourceDir
    private final StringTools stringTools

    @Inject
    @SuppressWarnings('UnnecessaryCast')
    DefaultIncludeDir(Project project) {
        final ccso = ConfigCacheSafeOperations.from(project)
        this.stringTools = ccso.stringTools()
        this.soft = project.objects.property(Boolean).convention(false)
        this.pathIsSourceDir = project.objects.property(Boolean).convention(false)
        this.value = project.objects.property(String)
        this.attributeName = this.soft.map { it ? INCLUDEDIR_SOFT : INCLUDEDIR }

        this.attributeProvider = this.value.zip(this.attributeName) { v, n ->
            [n: v] as Map<String, Object>
        }.orElse(EMPTY_MAP as Map<String, Object>)
    }

    @Override
    void setSoft(boolean flag) {
        this.soft.set(flag)
    }

    @Override
    void setSoft(Provider<Boolean> flag) {
        this.soft.set(flag)
    }

    @Override
    void withFixedPath(Object path) {
        this.value.set(stringTools.provideStringOrNull(path))
        this.pathIsSourceDir.set(false)
    }

    @Override
    Provider<String> getPath() {
        this.value
    }

    @Override
    void withSourceDir() {
        this.value.set((String) null)
        this.pathIsSourceDir.set(true)
    }

    @Override
    Provider<Boolean> getUseSourceDirOverPath() {
        this.pathIsSourceDir
    }
}
