/*
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
package org.asciidoctor.gradle.model5.core.pdfthemes

import org.asciidoctor.gradle.model5.core.plugins.AsciidoctorCorePdfThemesPlugin
import org.asciidoctor.gradle.testfixtures.model5.UnitTestSpecification
import spock.lang.IgnoreIf
import spock.lang.PendingFeature

/**
 *
 * @author Schalk W. Cronjé
 *
 * @since
 */
class PdfThemesPluginSpec extends UnitTestSpecification {

    AsciidoctorPdfThemeExtension extPdfThemes

    void setup() {
        project.pluginManager.apply(AsciidoctorCorePdfThemesPlugin)
        extPdfThemes = project.extensions.getByType(AsciidoctorPdfThemeExtension)
    }

    void 'Can add a local theme'() {
        setup:
        project.allprojects {
            // tag::local-theme[]
            asciidocPdfThemes {
                themeCollections {
                    myLocal(LocalThemeCollection) { // <.>
                        themeDir = 'src/pdfThemes/myLocal'  // <.>
                    }
                }
                themes {
                    myLocal {
                        fromCollection('myLocal') // <.>
                    }
                }
            }
            // end::local-theme[]
        }

        expect:
        extPdfThemes.themes.myLocal.themeDir.get().asFile == project.file('src/pdfThemes/myLocal')
    }

    @IgnoreIf(value = {IS_OFFLINE }, reason = OFFLINE_REASON)
    void 'Can load a package from Github'() {
        setup:
        project.allprojects {
            // tag::github-theme[]
            asciidocPdfThemes {
                themeCollections {
                    kuboaki(GithubThemeCollection) { // <.>
                        from {
                            organisation = 'kuboaki' // <.>
                            repository = 'beauty-pdf-using-asciidoctor-pdf' // <.>
                            branch = 'master' // <.>
                        }
                        pathInRepo = 'theme' // <.>
                    }
                }
                themes {
                    myTheme {
                        fromCollection('kuboaki') // <.>
                        themeName = 'mystyle' // <.>
                    }
                }
            }
            // end::github-theme[]
        }

        when:
        final dir = extPdfThemes.themes.myTheme.themeDir.get().asFile
        final themeName = extPdfThemes.themes.myTheme.themeName.get()

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
            asciidocPdfThemes {
                themeCollections {
                    asciidoc4555321(GitlabThemeCollection) {
                        from {
                            organisation = 'asciidoc'
                            repository = 'asciidoctor-pdf-cust-themes'
                            branch = 'master'
                        }
                    }
                }
                themes {
                    myTheme {
                        fromCollection('asciidoc4555321')
                        themeName = 'pcb-blue'
                    }
                }
            }
        }

        when:
        final dir = extPdfThemes.themes.myTheme.themeDir.get().asFile
        final themeName = extPdfThemes.themes.myTheme.themeName.get()

        then:
        dir.exists()
        new File(dir,'pcb-blue-theme.yml').exists()
        themeName == 'pcb-blue'
    }
}