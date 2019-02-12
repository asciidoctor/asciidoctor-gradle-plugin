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
package org.asciidoctor.gradle.jvm.gems

import com.github.jrubygradle.GemUtils
import com.github.jrubygradle.JRubyPrepare
import com.github.jrubygradle.internal.JRubyExecUtils
import groovy.transform.CompileStatic
import org.asciidoctor.gradle.jvm.AsciidoctorJExtension
import org.gradle.api.artifacts.Configuration

/** Prepare additional GEMs for AsciidoctorJ.
 *
 * @since 2.0
 */
@CompileStatic
class AsciidoctorGemPrepare extends JRubyPrepare {

    @Override
    @SuppressWarnings('Instanceof')
    void copy() {
        File jruby = JRubyExecUtils.jrubyJar(project.extensions.getByType(AsciidoctorJExtension).configuration)
        GemUtils.extractGems(project, jruby, GemUtils.getGems(project.files(dependencies)), outputDir, GemUtils.OverwriteAction.SKIP)

        if (!dependencies.isEmpty()) {
            dependencies.each {
                if (it instanceof Configuration) {
                    GemUtils.setupJars(it, outputDir, GemUtils.OverwriteAction.SKIP)
                }
            }
        }

    }
}
