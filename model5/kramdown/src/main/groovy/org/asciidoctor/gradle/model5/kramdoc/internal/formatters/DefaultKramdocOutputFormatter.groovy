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
import org.asciidoctor.gradle.model5.kramdoc.KramdocWrapMode
import org.asciidoctor.gradle.model5.kramdoc.formatters.AutoID
import org.asciidoctor.gradle.model5.kramdoc.formatters.KramdocOutputFormatter
import org.asciidoctor.gradle.model5.core.attributes.Attributes
import org.asciidoctor.gradle.model5.core.internal.attributes.DefaultAttributes
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskInputs
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations
import org.ysb33r.grolifant5.api.core.ProviderTools

import javax.inject.Inject

import static java.util.Collections.EMPTY_LIST
import static org.ysb33r.grolifant5.api.core.StringTools.COMMA
import static org.ysb33r.grolifant5.api.core.StringTools.EMPTY

/**
 * Default implementation of {@link KramdocOutputFormatter}
 * @author Schalk W. Cronjé
 *
 * @since 5.1
 */
@CompileStatic
class DefaultKramdocOutputFormatter implements KramdocOutputFormatter {
    public static final String DEFAULT_NAME = 'asciidoc'
    public static final String KEY_LANGS = 'diagramLanguages'
    public static final String KEY_PASSTHRU = 'htmlToNative'
    public static final String KEY_AUTOLINK = 'autoLinks'
    public static final String KEY_OFFSET = 'headingOffset'
    public static final String KEY_WRAP_MODE = 'wrap'
    public static final String KEY_AUTOID_GENERATE = 'autoid'
    public static final String KEY_AUTOID_PREFIX = 'prefix'
    public static final String KEY_AUTOID_SEPARATOR = 'separator'
    public static final String KEY_AUTOID_DROP = 'dropLazy'

    static class Factory implements NamedDomainObjectFactory<KramdocOutputFormatter> {

        private final ObjectFactory objectFactory

        @Inject
        Factory(Project project) {
            this.objectFactory = project.objects
        }

        @Override
        KramdocOutputFormatter create(String name) {
            objectFactory.newInstance(DefaultKramdocOutputFormatter, name)
        }
    }

    final String name
    final boolean copyResources = true
    final Provider<Map<String, String>> customProperties
    private final ListProperty<String> languages
    private final Property<Boolean> passthru
    private final Property<Boolean> autolink
    private final Property<Integer> offset
    private final Property<KramdocWrapMode> wrapMode
    private final DefaultAttributes attrs
    private final DefaultAutoID autoID

    @Inject
    DefaultKramdocOutputFormatter(String name, Project project) {
        this.name = name
        this.languages = project.objects.listProperty(String).value(['plantuml', 'mermaid'])
        this.passthru = project.objects.property(Boolean).convention(true)
        this.autolink = project.objects.property(Boolean).convention(true)
        this.wrapMode = project.objects.property(KramdocWrapMode).convention(KramdocWrapMode.PRESERVE)
        this.offset = project.objects.property(Integer).convention(0)
        this.attrs = project.objects.newInstance(DefaultAttributes)
        this.autoID = project.objects.newInstance(DefaultAutoID)
        this.customProperties = buildCustomProperties(ConfigCacheSafeOperations.from(project).providerTools())
    }

    @Override
    void setDiagramLanguages(Iterable<String> langs) {
        this.languages.set(langs)
    }

    @Override
    void diagramLanguages(String... langs) {
        this.languages.addAll(langs)
    }

    @Override
    void diagramLanguages(Iterable<String> langs) {
        this.languages.addAll(langs)
    }

    @Override
    void setPassthruHtml(boolean flag) {
        this.passthru.set(flag)
    }

    @Override
    void setWrap(KramdocWrapMode mode) {
        this.wrapMode.set(mode)
    }

    @Override
    void setHeadingOffset(int offset) {
        this.offset.set(offset)
    }

    @Override
    void setAutoLinks(boolean flag) {
        this.autolink.set(flag)
    }

    @Override
    AutoID getAutoIds() {
        this.autoID
    }

    @Override
    Attributes getAttributes() {
        this.attrs
    }

    @Override
    void configureTaskInputs(TaskInputs taskInputs) {
        taskInputs.property(DefaultKramdocOutputFormatter.canonicalName, customProperties).optional(true)
    }

    private Provider<Map<String, String>> buildCustomProperties(ProviderTools pt) {
        final part1 = pt.zip4(
            this.languages.orElse(EMPTY_LIST),
            this.passthru,
            this.autolink.map { it.toString() }.orElse(EMPTY),
            this.offset.map { it.toString() }
        ) { a, b, c, d ->
            final map = [(KEY_LANGS): a.join(COMMA)]

            if (b) {
                map.put(KEY_PASSTHRU, EMPTY)
            }

            if (c) {
                map.put(KEY_AUTOLINK, EMPTY)
            }

            if (d != 0) {
                map.put(KEY_OFFSET, d.toString())
            }
            map
        }

        final part2 = pt.zip4(
            autoID.generateIDs,
            autoID.prefixStr.orElse(EMPTY),
            autoID.drop,
            autoID.separatorChar.orElse(EMPTY)
        ) { a, b, c, d ->
            if (a) {
                final map = [(KEY_AUTOID_GENERATE): EMPTY]
                if (b) {
                    map.put(KEY_AUTOID_PREFIX, b)
                }
                if (c) {
                    map.put(KEY_AUTOID_DROP, EMPTY)
                }
                if (d) {
                    map.put(KEY_AUTOID_SEPARATOR, d)
                }
                map
            } else {
                Collections.EMPTY_MAP as Map<String,String>
            }
        }

        pt.zip3(
            wrapMode.map { it.name().toLowerCase(Locale.US) }.orElse(EMPTY),
            part1,
            part2
        ) { wm, p1, p2 ->
            final map = wm ? [(KEY_WRAP_MODE) : wm] : [:]
            p1 + p2 + map
        }
    }
}
