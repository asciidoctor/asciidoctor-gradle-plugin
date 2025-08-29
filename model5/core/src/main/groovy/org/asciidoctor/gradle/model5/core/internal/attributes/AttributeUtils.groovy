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
package org.asciidoctor.gradle.model5.core.internal.attributes

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.attributes.AttributeType
import org.asciidoctor.gradle.model5.core.errors.UnsupportedAttributeType
import org.ysb33r.grolifant5.api.core.StringTools
import org.ysb33r.grolifant5.api.core.Transform

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor
import java.util.function.BiFunction
import java.util.function.Function

/**
 * Utilities for converting attributes values.
 *
 * @since 5.0
 *
 * @author Schalk W. Cronjé
 */
@CompileStatic
class AttributeUtils {

    private static DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ISO_DATE_TIME
    private static DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_DATE
    private static DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ISO_TIME

    static String resolveAttribute(StringTools str, Object value) {
        Transform.convertItem(value, x -> CONVERTER.apply(str, x))
    }

    static String resolveDateType(Object value) {
        Transform.convertItem(value, DATE_CONVERTER)
    }

    static String resolveTimeType(Object value) {
        Transform.convertItem(value, TIME_CONVERTER)
    }

    private static Function<Object, String> TIME_CONVERTER = (Object value) -> {
        switch (value) {
            case Date:
                return formatTime(((Date) value).toLocalTime())
            case TemporalAccessor:
                return formatTime(LocalTime.from((TemporalAccessor) value))
            case CharSequence:
                return formatTime(LocalTime.parse((CharSequence) value))
            default:
                throw new UnsupportedAttributeType("The value '${value}' is not suitable for a time conversion")
        }
    }

    private static Function<Object, String> DATE_CONVERTER = (Object value) -> {
        switch (value) {
            case Date:
                return formatDate(((Date) value).toLocalDateTime())
            case TemporalAccessor:
                return formatDate(LocalDateTime.from((TemporalAccessor) value))
            default:
                throw new UnsupportedAttributeType(
                        "The value '${value}' is not suitable for a date (and time) conversion"
                )
        }
    }

    private static BiFunction<StringTools, Object, String> CONVERTER = (StringTools stringTools, Object input) -> {
        if (input == null) {
            return null
        }
        switch (input) {
            case boolean:
            case Boolean:
                return input ? "" : null
            case AttributeType:
                return ((AttributeType) input).render()
            case Date:
                return formatDateTime(((Date) input).toLocalDateTime())
            case LocalTime:
                return formatTime((LocalTime) input)
            case LocalDate:
                return formatDate(LocalDateTime.from((LocalDate) input))
            case LocalDateTime:
                return formatDateTime((LocalDateTime) input)
            case ZonedDateTime:
                return formatDateTime(LocalDateTime.from((ZonedDateTime) input))
            case OffsetDateTime:
                return formatDateTime(LocalDateTime.from((ZonedDateTime) input))
            case OffsetTime:
                return formatTime(LocalTime.from((OffsetTime) input))
            case URI:
                return ((URI) input).toASCIIString()
            default:
                stringTools.stringizeOrNull(input)
        }
    }

    static String formatDateTime(LocalDateTime dt) {
        dt.format(DATETIME_FORMAT)
    }

    static String formatDate(LocalDateTime dt) {
        dt.format(DATE_FORMAT)
    }

    static String formatTime(LocalTime dt) {
        dt.format(TIME_FORMAT)
    }
}
