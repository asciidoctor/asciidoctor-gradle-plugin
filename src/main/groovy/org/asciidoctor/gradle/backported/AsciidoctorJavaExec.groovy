/*
 * Copyright 2013-2018 the original author or authors.
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
package org.asciidoctor.gradle.backported

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.asciidoctor.Asciidoctor
import org.asciidoctor.Options
import org.asciidoctor.groovydsl.AsciidoctorExtensions

/** Runs Asciidoctor as an externally invoked Java process.
 *
 * @since 1.5.9 (Backported from 2.0)
 * @author Schalk W. Cronjé
 */
@CompileStatic
class AsciidoctorJavaExec {

    AsciidoctorJavaExec(ExecutorConfigurationContainer ecc) {
        this.runConfigurations = ecc.configurations
    }

    void run() {

        Thread.currentThread().contextClassLoader = this.class.classLoader
        Asciidoctor asciidoctor = asciidoctorInstance
        addRequires(asciidoctor)

        runConfigurations.each { runConfiguration ->
            if (runConfiguration.asciidoctorExtensions?.size()) {
                registerExtensions(asciidoctor, runConfiguration.asciidoctorExtensions)
            }
        }

        runConfigurations.each { runConfiguration ->
            runConfiguration.outputDir.mkdirs()
            convertFiles(asciidoctor, runConfiguration)
        }
    }

    @SuppressWarnings(['Println', 'CatchThrowable'])
    private void convertFiles(Asciidoctor asciidoctor, ExecutorConfiguration runConfiguration) {
        runConfiguration.sourceTree.each { File file ->
            try {
                if (runConfiguration.logDocuments) {
                    println("Converting ${file}")
                }
                asciidoctor.convertFile(file, normalisedOptionsFor(file, runConfiguration))
            } catch (Throwable t) {
                throw new AsciidoctorRemoteExecutionException("Error running Asciidoctor whilst attempting to process ${file} using backend ${runConfiguration.backendName}", t)
            }
        }
    }

    private Asciidoctor getAsciidoctorInstance() {
        String combinedGemPath = runConfigurations*.gemPath.join(File.pathSeparator)
        if (combinedGemPath.empty || combinedGemPath == File.pathSeparator) {
            Asciidoctor.Factory.create()
        } else {
            Asciidoctor.Factory.create(combinedGemPath)
        }
    }

    private void addRequires(Asciidoctor asciidoctor) {
        runConfigurations.each { runConfiguration ->
            for (require in runConfiguration.requires) {
                asciidoctor.requireLibrary(require)
            }
        }
    }

    @CompileDynamic
    private void registerExtensions(Asciidoctor asciidoctor, List<Object> exts) {

        AsciidoctorExtensions extensionRegistry = new AsciidoctorExtensions()

        for (Object ext in rehydrateExtensions(extensionRegistry, exts)) {
            extensionRegistry.addExtension(ext)
        }
        extensionRegistry.registerExtensionsWith((Asciidoctor) asciidoctor)
    }

    /** Normalises Asciidoctor options for a given source file.
     *
     * Relativizes certain attributes and ensure specific options for backend, sage mode and output
     * directory are in place.
     *
     * @param file Source file to be converted
     * @param runConfiguration The current executor configuration
     * @return Asciidoctor options
     */
    @SuppressWarnings(['Instanceof','Println','DuplicateStringLiteral'])
    protected
    Map<String, Object> normalisedOptionsFor(final File file, ExecutorConfiguration runConfiguration) {

        Map<String, Object> mergedOptions = [:]

        runConfiguration.with {
            final String srcRelative = getRelativePath(file.parentFile, sourceDir)

            mergedOptions.putAll(options)
            mergedOptions.putAll([
                (Options.BACKEND) : backendName,
                (Options.IN_PLACE): false,
                (Options.SAFE)    : safeModeLevel,
                (Options.TO_DIR)  : (srcRelative.empty ? outputDir : new File(outputDir, srcRelative)).absolutePath,
                (Options.MKDIRS)  : true
            ])

            mergedOptions[Options.BASEDIR] = (baseDir ?: file.parentFile).absolutePath

            if (mergedOptions.containsKey(Options.TO_FILE)) {
                Object toFileValue = mergedOptions[Options.TO_FILE]
                Object toDirValue = mergedOptions.remove(Options.TO_DIR)
                File toFile = toFileValue instanceof File ? (File) toFileValue : new File(toFileValue.toString())
                File toDir = toDirValue instanceof File ? (File) toDirValue : new File(toDirValue.toString())
                mergedOptions[Options.TO_FILE] = new File(toDir, toFile.name).absolutePath
            }

            // Note: Directories passed as relative to work around issue #83
            // Asciidoctor cannot handle absolute paths in Windows properly
            Map<String, Object> newAttrs = [:]
            newAttrs.putAll(attributes)
            newAttrs.projectdir = getRelativePath(projectDir, file.parentFile)
            newAttrs.rootdir = getRelativePath(rootDir, file.parentFile)

            newAttrs['gradle-projectdir'] = getRelativePath(projectDir, file.parentFile)
            newAttrs['gradle-rootdir'] = getRelativePath(rootDir, file.parentFile)
            println 'Implicit attributes projectdir and rootdir are deprecated and will no longer be set in 2.0. Please migrate your documents to use gradle-projectdir and gradle-rootdir instead.'
            mergedOptions[Options.ATTRIBUTES] = newAttrs
        }

        mergedOptions
    }

    /**
     * Returns the path of one File relative to another.
     *
     * @param target the target directory
     * @param base the base directory
     * @return target's path relative to the base directory
     * @throws IOException if an error occurs while resolving the files' canonical names
     */
    protected String getRelativePath(File target, File base) throws IOException {
        base.toPath().relativize(target.toPath()).toFile().toString()
    }

    /** Rehydrates extensions that were serialised.
     *
     * @param registry Asciidoctor GroovyDSL registry instance.
     * @param exts List of extensions to rehydrate.
     * @return
     */
    protected List<Object> rehydrateExtensions(final Object registry, final List<Object> exts) {
        exts.collect {
            switch (it) {
                case Closure:
                    Closure rehydrated = ((Closure) it).rehydrate(registry, null, null)
                    rehydrated.resolveStrategy = Closure.DELEGATE_ONLY
                    (Object) rehydrated
                    break
                default:
                    it
            }
        } as List<Object>
    }

    private final List<ExecutorConfiguration> runConfigurations

    static void main(String[] args) {
        if (args.size() != 1) {
            throw new AsciidoctorRemoteExecutionException('No serialised location specified')
        }

        ExecutorConfigurationContainer ecc
        new File(args[0]).withInputStream { input ->
            new ObjectInputStream(input).withCloseable { ois ->
                ecc = (ExecutorConfigurationContainer) ois.readObject()
            }
        }

        new AsciidoctorJavaExec(ecc).run()
    }

}
