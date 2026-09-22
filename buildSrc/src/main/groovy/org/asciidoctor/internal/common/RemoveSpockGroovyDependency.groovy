package org.asciidoctor.internal.common

import org.gradle.api.artifacts.CacheableRule
import org.gradle.api.artifacts.ComponentMetadataContext
import org.gradle.api.artifacts.ComponentMetadataRule
import org.gradle.api.artifacts.DirectDependenciesMetadata

@CacheableRule
@SuppressWarnings('AbstractClassWithoutAbstractMethod')
abstract class RemoveSpockGroovyDependency implements ComponentMetadataRule {
    void execute(ComponentMetadataContext context) {
        context.details.allVariants { vm ->
            vm.withDependencies { DirectDependenciesMetadata dtd ->
                dtd.removeAll { it.group in ['org.codehaus.groovy'] }
            }
        }
    }
}