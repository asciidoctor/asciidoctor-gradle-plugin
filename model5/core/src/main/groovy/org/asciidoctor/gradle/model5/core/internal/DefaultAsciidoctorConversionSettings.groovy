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
package org.asciidoctor.gradle.model5.core.internal

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.AsciidoctorConversionSettings
import org.asciidoctor.gradle.model5.core.AsciidoctorNamedBackend
import org.asciidoctor.gradle.model5.core.ConversionTemplate
import org.asciidoctor.gradle.model5.core.DocType
import org.asciidoctor.gradle.model5.core.ScriptCollection
import org.gradle.api.file.Directory
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty

import javax.inject.Inject
import java.util.regex.Pattern

/**
 * Default implementation of conversion settings used by a an
 * {@link org.asciidoctor.gradle.model5.core.AsciidoctorLauncher}
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class DefaultAsciidoctorConversionSettings implements AsciidoctorConversionSettings {

    final SetProperty<File> sourceFiles
    final Property<AsciidoctorNamedBackend> backend
    final Property<Directory> sourceRootDir
    final Property<Directory> baseDir
    final Property<Boolean> adjustBaseDirPerFile
    final Property<Directory> destinationDir
    final MapProperty<String, String> attributes
    final Property<DocType> docType
    final SetProperty<Pattern> fatalWarnings
    final Property<Boolean> embedded
    final Property<ConversionTemplate> templates
    final MapProperty<String, ScriptCollection> scriptCollections

    @Inject
    DefaultAsciidoctorConversionSettings(ObjectFactory objectFactory) {
        this.sourceFiles = objectFactory.setProperty(File)
        this.backend = objectFactory.property(AsciidoctorNamedBackend)
        this.sourceRootDir = objectFactory.directoryProperty()
        this.baseDir = objectFactory.directoryProperty()
        this.adjustBaseDirPerFile = objectFactory.property(Boolean).convention(false)
        this.destinationDir = objectFactory.directoryProperty()
        this.attributes = objectFactory.mapProperty(String, String)
        this.docType = objectFactory.property(DocType)
        this.fatalWarnings = objectFactory.setProperty(Pattern)
        this.embedded = objectFactory.property(Boolean).convention(false)
        this.templates = objectFactory.property(ConversionTemplate)
        this.scriptCollections = objectFactory.mapProperty(String, ScriptCollection)
    }

    void updateFrom(DefaultAsciidoctorConversionSettings other) {
        sourceFiles.set(other.sourceFiles)
        backend.set(other.backend)
        sourceRootDir.set(other.sourceRootDir)
        baseDir.set(other.baseDir)
        adjustBaseDirPerFile.set(other.adjustBaseDirPerFile)
        destinationDir.set(other.destinationDir)
        attributes.set(other.attributes)
        docType.set(other.docType)
        fatalWarnings.set(other.fatalWarnings)
        embedded.set(other.embedded)
        templates.set(other.templates)
        scriptCollections.set(other.scriptCollections)
    }
}
