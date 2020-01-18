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
package org.asciidoctor.gradle.jvm.leanpub

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.file.CopySpec
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.ysb33r.grolifant.api.StringUtils

/** Copies Leanpub Asciidoctor task output to a Dropbox folder.
 *
 * @author Schalk W. Cronjé
 *
 * @since 3.0.0
 */
@CompileStatic
class DropboxCopyTask extends DefaultTask {

    private Object dropboxRoot = "${System.getProperty('user.home')}/Dropbox"
    private Object bookPath
    private Object sourceDir

    @SuppressWarnings('UnnecessaryGetter')
    private final Action<CopySpec> copySpec = new Action<CopySpec>() {
        @Override
        void execute(CopySpec cs) {
            cs.into(destinationDir)
            cs.from(getSourceDir()).include('**')
        }
    }

    /** Root directory on filesystem where Dropbox synchronises all files.
     *
     * @return Path to Dropbox root on local filesystem.
     */
    @Internal
    File getDropboxRoot() {
        project.file(this.dropboxRoot)
    }

    /** Override location of Dropbox root
     *
     * @param f New location of Dropbox root
     */
    void setDropboxRoot(Object f) {
        this.dropboxRoot = f
    }

    /** The relative path to the book directory in the Dropbox folder.
     *
     * This is typically the relative path to the parent of the {@code manuscript} folder.
     *
     * @return A relative path. For instance {@code Leanpub/idiomaticgradle}.
     */
    @Input
    String getBookPath() {
        this.bookPath != null ? StringUtils.stringize(this.bookPath) : null
    }

    /** Sets the relative path of the Leanpub book in Dropbox.
     *
     * @param p Relative path of Leanpub as synchronised to Dropbox.
     */
    void setBookPath(Object p) {
        this.bookPath = p
    }

    /** Directory where source files will be read from.
     *
     * This is the directory where converted Leanpub files are written to. it is typically named {@code manuscript}.
     *
     * @return Directory containing generated Leanpub Markuva or Markdown content.
     */
    @InputDirectory
    File getSourceDir() {
        project.file(this.sourceDir)
    }

    void setSourceDir(Object s) {
        this.sourceDir = s
    }

    /** Directory where content will be copied to.
     *
     * @return Final output directory
     */
    @OutputDirectory
    File getDestinationDir() {
        project.file("${getDropboxRoot()}/${getBookPath()}/manuscript")
    }

    @TaskAction
    void copy() {
        project.copy(copySpec)
    }
}
