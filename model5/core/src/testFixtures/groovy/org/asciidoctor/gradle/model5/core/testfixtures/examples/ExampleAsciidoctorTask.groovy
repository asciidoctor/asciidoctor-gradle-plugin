package org.asciidoctor.gradle.model5.core.testfixtures.examples

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.BaseDirStrategy
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
