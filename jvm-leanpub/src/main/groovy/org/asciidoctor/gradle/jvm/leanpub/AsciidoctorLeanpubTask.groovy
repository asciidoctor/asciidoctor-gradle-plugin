/*
 * Copyright 2013-2020 the original author or authors.
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
package org.asciidoctor.gradle.jvm.leanpub

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.jvm.AbstractAsciidoctorTask
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.workers.WorkerExecutor

import javax.inject.Inject

/**
 * @author Schalk W. Cronjé
 *
 * @since 3.0.0
 */
@CompileStatic
@CacheableTask
class AsciidoctorLeanpubTask extends AbstractAsciidoctorTask {

    public static final String DISCUSSION = 'discussion'
    public static final String PARAGRAPH = 'paragraph'
    public static final String ASIDE = 'aside'

    public static final boolean MARKDOWN = false
    public static final boolean MARKUVA = true

    private static final String BACKEND = 'leanpub'
    private String colistStyle = ASIDE
    private String colistPrefix = 'line'

    @Inject
    AsciidoctorLeanpubTask(WorkerExecutor we) {
        super(we)

        configuredOutputOptions.backends = [BACKEND]
        copyNoResources()
        asciidoctorj.options.put('doctype', 'book')
    }

    /** The style used to format colists.
     *
     * @return Colist style
     */
    @Input
    String getColistStyle() {
        this.colistStyle
    }

    String setColistStyle(String val) {
        this.colistStyle = val
    }

    @Input
    String getColistPrefix() {
        this.colistPrefix
    }

    void setColistPrefix(String val) {
        this.colistPrefix = val
    }

    /** A task may add some default attributes.
     *
     * If the user specifies any of the attributes, then these attributes will not be utilised.
     *
     * The default implementation will add {@code includedir}
     *
     * @param workingSourceDir Directory where source files are located.
     *
     * @return A collection of default attributes.
     */
    @Override
    protected Map<String, Object> getTaskSpecificDefaultAttributes(File workingSourceDir) {
        Map<String,Object> attrs = super.getTaskSpecificDefaultAttributes(workingSourceDir)
//        attrs.put 'front-cover-image', getFrontCoverImage()
        attrs.put 'leanpub-colist-style', getColistStyle()
        attrs.put 'leanpub-colist-prefix', getColistPrefix()
        attrs
    }
}
