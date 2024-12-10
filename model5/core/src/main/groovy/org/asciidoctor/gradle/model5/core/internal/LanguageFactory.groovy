package org.asciidoctor.gradle.model5.core.internal

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.Language
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory

/**
 * Creates publications.
 *
 * @since 5.0
 *
 * @author Schalk W. Cronjé
 */
@CompileStatic
class LanguageFactory implements NamedDomainObjectFactory<Language> {

    private final ObjectFactory objectFactory

    LanguageFactory(Project project) {
        this.objectFactory = project.objects
    }

    @Override
    Language create(String name) {
        objectFactory.newInstance(DefaultLanguage, name)
    }
}
