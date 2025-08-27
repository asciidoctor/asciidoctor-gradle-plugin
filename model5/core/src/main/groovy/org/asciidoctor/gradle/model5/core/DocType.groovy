package org.asciidoctor.gradle.model5.core

import groovy.transform.CompileStatic

/**
 * Document type.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
enum DocType {
    ARTICLE,
    BOOK,
    MANPAGE,
    INLINE

    DocType fromDsl(String val) {
        valueOf(val.toUpperCase(Locale.US))
    }

    String lc() {
        name().toLowerCase(Locale.US)
    }
}
