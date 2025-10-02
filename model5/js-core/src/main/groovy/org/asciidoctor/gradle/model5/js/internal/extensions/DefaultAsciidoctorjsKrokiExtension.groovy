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
package org.asciidoctor.gradle.model5.js.internal.extensions

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.internal.DefaultKrokiConfiguration
import org.asciidoctor.gradle.model5.js.extensions.AsciidoctorjsKrokiExtension
import org.asciidoctor.gradle.model5.js.toolchains.AsciidoctorjsToolchain
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.Project

import javax.inject.Inject

import static org.asciidoctor.gradle.model5.js.internal.PluginUtils.loadDefaultVersion

/**
 * Implementation of {@link AsciidoctorjsKrokiExtension}
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class DefaultAsciidoctorjsKrokiExtension extends AbstractAsciidoctorjsExtensionVersioned
    implements AsciidoctorjsKrokiExtension {

    static class Factory extends AbstractFactory implements NamedDomainObjectFactory<AsciidoctorjsKrokiExtension> {

        @Inject
        Factory(AsciidoctorjsToolchain toolchain, Project project) {
            super(toolchain, project)
        }

        @Override
        AsciidoctorjsKrokiExtension create(String name) {
            objectFactory.newInstance(DefaultAsciidoctorjsKrokiExtension, name, toolchain)
        }
    }
    public static final String DEFAULT_NAME = 'kroki'
    private static final String PACKAGE = 'asciidoctor-kroki'
    private static final String REQUIRES = PACKAGE

    @Delegate
    private final DefaultKrokiConfiguration kroki

    @Inject
    DefaultAsciidoctorjsKrokiExtension(String name, AsciidoctorjsToolchain tc, Project project) {
        super(
            name,
            null,
            PACKAGE,
            loadDefaultVersion('asciidoctorjs.kroki', project, tc.class.classLoader),
            tc,
            project
        )
        this.kroki = project.objects.newInstance(DefaultKrokiConfiguration)
        this.packageRequires.add(REQUIRES)
    }
}
