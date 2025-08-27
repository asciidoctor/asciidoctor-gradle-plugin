/*
 * Copyright ${year} the original author or authors.
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
package org.asciidoctor.gradle.model5.core.testfixtures.examples

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.waitingroom.BaseDirStrategy
import org.asciidoctor.gradle.model5.core.SafeMode
import org.asciidoctor.gradle.model5.core.tasks.AsciidoctorTaskMethods
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.util.PatternFilterable
import org.ysb33r.grolifant5.api.core.runnable.GrolifantDefaultTask

@CompileStatic
class ExampleAsciidoctorTask extends GrolifantDefaultTask implements AsciidoctorTaskMethods {
    @Override
    void setAttributes(Provider<Map<String, String>> attrs) {

    }

    @Override
    void setSafeMode(Provider<SafeMode> safeMode) {

    }

    @Override
    void setLogDocuments(Provider<Boolean> flag) {

    }

    @Override
    void setBaseDirStrategy(Provider<BaseDirStrategy> dir) {

    }

    @Override
    void setSourceDir(Provider<File> dir) {

    }

    @Override
    void setSourcePatterns(Provider<PatternFilterable> patterns) {

    }

    @Override
    void setSecondarySourcePatterns(Provider<PatternFilterable> patterns) {

    }

    @TaskAction
    void exec() {

    }
}
