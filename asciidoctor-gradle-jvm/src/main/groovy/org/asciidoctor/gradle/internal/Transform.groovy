package org.asciidoctor.gradle.internal

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
