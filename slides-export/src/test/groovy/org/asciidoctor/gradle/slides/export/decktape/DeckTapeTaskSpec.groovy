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
package org.asciidoctor.gradle.slides.export.decktape

import org.asciidoctor.gradle.slides.export.base.ProfileNotSupportedException
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

import static org.asciidoctor.gradle.base.slides.Profile.SPF

class DeckTapeTaskSpec extends Specification {

    Project project = ProjectBuilder.builder().build()
    DeckTapeTask deckTapeTask

    void setup() {
        project.apply plugin: 'org.asciidoctor.decktape.base'
        deckTapeTask = project.tasks.create('decktapeTask', DeckTapeTask)
    }

    void 'Slides are empty by default, but not null'() {
        expect:
        deckTapeTask.slides.get().empty
    }

    void 'Slides can be set'() {
        when:
        deckTapeTask.slides = ['index1.html']
        deckTapeTask.slides(['index2.html'])
        deckTapeTask.slides 'index3.html', 'index4.html'

        then:
        deckTapeTask.slides.get().containsAll((1..4).collect {
            project.file("index${it}.html")
        })
    }

    void 'Only HTML files are accepted as slides'() {
        when:
        deckTapeTask.slides = ['index.html', 'index.xml']

        then:
        deckTapeTask.slides.get().find { it.name == 'index.xml' } == null
        deckTapeTask.slides.get().find { it.name == 'index.html' } != null
    }

    void 'Height and width are unset by default'() {
        expect:
        deckTapeTask.height == null
        deckTapeTask.width == null
    }

    void 'Height and width can be set'() {
        when:
        deckTapeTask.height = 123
        deckTapeTask.width = 456

        then:
        deckTapeTask.height == 123
        deckTapeTask.width == 456
    }

    void 'Profile is not set by default'() {
        expect:
        deckTapeTask.profile == null
    }

    void 'Only supported profiles can be set'() {
        when:
        deckTapeTask.profile = 'abc'

        then:
        thrown(IllegalArgumentException)

        when:
        deckTapeTask.profile = SPF
        deckTapeTask.profile

        then:
        thrown(ProfileNotSupportedException)
    }

    void 'Only JPG & PNG are acceptable image export formats'() {
        when:
        deckTapeTask.screenshots.format = 'png'
        deckTapeTask.screenshots.format = 'jpg'

        then:
        noExceptionThrown()

        when:
        deckTapeTask.screenshots.format = 'abc'

        then:
        thrown(IllegalArgumentException)
    }

    void 'Screenshots can be configured from an Action'() {
        when:
        deckTapeTask.screenshots(new Action<DeckTapeTask.ScreenShots>() {
            @Override
            void execute(DeckTapeTask.ScreenShots screenShots) {
                screenShots.height = 123
            }
        })

        then:
        deckTapeTask.screenshots.height == 123
    }

    void 'Interslide pause can be set from string or integer'() {
        when:
        deckTapeTask.interSlidePause = '12'

        then:
        deckTapeTask.interSlidePause == 12

        when:
        deckTapeTask.interSlidePause = 13

        then:
        deckTapeTask.interSlidePause == 13

        when:
        deckTapeTask.interSlidePause = 'abc'

        then:
        thrown(IllegalArgumentException)
    }

    void 'Load pause can be set from string or integer'() {
        when:
        deckTapeTask.loadPause = '12'

        then:
        deckTapeTask.loadPause == 12

        when:
        deckTapeTask.loadPause = 13

        then:
        deckTapeTask.loadPause == 13

        when:
        deckTapeTask.loadPause = 'abc'

        then:
        thrown(IllegalArgumentException)
    }

    void 'Chrome arguments can be set'() {
        when:
        deckTapeTask.chromeArgs = ['-a']
        deckTapeTask.chromeArgs '-b', '-c'

        then:
        deckTapeTask.chromeArgs.containsAll(['-a', '-b', '-c'])
    }

}