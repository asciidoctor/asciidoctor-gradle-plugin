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
package org.asciidoctor.gradle.jvm.slides

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Unroll

import static org.asciidoctor.gradle.jvm.slides.RevealJSOptions.SlideNumber
import static org.asciidoctor.gradle.jvm.slides.RevealJSOptions.SlideNumber.slideNumber

class RevealJSOptionsSpec extends Specification {

    Project project = ProjectBuilder.builder().build()
    RevealJSOptions options

    void setup() {
        options = new RevealJSOptions(project)
    }

    @Unroll
    void 'Set #method (as boolean)'() {
        when:
        final String setter = "set${method.capitalize()}"

        then:
        getBoolFlag(attribute) == null

        when:
        options."${setter}"(true)

        then:
        getBoolFlag(attribute) == true

        when:
        options."${setter}"(false)

        then:
        getBoolFlag(attribute) == false

        where:
        method                   | attribute
        'controls'               | 'controls'
        'progressBar'            | 'progress'
        'slideNumber'            | 'slideNumber'
        'pushToHistory'          | 'history'
        'keyboardShortcuts'      | 'keyboard'
        'overviewMode'           | 'overview'
        'touchMode'              | 'touch'
        'verticalCenter'         | 'center'
        'loop'                   | 'loop'
        'rightToLeft'            | 'rtl'
        'fragments'              | 'fragments'
        'flagEmbedded'           | 'embedded'
        'autoSlideStoppable'     | 'autoSlideStoppable'
        'mouseWheel'             | 'mouseWheel'
        'HideAddressBarOnMobile' | 'hideAddressBar'
        'previewLinks'           | 'previewLinks'
    }

    @Unroll
    void 'Set #method (as integer)'() {
        when:
        final String setter = "set${method.capitalize()}"
        final int safeValue = 3

        then:
        getIntegerFlag(attribute) == null

        when:
        options."${setter}"(-1)

        then:
        thrown(GradleException)
        getIntegerFlag(attribute) == null

        when:
        options."${setter}"(safeValue)

        then:
        getIntegerFlag(attribute) == safeValue

        where:
        method              | attribute
        'autoSlideInterval' | 'autoSlide'
        'viewDistance'      | 'viewDistance'
    }

    @Unroll
    void 'Set transition to #transition'() {
        when:
        String attribute = 'transition'

        then:
        getStringFlag(attribute) == null

        when:
        options.transition = transition

        then:
        getStringFlag(attribute) == transition.toString().toLowerCase()

        when:
        options.transition = null

        then:
        getStringFlag(attribute) == null

        when:
        options.transition = transition.toString()

        then:
        getStringFlag(attribute) == transition.toString().toLowerCase()

        where:
        transition << RevealJSOptions.Transition.values()
    }

    @Unroll
    void 'Set background transition to #transition'() {
        when:
        String attribute = 'backgroundTransition'

        then:
        getStringFlag(attribute) == null

        when:
        options.backgroundTransition = transition

        then:
        getStringFlag(attribute) == transition.toString().toLowerCase()

        when:
        options.backgroundTransition = null

        then:
        getStringFlag(attribute) == null

        when:
        options.backgroundTransition = transition.toString()

        then:
        getStringFlag(attribute) == transition.toString().toLowerCase()

        where:
        transition << RevealJSOptions.Transition.values()
    }

    @Unroll
    void 'Set transition speed to #transition'() {
        when:
        String attribute = 'transitionSpeed'

        then:
        getStringFlag(attribute) == null

        when:
        options.transitionSpeed = transition

        then:
        getStringFlag(attribute) == transition.toString().toLowerCase()

        when:
        options.transitionSpeed = null

        then:
        getStringFlag(attribute) == null

        when:
        options.transitionSpeed = transition.toString()

        then:
        getStringFlag(attribute) == transition.toString().toLowerCase()

        where:
        transition << RevealJSOptions.TransitionSpeed.values()
    }

    void 'Set Parallax background size'() {
        when:
        String attribute = 'parallaxBackgroundSize'

        then:
        getStringFlag(attribute) == null

        when:
        options.parallaxBackgroundSize = 'abc'

        then:
        getStringFlag(attribute) == 'abc'

        when:
        options.parallaxBackgroundSize = { 'def' }

        then:
        getStringFlag(attribute) == 'def'
    }

    @Unroll
    void 'Set slide number to #slideNumber == #expectedValue'() {
        when:
        String attribute = 'slideNumber'

        then:
        getStringFlag(attribute) == null

        when:
        options.slideNumber = slideNumber

        then:
        getStringFlag(attribute) == expectedValue

        when:
        options.slideNumber = null

        then:
        getStringFlag(attribute) == null

        where:
        slideNumber                        | expectedValue
        SlideNumber.NONE                   | 'false'
        SlideNumber.DEFAULT                | 'h.v'
        SlideNumber.HORIZONTAL_VERTICAL    | 'h/v'
        SlideNumber.COUNT                  | 'c'
        SlideNumber.COUNT_TOTAL            | 'c/t'
        null                               | null
        ''                                 | ''
        true                               | 'true'
        false                              | 'false'
        'true'                             | 'true'
        'false'                            | 'false'
        'c'                                | 'c'
        'c/t'                              | 'c/t'
        'h/v'                              | 'h/v'
        'h.v'                              | 'h.v'
        'c.t'                              | 'c.t'
    }

    @SuppressWarnings('DuplicateStringLiteral')
    @Unroll
    void 'Set relative path for #method'() {
        when:
        true

        then:
        options."${method}RelativePath" == defaultValue

        when:
        options."${method}RelativePath" = 'abc/def'

        then:
        options."${method}RelativePath" == 'abc/def'

        when:
        options."${method}RelativePath" = { 'def/fgh' }

        then:
        options."${method}RelativePath" == 'def/fgh'

        where:
        method                    | defaultValue
        'parallaxBackgroundImage' | 'images'
        'customTheme'             | 'style'
        'highlightJsTheme'        | 'css'
    }

    @Unroll
    void 'Set #description (file or URI)'() {
        when:
        final String relPath = 'xyz/rst'
        final String setLocation = "set${base}Location"
        final String setRelativePath = "set${base}RelativePath"
        final String getFile = "get${base}IfFile"
        final String getUri = "get${base}IfUri"
        options."${setRelativePath}"(relPath)

        then:
        options."${getFile}"() == null
        options."${getUri}"() == null
        options.asAttributeMap[attribute] == null

        when:
        options."${setLocation}"('http://def/')

        then:
        options."${getFile}"() == null
        options."${getUri}"() == 'http://def/'.toURI()
        options.asAttributeMap[attribute] == 'http://def/'

        when:
        options."${setLocation}"('https://efg/')

        then:
        options."${getFile}"() == null
        options."${getUri}"() == 'https://efg/'.toURI()
        options.asAttributeMap[attribute] == 'https://efg/'

        when:
        options."${setLocation}"('https://klm/'.toURI())

        then:
        options."${getFile}"() == null
        options."${getUri}"() == 'https://klm/'.toURI()
        options.asAttributeMap[attribute] == 'https://klm/'

        when:
        options."${setLocation}"('abc/def/file.img')

        then:
        options."${getFile}"() == project.file('abc/def/file.img')
        options."${getUri}"() == null
        options.asAttributeMap[attribute] == "${relPath}${File.separator}file.img"

        where:
        base                      | attribute                          | description
        'ParallaxBackgroundImage' | 'revealjs_parallaxBackgroundImage' | 'Parallax background image location'
        'CustomTheme'             | 'revealjs_customtheme'             | 'Custom theme location'
        'HighlightJsTheme'        | 'highlightjs-theme'                | 'Highlight.js theme location'
    }

    @Unroll
    void 'Map SlideNumber(#value) to #expectedValue'() {
        expect:
        slideNumber(value) == expectedValue

        where:
        value     | expectedValue
        true      | SlideNumber.DEFAULT
        false     | SlideNumber.NONE
        null      | SlideNumber.NONE
        'false'   | SlideNumber.NONE
        'none'    | SlideNumber.NONE
        ''        | SlideNumber.DEFAULT
        'true'    | SlideNumber.DEFAULT
        'h.v'     | SlideNumber.DEFAULT
        'default' | SlideNumber.DEFAULT
        'c'       | SlideNumber.COUNT
        'h/v'     | SlideNumber.HORIZONTAL_VERTICAL
        'c/t'     | SlideNumber.COUNT_TOTAL
    }

    Boolean getBoolFlag(final String flagName) {
        Map m = options.asAttributeMap
        final String key = "revealjs_${flagName}"

        if (m.containsKey(key)) {
            m[key] == 'true'
        } else {
            null
        }
    }

    Integer getIntegerFlag(final String flagName) {
        Map m = options.asAttributeMap
        final String key = "revealjs_${flagName}"

        if (m.containsKey(key)) {
            Integer.parseInt(m[key])
        } else {
            null
        }
    }

    String getStringFlag(final String flagName) {
        Map m = options.asAttributeMap
        final String key = "revealjs_${flagName}"
        m[key]
    }

}