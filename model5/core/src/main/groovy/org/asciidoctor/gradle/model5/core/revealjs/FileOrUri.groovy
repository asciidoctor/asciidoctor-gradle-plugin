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
package org.asciidoctor.gradle.model5.core.revealjs

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.CanConfigureTaskInputs
import org.asciidoctor.gradle.model5.core.attributes.HasAttributeProvider
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskInputs
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations
import org.ysb33r.grolifant5.api.core.FileSystemOperations
import org.ysb33r.grolifant5.api.core.StringTools

import javax.inject.Inject

/**
 * Re-usable component for where a lcoation, URI or local relative path can be set.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class FileOrUri implements HasAttributeProvider, CanConfigureTaskInputs {

    final Provider<Map<String, Object>> attributeProvider
    private final Property<String> location
    private final Property<Boolean> isFile
    private final StringTools stringTools
    private final FileSystemOperations fsOperations

    @Inject
    FileOrUri(String attrName, Project project) {
        final ccso = ConfigCacheSafeOperations.from(project)
        this.stringTools = ccso.stringTools()
        this.fsOperations = ccso.fsOperations()
        this.location = project.objects.property(String)
        this.isFile = project.objects.property(Boolean).convention(false)
        this.attributeProvider = stringTools.provideValuesDropNull([
                (attrName): this.location
        ]) as Provider<Map<String,Object>>
    }

    /**
     * Set location as URI.
     *
     * <p>
     *     This will unset any call to {@link #setLocation(Object)} or {@link #setRelativePath(Object)}.
     * </p>
     *
     * @param uriThingy Anything convertible to a URI.
     */
    void setUri(Object uriThingy) {
        this.location.set(stringTools.provideUri(uriThingy).map { stringTools.stringize(it)})
        this.isFile.set(false)
    }

    /**
     * Set location from local file system.
     *
     * <p>
     *     This will unset any call to {@link #setUri(Object)} or {@link #setRelativePath(Object)}.
     * </p>
     *
     * @param fileThingy Anything convertible to a file.
     */
    void setLocation(Object fileThingy) {
        this.location.set(fsOperations.provideFile(fileThingy).map { it.absolutePath})
        this.isFile.set(true)
    }

    /**
     * Set as a relative path on the file space.
     *
     * <p>
     *     This will unset any call to {@link #setUri(Object)} or {@link #setLocation(Object)}.
     * </p>
     *
     * @param strThingy Anything convertible to a string.
     */
    void setRelativePath(Object strThingy) {
        this.location.set(stringTools.provideString(strThingy))
        this.isFile.set(false)
    }

    @Override
    void configureTaskInputs(TaskInputs taskInputs) {
        taskInputs.files(location.zip(isFile) { loc, flag -> flag ? loc : null}).optional(true)
    }
}
