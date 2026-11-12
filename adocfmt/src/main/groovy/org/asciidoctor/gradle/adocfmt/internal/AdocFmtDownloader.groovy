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
package org.asciidoctor.gradle.adocfmt.internal

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations
import org.ysb33r.grolifant5.api.core.OperatingSystem
import org.ysb33r.grolifant5.api.core.downloader.AbstractDistributionInstaller
import org.ysb33r.grolifant5.api.core.downloader.ExecutableDownloader
import org.ysb33r.grolifant5.api.errors.DistributionFailedException

import static org.ysb33r.grolifant5.api.core.OperatingSystem.Arch.ARM64
import static org.ysb33r.grolifant5.api.core.OperatingSystem.Arch.X86_64
import static org.ysb33r.grolifant5.api.core.StringTools.EMPTY

/**
 * Bootstraps a specific version of {@code adocfmt}.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.2
 */
@CompileStatic
class AdocFmtDownloader extends AbstractDistributionInstaller implements ExecutableDownloader {
    public static final String DISTPATH = 'native-binaries/adocfmt'

    public static final String BASEURI = System.getProperty(
        'org.asciidoctor.gradle.adocfmt.releases.uri',
        'https://github.com/ypfaff/adocfmt/releases/download'
    )

    private static final OperatingSystem OS = OperatingSystem.current()
    private static final String EXE_NAME = "adocfmt${OS.windows ? '.exe' : EMPTY}"
    public static final boolean DOWNLOAD_SUPPORTED = (OS.windows || OS.linux || OS.macOsX) &&
        (OS.arch in [ARM64, X86_64])

    private final String archAndExtension

    AdocFmtDownloader(Project project) {
        super('Adocfmt distribution', DISTPATH, ConfigCacheSafeOperations.from(project))
        this.archAndExtension = "${osName}_${archName}.tar.gz"
    }

    @Override
    URI uriFromVersion(String version) {
        if (DOWNLOAD_SUPPORTED) {
            "${BASEURI}/v${version}/adocfmt_${version}_${archAndExtension}".toURI()
        } else {
            null
        }
    }

    @Override
    File getByVersion(String version) {
        getDistributionFile(version, EXE_NAME).get()
    }

    @Override
    protected File verifyDistributionRoot(File distDir) {
        if (new File(distDir, EXE_NAME).exists()) {
            distDir
        } else {
            throw new DistributionFailedException("${EXE_NAME} is missing from unpacked archive")
        }
    }

    private String getArchName() {
        switch (OS.arch) {
            case ARM64:
                return 'arm64'
            case X86_64:
                return 'amd64'
            default:
                null
        }
    }

    private String getOsName() {
        if (OS.windows) {
            'windows'
        } else if (OS.macOsX) {
            'darwin'
        } else if (OS.linux) {
            'linux'
        } else {
            null
        }
    }
}
