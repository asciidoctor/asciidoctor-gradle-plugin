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
package org.asciidoctor.gradle.model5.core.revealjs

import org.asciidoctor.gradle.testfixtures.model5.UnitTestSpecification
import org.gradle.api.provider.Provider

class RevealjsOptionsSpec extends UnitTestSpecification {

    RevealjsOptions options

    void setup() {
        options = project.objects.newInstance(RevealjsOptions)
    }

    void "The slide number '#value' is passed as '#expected'"() {
        when:
        options.slideNumber = value

        then:
        slideNumberAttribute == expected

        where:
        value                 | expected
        'h.v'                 | 'h.v'
        'h/v'                 | 'h/v'
        'c'                   | 'c'
        'c/t'                 | 'c/t'
        'true'                | 'h.v'
        'false'               | 'false'
        'none'                | 'false'
        'HORIZONTAL_VERTICAL' | 'h/v'
        'count_total'         | 'c/t'
    }

    void 'The slide number #value is passed as #expected'() {
        when:
        options.slideNumber = value

        then:
        slideNumberAttribute == expected

        where:
        value                                | expected
        true                                 | 'h.v'
        false                                | 'false'
        RevealjsOptions.SlideNumber.COUNT    | 'c'
    }

    private Object getSlideNumberAttribute() {
        final value = options.attributeProvider.get()['revealjs_slideNumber']
        value instanceof Provider ? value.get() : value
    }
}
