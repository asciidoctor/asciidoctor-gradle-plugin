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
package org.asciidoctor.gradle.jvm.gems

import com.github.jrubygradle.api.core.AbstractJRubyPrepare
import groovy.transform.CompileStatic
import org.asciidoctor.gradle.jvm.AsciidoctorJExtension
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.CacheableTask

import java.util.concurrent.Callable

import static com.github.jrubygradle.api.gems.GemUtils.JRUBY_ARCHIVE_NAME

/** Prepare additional GEMs for AsciidoctorJ.
 *
 * @since 2.0
 */
@CacheableTask
@CompileStatic
class AsciidoctorGemPrepare extends AbstractJRubyPrepare {

    /** Location of {@code jruby-complete} JAR.
     *
     * @return Path on local filesystem
     */
    @Override
    protected Provider<File> getJrubyJarLocation() {
        project.provider({ AsciidoctorJExtension jruby ->
            jruby.configuration.files.find { it.name.startsWith(JRUBY_ARCHIVE_NAME) }
        }.curry(jruby) as Callable<File>)
    }

    /** Version of JRuby that will be used if explicitly set
     *
     * This method does not resolve any files to obtain the version.
     *
     * @return Explicitly configured project global version of JRuby or
     * {@code null} if inferred from the asciidoctorj dependency.
     */
    @Override
    protected String getProposedJRubyVersion() {
        jruby.jrubyVersion
    }

    private AsciidoctorJExtension getJruby() {
        project.extensions.getByType(AsciidoctorJExtension)
    }
}
