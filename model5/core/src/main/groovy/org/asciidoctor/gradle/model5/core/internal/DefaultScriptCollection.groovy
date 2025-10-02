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
package org.asciidoctor.gradle.model5.core.internal

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.ScriptCollection
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.provider.SetProperty
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations

import javax.inject.Inject

/**
 * Implementation of {@link ScriptCollection}.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class DefaultScriptCollection implements ScriptCollection {

    final String scriptType
    private final ConfigCacheSafeOperations ccso
    private final SetProperty<String> scripts
    private final SetProperty<File> scriptFiles

    @Inject
    DefaultScriptCollection(String language, Project project) {
        this.ccso = ConfigCacheSafeOperations.from(project)
        this.scripts = project.objects.setProperty(String)
        this.scriptFiles = project.objects.setProperty(File)
        this.scriptType = language
    }

    void clear() {
        this.scripts.set([])
        this.scriptFiles.set([])
    }

    void addScript(Object script) {
        this.scripts.add(ccso.stringTools().provideString(script))
    }

    void addScriptFile(Object file) {
        this.scriptFiles.add(ccso.fsOperations().provideFile(file))
    }

    void addScripts(Provider<? extends Iterable<? extends String>> more) {
        this.scripts.addAll(more)
    }

    void addScriptFiles(Provider<? extends Iterable<? extends File>> more) {
        this.scriptFiles.addAll(more)
    }

    @Override
    Provider<Set<String>> getScripts() {
        this.scripts
    }

    @Override
    Provider<Set<File>> getScriptFiles() {
       this.scriptFiles
    }
}
