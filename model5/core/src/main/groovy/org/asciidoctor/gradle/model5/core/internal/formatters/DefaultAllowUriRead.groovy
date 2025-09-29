/*
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
package org.asciidoctor.gradle.model5.core.internal.formatters

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.attributes.HasAttributeProvider
import org.asciidoctor.gradle.model5.core.formatters.HasAllowUriRead
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider

import javax.inject.Inject

import static java.util.Collections.EMPTY_MAP
import static org.ysb33r.grolifant5.api.core.StringTools.EMPTY

/**
 * Default implementation of {@link HasAllowUriRead}.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class DefaultAllowUriRead implements HasAllowUriRead, HasAttributeProvider {

    final Provider<Map<String, Object>> attributeProvider
    private final Property<Boolean> allowUri

    @Inject
    DefaultAllowUriRead(Project project) {
        this.allowUri = project.objects.property(Boolean).value(false)
        this.attributeProvider = this.allowUri.map {
            it ? ['allow-uri-read': EMPTY] : EMPTY_MAP
        }
    }

    @Override
    void setAllowUriRead(boolean flag) {
        this.allowUri.set(flag)
    }
}
