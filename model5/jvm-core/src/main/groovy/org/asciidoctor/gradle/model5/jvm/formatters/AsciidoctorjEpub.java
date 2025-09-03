/**
 * Copyright 2013 - 2025 the original author or authors.
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
// tag::hacking-asciidoctorj-output-formatter[]
package org.asciidoctor.gradle.model5.jvm.formatters;

// end::hacking-asciidoctorj-output-formatter[]

/**
 * The {@code asciidoctorj-epub} output formatter.
 *
 * @author Schalk W. Cronjé
 * @since 5.0
 */
// tag::hacking-asciidoctorj-output-formatter[]
// tag::hacking-asciidoctorj-output-formatter-attrs[]
public interface AsciidoctorjEpub extends AsciidoctorjOutputFormatterVersioned { // <.>
// end::hacking-asciidoctorj-output-formatter[]

    // end::hacking-asciidoctorj-output-formatter-attrs[]
    /**
     * Set the chapter level.
     *
     * @param level Chapter level 1-5
     */
    // tag::hacking-asciidoctorj-output-formatter-attrs[]
    void setChapterLevel(int level);
    // end::hacking-asciidoctorj-output-formatter-attrs[]

    /**
     * The path to a directory that contains frontmatter files.
     *
     * @param dir Anything convertible to a file.
     */
    // tag::hacking-asciidoctorj-output-formatter-attrs[]
    void setFrontmatterDir(Object dir);
    // end::hacking-asciidoctorj-output-formatter-attrs[]

    /**
     * The path to a directory that contains alternate {@code epub3.css} and {@code epub3-css3-only.css} files to
     * customise the look and feel.
     *
     * @param dir Anything convertible to a file.
     */
    // tag::hacking-asciidoctorj-output-formatter-attrs[]
    void setStylesDir(Object dir);
    // end::hacking-asciidoctorj-output-formatter-attrs[]

// tag::hacking-asciidoctorj-output-formatter-attrs[]
// tag::hacking-asciidoctorj-output-formatter[]
}
// end::hacking-asciidoctorj-output-formatter[]
// end::hacking-asciidoctorj-output-formatter-attrs[]
