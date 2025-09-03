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
// tag::hacking-asciidoctorj-output-formatter[]
package org.asciidoctor.gradle.model5.jvm.internal.formatters

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.errors.IncorrectOutputFormatException
import org.asciidoctor.gradle.model5.jvm.JvmModel

// end::hacking-asciidoctorj-output-formatter[]

import org.asciidoctor.gradle.model5.jvm.formatters.AsciidoctorjEpub
import org.asciidoctor.gradle.model5.jvm.internal.PluginUtils
import org.asciidoctor.gradle.model5.jvm.toolchains.AsciidoctorjToolchain
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskInputs

import javax.inject.Inject

import static java.util.Collections.EMPTY_MAP

/**
 * Implementation of {@code asciidoctorj-epub} output formatter.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
// tag::hacking-asciidoctorj-output-formatter[]
@CompileStatic
class DefaultAsciidoctorjEpub extends AbstractAsciidoctorjFormatterVersioned implements AsciidoctorjEpub { // <.>
    public static final String DEFAULT_NAME = 'epub' // <.>
    public static final String BACKEND_NAME = 'epub3' // <.>

    final boolean copyResources = false // <.>

    // tag::hacking-asciidoctorj-output-formatter-attrs-fields[]
    final Property<Integer> level // <.>
    final DirectoryProperty stylesDir // <.>
    final DirectoryProperty frontmatterDir // <.>
    // end::hacking-asciidoctorj-output-formatter-attrs-fields[]

    @Inject
    DefaultAsciidoctorjEpub(String name, AsciidoctorjToolchain tc, Project project) {
        super(
                name,
                BACKEND_NAME, // <.>
                JvmModel.ASCIIDOCTORJ_EPUB_DEPENDENCY, // <.>
                PluginUtils.loadDefaultVersion('asciidoctorj.epub', project, tc.class.classLoader), // <.>
                tc,
                project
        )
        // end::hacking-asciidoctorj-output-formatter[]

        // tag::hacking-asciidoctorj-output-formatter-attrs-ctor[]
        this.level = project.objects.property(Integer)
        this.stylesDir = project.objects.directoryProperty()
        this.frontmatterDir = project.objects.directoryProperty()

        attributes.putAll(this.level.map {
            ['epub-chapter-level': it] as Map<String, Object>
        }.orElse(EMPTY_MAP)) // <.>

        attributes.putAll(this.stylesDir.map {
            ['epub3-stylesdir': it.asFile.absolutePath] as Map<String, Object>
        }.orElse(EMPTY_MAP)) // <.>

        attributes.putAll(this.frontmatterDir.map {
            ['epub3-frontmatterdir': it.asFile.absolutePath] as Map<String, Object>
        }.orElse(EMPTY_MAP)) // <.>
        // end::hacking-asciidoctorj-output-formatter-attrs-ctor[]
        // tag::hacking-asciidoctorj-output-formatter[]
    }
    // end::hacking-asciidoctorj-output-formatter[]

    /**
     * The path to a directory that contains frontmatter files.
     *
     * @param dir Anything convertible to a file.
     */
    @Override
    // tag::hacking-asciidoctorj-output-formatter-attrs[]
    void setFrontmatterDir(Object dir) {
        ccso.fsOperations().updateDirectoryProperty(this.frontmatterDir, dir) // <.>
    }
    // end::hacking-asciidoctorj-output-formatter-attrs[]

    /**
     * The path to a directory that contains alternate {@code}epub3.css} and {@code epub3-css3-only.css} files to
     * customise the look and feel.
     *
     * @param dir Anything convertible to a file.
     */
    @Override
    // tag::hacking-asciidoctorj-output-formatter-attrs[]
    void setStylesDir(Object dir) {
        ccso.fsOperations().updateDirectoryProperty(this.stylesDir, dir)
    }
    // end::hacking-asciidoctorj-output-formatter-attrs[]

    /**
     * Set the chapter level.
     *
     * @param level Chapter level 1-5
     */
    // tag::hacking-asciidoctorj-output-formatter-attrs[]
    @Override
    void setChapterLevel(int level) {
        if(level < 1 || level > 5) {
            throw new IncorrectOutputFormatException('The level can only be set between 1-5 (inclusive)')
        }
        this.level.set(level)
    }
    // end::hacking-asciidoctorj-output-formatter-attrs[]

    // tag::hacking-asciidoctorj-output-formatter-attrs[]
    @Override
    void configureTaskInputs(TaskInputs taskInputs) { // <.>
        taskInputs.property('chapter-level', this.level).optional(true)
        taskInputs.dir(this.stylesDir).optional(true).withPathSensitivity(PathSensitivity.RELATIVE)
        taskInputs.dir(this.frontmatterDir).optional(true).withPathSensitivity(PathSensitivity.RELATIVE)
    }
    // end::hacking-asciidoctorj-output-formatter-attrs[]

    @Override
    protected final Class<?> getDslType() {
        AsciidoctorjEpub // <.>
    }
// tag::hacking-asciidoctorj-output-formatter[]
}
// end::hacking-asciidoctorj-output-formatter[]

//epub-properties
//
//
//An optional override of the properties attribute for this document’s item in the manifest. Only applies to a chapter document.
//
//        epub-chapter-level
//
//
//Specify the section level at which to split the EPUB into separate "chapter" files. This attribute only affects documents with :doctype: book. The default is to split into chapters at level-1 sections. This attribute only affects the internal composition of the EPUB, not the way chapters and sections are displayed to users. Some readers may be slow if the chapter files are too large, so for large documents with few level-1 headings, one might want to use a chapter level of 2 or 3.
//
//        series-name, series-volume, series-id
//
//
//Populates the series statements (belongs-to-collection) in the package metadata. Volume is a number, ID probably a UUID that is constant for all volumes in the series.
//
//epub3-frontmatterdir
//
//
//The path to a directory that contains frontmatter files. The file names must match front-matter*.html and will be included in alphabetic order. The files are expected to be valid EPUB HTML files. If only one front matter page is required, the default 'front-matter.html' file can be used instead.
//
//        epub3-stylesdir
//
//
//The path to a directory that contains alternate epub3.css and epub3-css3-only.css files to customize the look and feel.