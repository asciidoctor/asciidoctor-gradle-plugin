/*
 * Copyright 2012-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.asciidoctor.gradle

import javax.script.Bindings
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager
import javax.script.SimpleBindings

/**
 * @author Noam Tenne
 * @author Andres Almiray
 */
class JRubyAsciidoctorWorker implements AsciidoctorWorker {
    static final String SCRIPT_ENGINE_NAME = 'jruby'
    static final String SCRIPT_NAME = 'execute_asciidoctor.rb'

    @Override
    void execute(File sourceDir, File outputDir, String backend) {
        ScriptEngine rubyEngine = createScriptEngine()
        Bindings bindings = createBindings(sourceDir, outputDir, backend)
        runRubyScript(rubyEngine, bindings)
    }

    private ScriptEngine createScriptEngine() {
        ScriptEngineManager engineManager = new ScriptEngineManager()
        engineManager.getEngineByName(SCRIPT_ENGINE_NAME)
    }

    private Bindings createBindings(File sourceDir, File outputDir, String backend) {
        Bindings bindings = new SimpleBindings()
        bindings.srcDir = sourceDir.absolutePath
        bindings.outputDir = outputDir.absolutePath
        bindings.backend = backend
        bindings
    }

    private void runRubyScript(ScriptEngine rubyEngine, Bindings bindings) {
        InputStream script = JRubyAsciidoctorWorker.class.classLoader.getResourceAsStream(SCRIPT_NAME)
        InputStreamReader streamReader = new InputStreamReader(script)
        rubyEngine.eval(streamReader, bindings)
    }
}
