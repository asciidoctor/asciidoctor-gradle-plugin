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
package org.asciidoctor.gradle.kindlegen

import groovy.transform.CompileStatic
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.ysb33r.grolifant.api.exec.AbstractToolExtension
import org.ysb33r.grolifant.api.exec.NamedResolvedExecutableFactory
import org.ysb33r.grolifant.api.exec.ResolvableExecutable
import org.ysb33r.grolifant.api.exec.ResolveExecutableByVersion
import org.ysb33r.grolifant.api.os.Linux
import org.ysb33r.grolifant.api.os.MacOsX
import org.ysb33r.grolifant.api.os.Windows

import static org.asciidoctor.gradle.kindlegen.KindleGenDownloader.KINDLEGEN_BASE

/** Extension for configuring the path to {@code kindlegen} or a version which will then
 * be bootstrapped by Gradle.
 *
 * @author Schalk W. Cronjé
 *
 * @since 2.0.0
 */
@CompileStatic
class KindleGenExtension extends AbstractToolExtension {

    public final static String NAME = 'kindlegen'
    public final static String DEFAULT_KINDLEGEN_VERSION = '2_9'

    KindleGenExtension(Project project) {
        super(project)
        resolverFactoryRegistry.registerExecutableKeyActions(resolverFactory(project))
        executable version: DEFAULT_KINDLEGEN_VERSION
    }

    KindleGenExtension(Task task) {
        super(task, NAME)
        resolverFactoryRegistry.registerExecutableKeyActions(resolverFactory(task.project))
    }

    /** Explicitly configure to agree to Amazon Terms of Usage
     *
     * @sa {@link https://www.amazon.com/gp/feature.html?docId=1000599251}
     *
     */
    boolean agreeToTermsOfUse = false

    /** Obtain a lazy-evaluated object to resolve a path to an executable.
     *
     * @return An object for which will resolve a path on-demand.
     */
    @Override
    ResolvableExecutable getResolvableExecutable() {
        if (!agreeToTermsOfUse) {
            throw new GradleException(
                'You need to agree to Amazon\'s terms of usage for KindleGen. Set kindlegen.agreeToTermsOfUse=true. ' +
                    'For more details on the ToU see https://www.amazon.com/gp/feature.html?docId=1000599251'
            )
        }
        super.resolvableExecutable
    }

    private static NamedResolvedExecutableFactory resolverFactory(Project project) {
        new ResolveExecutableByVersion<KindleGenDownloader>(
            project, { Map<String, Object> options, String version, Project p ->
            new KindleGenDownloader(version, project)
        } as ResolveExecutableByVersion.DownloaderFactory,
            { KindleGenDownloader downloader ->
                switch (KindleGenDownloader.OS) {
                    case Windows:
                        return new File(downloader.distributionRoot, "${KINDLEGEN_BASE}.exe")
                    case Linux:
                    case MacOsX:
                        return new File(downloader.distributionRoot, KINDLEGEN_BASE)
                    default:
                        null
                }
            } as ResolveExecutableByVersion.DownloadedExecutable)
    }
}
