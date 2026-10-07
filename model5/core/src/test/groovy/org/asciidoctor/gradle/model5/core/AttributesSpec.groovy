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
package org.asciidoctor.gradle.model5.core

import org.asciidoctor.gradle.model5.core.attributes.Attributes
import org.asciidoctor.gradle.model5.core.plugins.AsciidoctorCorePlugin
import org.asciidoctor.gradle.testfixtures.model5.UnitTestSpecification

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

import static org.asciidoctor.gradle.model5.core.internal.publications.PublicationUtils.DEFAULT_PUBLICATION

class AttributesSpec extends UnitTestSpecification {

    Attributes attributes

    void setup() {
        project.pluginManager.apply(AsciidoctorCorePlugin)
        attributes = project.extensions
            .getByType(AsciidoctorModelExtension)
            .publications
            .getByName(DEFAULT_PUBLICATION)
            .sourceSet
            .attributes
    }

    void '#value.class.simpleName values are converted to #expected'() {
        when:
        attributes.add('value', value)

        then:
        attributes.attributeResolver.get()['value'] == expected

        where:
        value                                                          | expected
        LocalDate.of(2026, 10, 7)                                      | '2026-10-07'
        LocalDateTime.of(2026, 10, 7, 9, 30)                           | '2026-10-07T09:30:00'
        ZonedDateTime.of(2026, 10, 7, 9, 30, 0, 0, ZoneOffset.UTC)     | '2026-10-07T09:30:00'
        OffsetDateTime.of(2026, 10, 7, 9, 30, 0, 0, ZoneOffset.UTC)    | '2026-10-07T09:30:00'
        LocalTime.of(9, 30)                                            | '09:30:00'
    }

    void 'asDate converts #value.class.simpleName values to a date'() {
        when:
        attributes.add('value', attributes.asDate(value))

        then:
        attributes.attributeResolver.get()['value'] == '2026-10-07'

        where:
        value << [
            LocalDate.of(2026, 10, 7),
            LocalDateTime.of(2026, 10, 7, 9, 30),
            ZonedDateTime.of(2026, 10, 7, 9, 30, 0, 0, ZoneOffset.UTC),
            OffsetDateTime.of(2026, 10, 7, 9, 30, 0, 0, ZoneOffset.UTC),
            '2026-10-07'
        ]
    }

    void 'asTime converts #value.class.simpleName values to a time'() {
        when:
        attributes.add('value', attributes.asTime(value))

        then:
        attributes.attributeResolver.get()['value'] == '09:30:00'

        where:
        value << [
            LocalTime.of(9, 30),
            LocalDateTime.of(2026, 10, 7, 9, 30),
            OffsetDateTime.of(2026, 10, 7, 9, 30, 0, 0, ZoneOffset.UTC),
            '09:30'
        ]
    }
}
