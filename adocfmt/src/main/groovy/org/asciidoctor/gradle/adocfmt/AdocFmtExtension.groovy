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
package org.asciidoctor.gradle.adocfmt

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.adocfmt.internal.AdocFmtDownloader
import org.gradle.api.Project
import org.ysb33r.grolifant5.api.core.downloader.ExecutableDownloader
import org.ysb33r.grolifant5.api.core.runnable.AbstractNativeToolchain
import org.ysb33r.grolifant5.api.errors.ConfigurationException

/**
 * A simple extension to bootstrap {@code adocfmt}.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.2
 */
@CompileStatic
class AdocFmtExtension extends AbstractNativeToolchain {
    public static final String NAME = 'adocfmt'
    private static final String VERSION_KEY = NAME

    private final AdocFmtDownloader downloader

    AdocFmtExtension(Project project) {
        super(project)
        this.downloader = new AdocFmtDownloader(project)

        if (AdocFmtDownloader.DOWNLOAD_SUPPORTED) {
            final ver = ccso.fsOperations().loadPropertiesFromResource(
                'META-INF/asciidoctor.gradle/asciidoctor5-adocfmt-plugin.properties',
                this.class.classLoader
            )[VERSION_KEY].toString()

            executableByVersion(ver)
        }
    }

    @Override
    protected ExecutableDownloader getDownloader() {
        this.downloader
    }

    @Override
    protected String runExecutableAndReturnVersion(File exe) throws ConfigurationException {
        ccso.execTools().parseVersionFromOutput(['--version'], executable.get()) {
            it.readLines().first().trim()
        }
    }
}
