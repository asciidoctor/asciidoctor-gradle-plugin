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
package org.asciidoctor.gradle.kramdown.internal

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.kramdown.KramdownConversionSettings
import org.asciidoctor.gradle.kramdown.MarkdownFormat
import org.gradle.api.file.Directory
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.SetProperty

import javax.inject.Inject
import java.util.regex.Pattern

import static java.util.Collections.EMPTY_MAP
import static java.util.Collections.EMPTY_SET

/**
 * Default implementaiton of {@link KramdownConversionSettings}.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.1
 */
@CompileStatic
class DefaultKramdownConversionSettings implements KramdownConversionSettings {
    final SetProperty<File> sourceFiles
    final Property<Directory> sourceRootDir
    final Property<Directory> destinationDir
    final MapProperty<String, String> attributes
    final MapProperty<String, String> customProperties
    final SetProperty<Pattern> fatalWarnings
    final Property<MarkdownFormat> sourceFormat
    final Provider<Map<String, String>> inputProperties

    @Inject
    @SuppressWarnings('UnnecessaryCast')
    DefaultKramdownConversionSettings(ObjectFactory objectFactory) {
        this.sourceFiles = objectFactory.setProperty(File)
        this.sourceRootDir = objectFactory.directoryProperty()
        this.destinationDir = objectFactory.directoryProperty()
        this.attributes = objectFactory.mapProperty(String, String)
        this.customProperties = objectFactory.mapProperty(String, String)
        this.fatalWarnings = objectFactory.setProperty(Pattern)
        this.sourceFormat = objectFactory.property(MarkdownFormat)
        this.inputProperties = attributes.orElse(EMPTY_MAP).zip(fatalWarnings.orElse(EMPTY_SET)) { attrs, fw ->
            [
                attrs: attrs.toString(),
                fw   : fw*.toString().toString(),
            ] as Map<String, String>
        }
    }
}
