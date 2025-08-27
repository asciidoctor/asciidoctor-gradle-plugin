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
package org.asciidoctor.gradle.model5.core.internal.basedir

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.waitingroom.BaseDirStrategy
import org.gradle.api.provider.Provider

/** Strategy where a lazy-evaluated fixed path is used as the base
 * directory for asciidoctor conversions.
 *
 * @author Schalk W. Cronjé
 *
 * @since 2.2.0
 */
@CompileStatic
class BaseDirIsFixedPath implements BaseDirStrategy {

    private final Provider<File> location

    BaseDirIsFixedPath(Provider<File> lazyResolvedLocation) {
        this.location = lazyResolvedLocation
    }

    @Override
    Provider<File> getBaseDir(Provider<File> srcDir) {
        this.location
    }

    @Override
    Provider<File> getBaseDir(Provider<File> srcDir, String lang) {
        this.location
    }
}
