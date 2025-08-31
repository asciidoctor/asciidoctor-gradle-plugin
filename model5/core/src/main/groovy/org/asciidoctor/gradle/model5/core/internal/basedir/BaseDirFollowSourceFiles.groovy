package org.asciidoctor.gradle.model5.core.internal.basedir

import groovy.transform.CompileStatic
import org.gradle.api.model.ObjectFactory

import javax.inject.Inject

/**
 * Allows base directory to be adjusted on a per-source file basis.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class BaseDirFollowSourceFiles extends BaseDirFollowSourceDir {

    @Inject
    BaseDirFollowSourceFiles(ObjectFactory objectFactory) {
        super(objectFactory)
        adjustBaseDir.set(true)
    }
}
