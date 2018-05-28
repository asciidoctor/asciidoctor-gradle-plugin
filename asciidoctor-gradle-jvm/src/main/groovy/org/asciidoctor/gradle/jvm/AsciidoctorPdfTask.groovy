/*
 * Copyright 2013-2018 the original author or authors.
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
package org.asciidoctor.gradle.jvm

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.internal.AsciidoctorUtils
import org.gradle.api.tasks.util.PatternSet
import org.gradle.util.GradleVersion
import org.gradle.workers.WorkerExecutor

import javax.inject.Inject

/**
 * @since 2.0.0
 * @author Schalk W. Cronjé
 */
@CompileStatic
class AsciidoctorPdfTask extends AbstractAsciidoctorTask {

    @Inject
    AsciidoctorPdfTask(WorkerExecutor we) {
        super(we)

        configuredOutputOptions.backends = ['pdf']
        copyNoResources()

    }

    /** Selects a final process mode of PDF processing.
     *
     * If the system is running on Windows with a GRadle version which wtill has classpath leakage problems
     * it will switch to using {@link #JAVA_EXEC}.
     *
     * @return Process mode to use for execution.
     */
    @Override
    protected ProcessMode getFinalProcessMode() {
        if(GradleVersion.current() <= LAST_GRADLE_WITH_CLASSPATH_LEAKAGE && AsciidoctorUtils.OS.windows) {
            if(inProcess != JAVA_EXEC) {
                logger.warn 'PDF processing on this version of Gradle combined with running on Microsoft Windows will fail due to classpath issues. Switching to JAVA_EXEC instead.'
            }
            JAVA_EXEC
        } else {
            super.finalProcessMode
        }
    }

    /** The default pattern set for secondary sources.
     *
     * @return {@link #getDefaultSourceDocumentPattern} + `*docinfo*`.
     */
    @Override
    protected PatternSet getDefaultSecondarySourceDocumentPattern() {
        PatternSet ps = defaultSourceDocumentPattern
        ps.include '*-theme.ym*l'
        ps
    }
}
