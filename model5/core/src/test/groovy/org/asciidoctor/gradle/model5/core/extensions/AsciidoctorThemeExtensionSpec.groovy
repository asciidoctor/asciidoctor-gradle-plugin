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
package org.asciidoctor.gradle.model5.core.extensions

import org.asciidoctor.gradle.model5.core.pdfthemes.GithubThemeCollection
import org.asciidoctor.gradle.model5.core.pdfthemes.GitlabThemeCollection
import org.asciidoctor.gradle.model5.core.pdfthemes.LocalThemeCollection
import org.asciidoctor.gradle.model5.core.plugins.AsciidoctorCoreThemesPlugin
import org.asciidoctor.gradle.testfixtures.model5.UnitTestSpecification
import spock.lang.IgnoreIf
import spock.lang.PendingFeature

class AsciidoctorThemeExtensionSpec extends UnitTestSpecification {

    AsciidoctorThemeExtension extThemes

    void setup() {
        project.pluginManager.apply(AsciidoctorCoreThemesPlugin)
        extThemes = project.extensions.getByType(AsciidoctorThemeExtension)
    }

    void 'Can add a local PDF theme'() {
        setup:
        project.allprojects {
            // tag::local-theme[]
            asciidocThemes {
                pdfThemeCollections {
                    myLocal(LocalThemeCollection) { // <.>
                        themeDir = 'src/pdfThemes/myLocal'  // <.>
                    }
                }
                pdfThemes {
                    myLocal {
                        fromCollection('myLocal') // <.>
                    }
                }
            }
            // end::local-theme[]
        }

        expect:
        extThemes.pdfThemes.myLocal.themeDir.get().asFile == project.file('src/pdfThemes/myLocal')
    }

    @IgnoreIf(value = {IS_OFFLINE }, reason = OFFLINE_REASON)
    void 'Can load a PDF package from Github'() {
        setup:
        project.allprojects {
            // tag::github-theme[]
            asciidocThemes {
                pdfThemeCollections {
                    kuboaki(GithubThemeCollection) { // <.>
                        from {
                            organisation = 'kuboaki' // <.>
                            repository = 'beauty-pdf-using-asciidoctor-pdf' // <.>
                            branch = 'master' // <.>
                        }
                        pathInRepo = 'theme' // <.>
                    }
                }
                pdfThemes {
                    myTheme {
                        fromCollection('kuboaki') // <.>
                        themeName = 'mystyle' // <.>
                    }
                }
            }
            // end::github-theme[]
        }

        when:
        final dir = extThemes.pdfThemes.myTheme.themeDir.get().asFile
        final themeName = extThemes.pdfThemes.myTheme.themeName.get()

        then:
        dir.exists()
        new File(dir,'mystyle-theme.yml').exists()
        themeName == 'mystyle'
    }

    @IgnoreIf(value = {IS_OFFLINE }, reason = OFFLINE_REASON)
    @PendingFeature(reason = 'Currently Grolifant fails to download from Gitlab with 406 error.')
    void 'Can load a package from Gitlab'() {
        setup:
        project.allprojects {
            asciidocThemes {
                pdfThemeCollections {
                    asciidoc4555321(GitlabThemeCollection) {
                        from {
                            organisation = 'asciidoc'
                            repository = 'asciidoctor-pdf-cust-themes'
                            branch = 'master'
                        }
                    }
                }
                pdfThemes {
                    myTheme {
                        fromCollection('asciidoc4555321')
                        themeName = 'pcb-blue'
                    }
                }
            }
        }

        when:
        final dir = extThemes.pdfThemes.myTheme.themeDir.get().asFile
        final themeName = extThemes.pdfThemes.myTheme.themeName.get()

        then:
        dir.exists()
        new File(dir,'pcb-blue-theme.yml').exists()
        themeName == 'pcb-blue'
    }
}