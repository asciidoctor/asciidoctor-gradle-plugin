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
package org.asciidoctor.gradle.adocfmt.tasks

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.adocfmt.errors.FormatExecException
import org.gradle.api.tasks.CacheableTask
import org.ysb33r.grolifant5.api.core.runnable.ExecOutput

/**
 * Recursively formats a directory of AsciiDoc files.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.2
 */
@CompileStatic
@CacheableTask
class AdocFmtTask extends AdocFmtBaseTask {
    AdocFmtTask() {
        super('write')
    }

    @Override
    protected void handleResult(ExecOutput execResult) {
        final result = execResult.result.get()
        if (result.exitValue != 0) {
            throw new FormatExecException("Failed to execute 'adocfmt'. Exit code ${result.exitValue}")
        }
    }
}
