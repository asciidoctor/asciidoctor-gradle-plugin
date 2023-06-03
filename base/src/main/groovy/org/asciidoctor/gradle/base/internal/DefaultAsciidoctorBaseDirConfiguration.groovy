/*
 * Copyright 2013-2024 the original author or authors.
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
package org.asciidoctor.gradle.base.internal

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.base.AsciidoctorTaskBaseDirConfiguration
import org.asciidoctor.gradle.base.AsciidoctorTaskFileOperations
import org.asciidoctor.gradle.base.BaseDirStrategy
import org.asciidoctor.gradle.base.basedir.BaseDirFollowsProject
import org.asciidoctor.gradle.base.basedir.BaseDirIsFixedPath
import org.asciidoctor.gradle.base.basedir.BaseDirIsNull
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.ysb33r.grolifant.api.core.ProjectOperations

import javax.annotation.Nullable
import java.util.concurrent.Callable

/**
 * The default implementation for base directory copnfiguration.
 *
 * @author Schalk W. Cronjé
 *
 * @since 4.0
 */
@CompileStatic
class DefaultAsciidoctorBaseDirConfiguration implements AsciidoctorTaskBaseDirConfiguration {
    private BaseDirStrategy baseDirStrategy
    private final Property<File> baseDirProperty
    private final ProjectOperations po
    private final AsciidoctorTaskFileOperations fileOperations

    DefaultAsciidoctorBaseDirConfiguration(Project project, AsciidoctorTaskFileOperations atfo) {
        this.po = ProjectOperations.find(project)
        this.fileOperations = atfo
        this.baseDirProperty = project.objects.property(File)
        this.baseDirProperty.set(project.provider { ->
            owner.baseDirStrategy ? owner.baseDirStrategy.baseDir : owner.po.projectDir
        })
    }

    /**
     * The base dir will be the same as the source directory.
     * <p>
     * If an intermediate working directory is used, the the base dir will be where the
     * source directory is located within the temporary working directory.
     * </p>
     */
    @Override
    void baseDirFollowsSourceDir() {
        this.baseDirStrategy = new BaseDirIsFixedPath(po.provider({ AsciidoctorTaskFileOperations task ->
            task.hasIntermediateWorkDir() ? task.intermediateWorkDir : task.sourceDir
        }.curry(fileOperations) as Callable<File>))
    }

    /**
     * Sets the basedir to be the same directory as each individual source file.
     */
    @Override
    void baseDirFollowsSourceFile() {
        this.baseDirStrategy = BaseDirIsNull.INSTANCE
    }

    /**
     * Sets the basedir to be the same directory as the current project directory.
     *
     * @since 2.2.0
     */
    @Override
    void baseDirIsProjectDir() {
        this.baseDirStrategy = new BaseDirFollowsProject(po.projectDir)
    }

    /**
     * Sets the basedir to be the same directory as the root project directory.
     */
    @Override
    void baseDirIsRootProjectDir() {
        this.baseDirStrategy = new BaseDirFollowsProject(po.projectRootDir)
    }

    /**
     * Base directory (current working directory) for a conversion.
     *
     * Depending on the strateggy in use, the source language used in the conversion
     * may change the final base directory relative to the value returned by {@link #getBaseDir}.
     *
     * @param lang Language in use
     * @return Language-dependent base directory
     */
    @Override
    File getBaseDir(String lang) {
        this.baseDirStrategy ? this.baseDirStrategy.getBaseDir(lang) : po.projectDir
    }

    @Override
    Provider<File> getBaseDirProvider() {
        this.baseDirProperty
    }

    /**
     * Returns the current basedir strategy if it has been configured.
     *
     * @return Strategy or @{code null}.
     */
    @Override
    BaseDirStrategy getBaseDirStrategy() {
        this.baseDirStrategy
    }

    /**
     * Checks whether an explicit strategy has been set for base directory.
     *
     * @return {@code true} if a strategy has been configured.
     */
    @Override
    boolean isBaseDirConfigured() {
        baseDirStrategy != null
    }

    /**
     * Sets the base directory for a conversion.
     * <p>
     * The base directory is used by AsciidoctorJ to set a current working directory for
     * a conversion. If never set, then {@code project.projectDir} will be assumed to be the base directory.
     * </p>
     * @param f Can be a {@link BaseDirStrategy}, {@code null}, or anything convertible to a file.
     */
    @Override
    void setBaseDir(@Nullable Object f) {
        switch (f) {
            case BaseDirStrategy:
                this.baseDirStrategy = (BaseDirStrategy) f
                break
            case null:
                this.baseDirStrategy = BaseDirIsNull.INSTANCE
                break
            default:
                this.baseDirStrategy = new BaseDirIsFixedPath(po.provider({
                    po.fsOperations.file(f)
                } as Callable<File>))
        }
    }
}
