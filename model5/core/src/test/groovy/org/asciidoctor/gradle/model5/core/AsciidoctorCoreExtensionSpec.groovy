package org.asciidoctor.gradle.model5.core

import org.asciidoctor.gradle.testfixtures.UnitTestSpecification

class AsciidoctorCoreExtensionSpec extends UnitTestSpecification {

    void setup() {
        project.pluginManager.apply(AsciidoctorCorePlugin)
    }


}