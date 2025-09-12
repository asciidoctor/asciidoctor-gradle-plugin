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
package org.asciidoctor.gradle.model5.core.internal.publications

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.publications.AsciidoctorDocInfo
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskInputs
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations
import org.ysb33r.grolifant5.api.core.FileSystemOperations

import javax.inject.Inject

import static org.ysb33r.grolifant5.api.core.StringTools.COMMA

/**
 * Implementation of {@link AsciidoctorDocInfo}.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class DefaultDocInfo implements AsciidoctorDocInfo {

    final Provider<Map<String, Object>> attributeProvider
    private final Property<Boolean> head
    private final Property<Boolean> header
    private final Property<Boolean> footer
    private final DirectoryProperty docInfoDir
    private final FileSystemOperations fsOperations

    @Inject
    DefaultDocInfo(Project project) {
        final ccso = ConfigCacheSafeOperations.from(project)
        this.head = project.objects.property(Boolean)
        this.header = project.objects.property(Boolean)
        this.footer = project.objects.property(Boolean)
        this.docInfoDir = project.objects.directoryProperty()
        this.fsOperations = ccso.fsOperations()

        final docInfo = project.provider { ->
            final mapping = [
                head.map { it ? 'private-head' : 'shared-head' }.getOrNull(),
                header.map { it ? 'private-header' : 'shared-header' }.getOrNull(),
                footer.map { it ? 'private-footer' : 'shared-footer' }.getOrNull()
            ].findAll { it }
            if (mapping) {
                mapping.join(COMMA)
            } else {
                (String) null
            }
        }

        this.attributeProvider = ccso.stringTools().provideValuesDropNull([
            docinfodir: docInfoDir.map { it.asFile.absolutePath },
            docinfo   : docInfo
        ]) as Provider<Map<String,Object>>
    }

    /**
     * Setting this will apply to all source files in the source set.
     * It can still be overridden in a file.
     *
     * @param flag {@code true} to have private docinfo files.
     */
    @Override
    void setHeadIsPrivate(boolean flag) {
        this.head.set(flag)
    }

    /**
     * Setting this will apply to all source files in the source set.
     * It can still be overridden in a file.
     *
     * @param flag {@code true} to have private docinfo files.
     */
    @Override
    void setHeaderIsPrivate(boolean flag) {
        this.header.set(flag)
    }

    /**
     * Setting this will apply to all source files in the source set.
     * It can still be overridden in a file.
     *
     * @param flag {@code true} to have private docinfo files.
     */
    @Override
    void setFooterIsPrivate(boolean flag) {
        this.footer.set(flag)
    }

    /**
     * Fix a specific directory to contain docinfo content.
     *
     * <p>
     *     Normally base dir determines docinfo discovery.
     *     If this is set, the base dir will be ignored.
     * </p>
     * @param dir Anything lazy-evaluatable to a directory.
     */
    @Override
    void setDocInfoDir(Object dir) {
        fsOperations.updateDirectoryProperty(this.docInfoDir, dir)
    }

    @Override
    void configureTaskInputs(TaskInputs taskInputs) {
        taskInputs.files( docInfoDir.map {
            fsOperations.fileTree(it).matching {
                include('*-docinfo.*')
                include('*-docinfo-*.*')
                include('docinfo.*')
                include('docinfo-*.*')
            }.files
        } ).optional()
    }
}
