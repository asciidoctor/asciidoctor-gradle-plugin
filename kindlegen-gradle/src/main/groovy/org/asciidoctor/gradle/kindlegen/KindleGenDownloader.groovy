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
import org.ysb33r.grolifant.api.AbstractDistributionInstaller
import org.ysb33r.grolifant.api.OperatingSystem
import org.ysb33r.grolifant.api.errors.DistributionFailedException
import org.ysb33r.grolifant.api.os.Linux
import org.ysb33r.grolifant.api.os.MacOsX
import org.ysb33r.grolifant.api.os.Windows

/** Performs the hard work of downloading and unpacking the {@code kindlegen}
 * distribution.
 *
 * @since 2.0.0* @author Schalk W. Cronjé
 */
@CompileStatic
class KindleGenDownloader extends AbstractDistributionInstaller {

    public static final OperatingSystem OS = OperatingSystem.current()
    public static final String BASE_URI = System.getProperty(
        'org.asciidoctor.gradle.kindlegen.uri',
        'http://kindlegen.s3.amazonaws.com'
    )
    public static final String KINDLEGEN_BASE = 'kindlegen'

    KindleGenDownloader(String distributionVersion, Project project) {
        super(KINDLEGEN_BASE, distributionVersion, 'native-binaries', project)
    }

    /** Creates a download URI from a given distribution version
     *
     * @param version Version of the distribution to download
     * @return
     */
    @Override
    URI uriFromVersion(String version) {
        switch (OS) {
            case Windows:
                return "${BASE_URI}/kindlegen_win32_v${version}.zip".toURI()
            case Linux:
                return "${BASE_URI}/kindlegen_linux_2.6_i386_v${version}.tar.gz".toURI()
            case MacOsX:
                return "${BASE_URI}/KindleGen_Mac_i386_v${version}.zip".toURI()
            default:
                throw new GradleException('Kindlegen downloads are only supported on Windows, MacOS & Linux')
        }
    }

    /** Validates that the unpacked distribution is good.
     *
     * @param distDir Directory where distribution was unpacked to.
     * @param distributionDescription A descriptive name of the distribution
     * @return The directory where the real distribution now exists. In the default implementation it will be
     *   the single directory that exists below {@code distDir}.
     *
     * @throw {@link org.ysb33r.grolifant.api.errors.DistributionFailedException} if distribution
     *   failed to meet criteria.
     */
    @Override
    protected File getAndVerifyDistributionRoot(File distDir, String distributionDescription) {
        if (!new File(distDir, kindleGenFileName)) {
            throw new DistributionFailedException("${kindleGenFileName} not found in ${distDir}")
        }
        distDir
    }

    private String getKindleGenFileName() {
        switch (OS) {
            case Windows:
                return "${KINDLEGEN_BASE}.exe"
            case Linux:
                return KINDLEGEN_BASE
            case MacOsX:
                return KINDLEGEN_BASE
        }
    }

}
