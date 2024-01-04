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
package org.asciidoctor.gradle.base;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import groovy.transform.CompileDynamic;
import org.asciidoctor.gradle.base.basedir.BaseDirIsNull;
import org.gradle.api.Action;
import org.gradle.api.file.CopySpec;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileTree;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.*;
import org.gradle.api.tasks.util.PatternSet;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * The standard methods all Asciidoctor conversion tasks will offer.
 *
 * @since 4.0
 *
 * @author Schalk W. Cronjé
 */
public interface AsciidoctorTaskFileOperations {

    /**
     * The name of the Asciidoctor engine implementation.
     *
     * @return Engine name.
     */
    @Internal
    String getEngineName();

    /**
    * Logs documents as they are converted
     *
     */
    @Console
    boolean getLogDocuments();

    /**
     * Whether to log documents as they are being converted.
     *
     * @param mode Set {@code true} in order to log documents.
     */
    void setLogDocuments(boolean mode);


    /**
     * Sets the new Asciidoctor parent source directory.
     *
     * @param f Any object convertible with {@code project.file}.
     */
    void setSourceDir(Object f);

    /**
     * Sets the new Asciidoctor parent source directory in a declarative style.
     *
     * @param f Any object convertible with {@code project.file}.
     */
    void sourceDir(Object f);

    /**
     * Returns the parent directory for Asciidoctor source.
     *
     * @return Location of source directory.
     */
    @Internal
    default File getSourceDir() {
        return getSourceDirProperty().getAsFile().get();
    }

    /**
     * Returns the parent directory for Asciidoctor source as a property object.
     *
     * @return Source directory as a property.
     */
    @Internal
    DirectoryProperty getSourceDirProperty();

    /**
     * Returns the current toplevel output directory
     *
     * @return Output directory.
     */
    @OutputDirectory
    default File getOutputDir() {
        return getOutputDirProperty().getAsFile().get();
    }

    /**
     * Sets the new Asciidoctor parent output directory.
     *
     * @param f An object convertible via {@code project.file}.
     */
    void setOutputDir(Object f);

    /**
     * Returns the current toplevel output directory as a property object.
     *
     * @return Output directory as a property.
     */
    @Internal
    DirectoryProperty getOutputDirProperty();

    /** Configures sources.
     *
     * @param cfg Configuration closure. Is passed a {@link PatternSet}.
     */
    void sources(@DelegatesTo(PatternSet.class) Closure<?> cfg);

    /** Configures sources.
     *
     * @param cfg Configuration {@link org.gradle.api.Action}. Is passed a {@link PatternSet}.
     */
    void sources(final Action<? super PatternSet> cfg);

    /**
     * Include source patterns.
     *
     * @param includePatterns ANT-style patterns for sources to include
     */
    void sources(String... includePatterns);

    /**
     * Clears existing sources patterns.
     */
    void clearSources();

    /**
     * Clears any of the existing secondary soruces patterns.
     *
     * This should be used if none of the default patterns should be monitored.
     */
    void clearSecondarySources();

    /** Configures secondary sources.
     *
     * @param cfg Configuration closure. Is passed a {@link PatternSet}.
     */
    void secondarySources(@DelegatesTo(PatternSet.class) Closure<?> cfg);

    /** Configures sources.
     *
     * @param cfg Configuration {@link Action}. Is passed a {@link PatternSet}.
     */
    void secondarySources(final Action<? super PatternSet> cfg);

    /**
     * A provider of patterns identifying intermediate artifacts.
     *
     * @return Provider to a {@link PatternSet}. Can be empty.
     */
    @Internal
    Provider<PatternSet> getIntermediateArtifactPatternProvider();

    /**
     * Returns the copy specification for the resources of a specific language.
     *
     * @param lang Language
     *
     * @return Copy specification. Can be {@code null}.
     */
    CopySpec getLanguageResourceCopySpec(String lang);

    /**
     * Gets the CopySpec for additional resources.
     *
     * If {@code resources} was never called, it will return a default CopySpec otherwise it will return the
     * one built up via successive calls to {@code resources}
     *
     * @param lang Language to to apply to or empty for no-language support.
     * @return A{@link CopySpec}. Never {@code null}.
     */
    CopySpec getResourceCopySpec(Optional<String> lang);

    /**
     * The default CopySpec that will be used if {@code resources} was never called
     *
     * By default, anything below {@code $sourceDir/images} will be included.
     *
     * @param lang Language to use. Can be empty (not {@code null}) when not to use a language.
     * @return A{@link CopySpec}. Never {@code null}.
     */
    CopySpec getDefaultResourceCopySpec(Optional<String> lang);

    /**
     * Returns a FileTree containing all source documents
     *
     * If a filter with {@link #sources} was never set then all asciidoc source files
     * below {@link #setSourceDir} will be included. If multiple languages are used, all
     *      * language secondary source sets are included.
     * @return Applicable source trees.
     */
    @Internal
    FileTree getSourceFileTree();

    /**
     * Returns a FileTree containing all secondary source documents.
     *
     * If a filter with {@link #secondarySources} was never set then all asciidoc source files
     * below {@link #setSourceDir} will be included. If multiple languages are used, all
     * language secondary source sets are included.
     *
     * @return Collection of secondary source files
     *
     */
    @Internal
    FileTree getSecondarySourceFileTree();

    /**
     * Add to the CopySpec for extra files. The destination of these files will always have a parent directory
     * of {@code outputDir} or {@code outputDir + backend}
     *
     * @param cfg {@link CopySpec} runConfiguration closure
     */
    void resources(Closure cfg);

    /**
     * Add to the CopySpec for extra files. The destination of these files will always have a parent directory
     * of {@code outputDir} or {@code outputDir + backend}
     *
     * @param cfg {@link CopySpec} runConfiguration {@link Action}
     */
    void resources(Action<? super CopySpec> cfg);

    /**
     * Add to the CopySpec for extra files. The destination of these files will always have a parent directory
     * of {@code outputDir} or {@code outputDir + backend}
     *
     * If not languages are set. these resources will be ignored.
     *
     * @param cfg {@link CopySpec} runConfiguration closure
     * @param lang Language to which these resources will be applied to.
     */
    void resources(final String lang, Closure cfg);

    /** Add to the CopySpec for extra files. The destination of these files will always have a parent directory
     * of {@code outputDir} or {@code outputDir + backend}
     *
     * If not languages are set. these resources will be ignored.
     *
     * @param cfg {@link CopySpec} runConfiguration {@link Action}
     * @param lang Language to which these resources will be applied to.
     * @since 3.0.0
     */
    void resources(final String lang, Action<? super CopySpec> cfg);

    /**
     * Some extensions such as {@code ditaa} creates images in the source directory.
     *
     * Use this setting to copy all sources and resources to an intermediate work directory
     * before processing starts. This will keep the source directory pristine
     */
    void useIntermediateWorkDir();

    /**
     * Checks whether an intermediate workdir is required.
     *
     * @return {@code true} is there is an intermediate working directory.
     */
    boolean hasIntermediateWorkDir();

    /**
     * The document conversion might generate additional artifacts that could
     * require copying to the final destination.
     *
     * An example is use of {@code ditaa} diagram blocks. These artifacts can be specified
     * in this block. Use of the option implies {@link #useIntermediateWorkDir}.
     *
     * @param cfg Configures a {@link PatternSet} with a base directory of the intermediate working
     * directory.
     */
    void withIntermediateArtifacts(@DelegatesTo(PatternSet.class) Closure cfg);

    /**
     * Additional artifacts created by Asciidoctor that might require copying.
     *
     * @param cfg Action that configures a {@link PatternSet}.
     *
     * @see {@link #withIntermediateArtifacts(Closure cfg)}
     */
    void withIntermediateArtifacts(final Action<PatternSet> cfg);

    /**
     * The directory that will be the intermediate directory should one be required.
     *
     * @return Intermediate working directory
     */
    @Internal
    default File getIntermediateWorkDir() {
        return getIntermediateWorkDirProvider().get();
    }

    @Internal
    Provider<File> getIntermediateWorkDirProvider();

    /**
     * Returns a list of all output directories by backend
     */
    @OutputDirectories
    Set<File> getBackendOutputDirectories();

    /**
     * Obtain List of languages the sources documents are written in.
     *
     * @return List of languages. Can be empty, but never {@code null}.
     */
    @Input
    List<String> getLanguages();

    /**
     * Reset current list of languages and replace with a new set.
     *
     * @param langs List of new languages
     */
    void setLanguages(Iterable<String> langs);

    /**
     * Add to list of languages to process.
     *
     * @param langs List of additional languages
     */
    void languages(Iterable<String> langs);

    /**
     * Add to list of languages to process.
     *
     * @param langs List of additional languages
     */
    void languages(String... langs);

    /**
     * A task may add some default attributes.
     * If the user specifies any of these attributes, then those attributes will not be utilised.
     * The default implementation will add {@code includedir}, {@code revnumber}, {@code gradle-project-group},
     * {@code gradle-project-name}
     *
     * @param workingSourceDir Directory where source files are located.
     *
     * @return A collection of default attributes.
     */
    Map<String, ?> getTaskSpecificDefaultAttributes(File workingSourceDir);
}
