package org.asciidoctor.gradle.model5.core.internal.basedir

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.BaseDirStrategy
import org.gradle.api.provider.Provider

@CompileStatic
class BaseDirFollowSourceDir implements BaseDirStrategy{
    @Override
    Provider<File> getBaseDir(Provider<File> srcDir) {
        srcDir
    }

    @Override
    Provider<File> getBaseDir(Provider<File> srcDir, String lang) {
        srcDir
    }
}
