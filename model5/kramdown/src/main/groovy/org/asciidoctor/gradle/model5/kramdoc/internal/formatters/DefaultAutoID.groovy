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
package org.asciidoctor.gradle.model5.kramdoc.internal.formatters

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.kramdoc.formatters.AutoID
import org.asciidoctor.gradle.model5.core.errors.ConfigurationNotSupportedException
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations
import org.ysb33r.grolifant5.api.core.StringTools

import javax.inject.Inject

/**
 * Default implementation of {@link AutoID}.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.1
 */
@CompileStatic
class DefaultAutoID implements AutoID {
    final Property<Boolean> generateIDs
    final Property<Boolean> drop
    final Property<String> prefixStr
    final Property<String> separatorChar
    final Provider<? super Map<? super String, ? super String>> inputProperty

    private final StringTools stringTools

    @Inject
    DefaultAutoID(Project project) {
        this.generateIDs = project.objects.property(Boolean).convention(false)
        this.drop = project.objects.property(Boolean).convention(true)
        this.prefixStr = project.objects.property(String)
        this.separatorChar = project.objects.property(String)

        final ccso = ConfigCacheSafeOperations.from(project)
        this.stringTools = ccso.stringTools()

        this.inputProperty = ccso.providerTools().zip4(
            generateIDs.map { it.toString() },
            drop.map { it.toString() }.orElse(StringTools.EMPTY),
            prefixStr.orElse(StringTools.EMPTY),
            separatorChar.orElse(StringTools.EMPTY)
        ) { a, b, c, d ->
            final Map<String, String> map = [generate: a, drop: b, ps: c, pc: d]
            map.asImmutable()
        }
    }

    void setGenerate(boolean flag) {
        this.generateIDs.set(flag)
    }

    @Override
    void setPrefix(Object prefix) {
        this.prefixStr.set(stringTools.provideString(prefix))
    }

    @Override
    void setSeparator(String separator) {
        if (separator?.size() > 1 || separator?.empty) {
            throw new ConfigurationNotSupportedException(
                'The kramdown-asciidoc separator can only be a single character'
            )
        }
        this.separatorChar.set(separator)
    }

    @Override
    void setDropLazyValues(boolean flag) {
        this.drop.set(flag)
    }
}
