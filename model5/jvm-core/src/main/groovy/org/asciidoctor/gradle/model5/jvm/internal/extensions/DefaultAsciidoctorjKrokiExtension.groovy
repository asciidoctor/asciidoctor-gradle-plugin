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
package org.asciidoctor.gradle.model5.jvm.internal.extensions

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.internal.DefaultKrokiConfiguration
import org.asciidoctor.gradle.model5.jvm.extensions.AsciidoctorjKrokiExtension
import org.asciidoctor.gradle.model5.jvm.internal.PluginUtils
import org.asciidoctor.gradle.model5.jvm.toolchains.AsciidoctorjToolchain
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.Project

import javax.inject.Inject

import static org.asciidoctor.gradle.model5.jvm.JvmModel.ASCIIDOCTORJ_GEM_KROKI

/**
 * Implementation of {@link AsciidoctorjKrokiExtension}.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class DefaultAsciidoctorjKrokiExtension extends AbstractAsciidoctorjGemBasedExtension
    implements AsciidoctorjKrokiExtension {

    static class Factory extends AbstractFactory implements NamedDomainObjectFactory<AsciidoctorjKrokiExtension> {
        @Inject
        Factory(AsciidoctorjToolchain toolchain, Project project) {
            super(toolchain, project)
        }

        @Override
        AsciidoctorjKrokiExtension create(String name) {
            objectFactory.newInstance(DefaultAsciidoctorjKrokiExtension, name, toolchain)
        }
    }

    public static final String DEFAULT_NAME = 'kroki'
    public static final String REQUIRES = 'asciidoctor-kroki'

    @Delegate
    private final DefaultKrokiConfiguration kroki

    @Inject
    DefaultAsciidoctorjKrokiExtension(String name, AsciidoctorjToolchain tc, Project project) {
        super(
            name,
            ASCIIDOCTORJ_GEM_KROKI,
            PluginUtils.loadDefaultVersion('asciidoctorj.kroki', project, tc.class.classLoader),
            REQUIRES,
            ['asdciidoctor'],
            tc,
            project
        )
        this.kroki = project.objects.newInstance(DefaultKrokiConfiguration)
        attributes.putAll(kroki.attributeProvider)
    }

    @Override
    protected Class<?> getDslType() {
        AsciidoctorjKrokiExtension
    }
}
