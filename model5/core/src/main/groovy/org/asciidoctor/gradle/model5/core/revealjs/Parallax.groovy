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
package org.asciidoctor.gradle.model5.core.revealjs

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.CanConfigureTaskInputs
import org.asciidoctor.gradle.model5.core.attributes.HasAttributeProvider
import org.gradle.api.Project
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskInputs
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations
import org.ysb33r.grolifant5.api.core.StringTools

import javax.inject.Inject

/**
 * Configures Parallax options.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class Parallax implements HasAttributeProvider, CanConfigureTaskInputs {

    final Provider<Map<String, Object>> attributeProvider
    final FileOrUri backgroundImage
    private final MapProperty<String, Object> attrs
    private final StringTools stringTools
    private final Property<String> size
    private final Property<Integer> pixelsHorizontal
    private final Property<Integer> pixelsVertical

    @Inject
    Parallax(Project project) {
        final ccso = ConfigCacheSafeOperations.from(project)
        this.stringTools = ccso.stringTools()
        this.size = project.objects.property(String)
        this.pixelsHorizontal = project.objects.property(Integer)
        this.pixelsVertical = project.objects.property(Integer)
        this.backgroundImage = project.objects.newInstance(FileOrUri, 'revealjs_parallaxBackgroundImage')
        this.attrs = project.objects.mapProperty(String, Object)
        this.attributeProvider = this.attrs

        this.attrs.putAll(stringTools.provideValuesDropNull([
                revealjs_parallaxBackgroundSize      : this.size,
                revealjs_parallaxBackgroundHorizontal: this.pixelsHorizontal,
                revealjs_parallaxBackgroundVertical  : this.pixelsVertical
        ]))
        this.attrs.putAll(this.backgroundImage.attributeProvider)
    }

    /**
     * Background size in CSS syntax
     *
     * @param css Anything that can be converted to a string.
     */
    void setBackgroundSize(Object css) {
        stringTools.updateStringProperty(this.size, css)
    }

    /**
     * Number of pixels to move the parallax background per slide.
     *
     * <p>
     *     Calculated automatically unless specified.
     *     Set to 0 to disable movement along an axis.
     * </p>
     *
     * @param pixels Number of pixels.
     */
    void setBackgroundHorizontal(Integer pixels) {
        this.pixelsHorizontal.set(pixels)
    }

    /**
     * Number of pixels to move the parallax background per slide.
     *
     * <p>
     *     Calculated automatically unless specified.
     *     Set to 0 to disable movement along an axis.
     * </p>
     *
     * @param pixels Number of pixels.
     */
    void setBackgroundVertical(Integer pixels) {
        this.pixelsVertical.set(pixels)
    }

    @Override
    void configureTaskInputs(TaskInputs taskInputs) {
        this.backgroundImage.configureTaskInputs(taskInputs)
    }
}
