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
package org.asciidoctor.gradle.base.slides

import spock.lang.Specification
import spock.lang.Unroll

class ProfileSpec extends Specification {
    @Unroll
    void '#mode is a supported slide deck profile'() {
        expect:
        Profile.valueOf(mode)

        where:
        mode << ['BESPOKE', 'DECK_JS', 'FLOWTIME_JS', 'GENERIC', 'GOOGLE_HTML5',
                 'IMPRESS_JS', 'REMARK_JS', 'REVEAL_JS', 'RUBAN', 'SPF']
    }

    void 'Profile has multi-dimensional naming'() {
        when:
        def profile = Profile.BESPOKE

        then:
        profile.profileShortName == 'bespokejs'
        profile.deckTapeShortName == 'bespoke'
        profile.name == 'Bespoke.js'
    }
}
