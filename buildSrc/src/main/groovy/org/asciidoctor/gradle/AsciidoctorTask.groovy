package org.asciidoctor.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

import javax.script.Bindings
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager
import javax.script.SimpleBindings

class AsciidoctorTask extends DefaultTask {
    @Input File sourceDir
    @OutputDirectory File outputDir
    @Input String backend

    AsciidoctorTask() {
        sourceDir = project.file("src/asciidoc")
        outputDir = new File("${project.buildDir}/asciidoc")
        backend = 'html5'
    }

    @TaskAction
    void gititdone() {
        final ScriptEngineManager engineManager = new ScriptEngineManager();
        final ScriptEngine rubyEngine = engineManager.getEngineByName("jruby");
        final Bindings bindings = new SimpleBindings();

        bindings.put("srcDir", sourceDir.getAbsolutePath());
        bindings.put("outputDir", outputDir.getAbsolutePath());
        bindings.put("backend", backend);

        try {
            final InputStream script = AsciidoctorTask.class.getClassLoader().getResourceAsStream("execute_asciidoctor.rb");
            final InputStreamReader streamReader = new InputStreamReader(script);
            rubyEngine.eval(streamReader, bindings);
        } catch (ScriptException e) {
            project.getLogger().error("Error running ruby script", e);
        }
    }
}
