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
package org.asciidoctor.gradle.base

import groovy.transform.CompileStatic

import java.util.function.Function
import java.util.stream.Collectors

/** Transforms a collection to another collection.
 *
 * @since 2.0
 */
@CompileStatic
class Transform {
    static <I,O> List<O> toList(final Collection<I> collection, Function<I,O> tx ) {
        collection.stream().map(tx).collect(Collectors.toList())
    }

    static <I,O> Set<O> toSet(final Collection<I> collection, Function<I,O> tx ) {
        collection.stream().map(tx).collect(Collectors.toSet())
    }

    static <I,O> Set<O> toSet(final Iterable<I> collection, Function<I,O> tx ) {
        collection.toList().stream().map(tx).collect(Collectors.toSet())
    }
}
