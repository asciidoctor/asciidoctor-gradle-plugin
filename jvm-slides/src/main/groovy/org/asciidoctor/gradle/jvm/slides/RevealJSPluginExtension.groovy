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
package org.asciidoctor.gradle.jvm.slides

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.base.AbstractDownloadableComponent
import org.gradle.api.Project
import org.ysb33r.grolifant.api.StringUtils

/** Reveal.js plugins.
 *
 * @since 2.0
 */
@CompileStatic
class RevealJSPluginExtension extends AbstractDownloadableComponent<LocalRevealJSPlugin, ResolvedRevealJSPlugin> {

    public final static String NAME = 'revealjsPlugins'

    RevealJSPluginExtension(Project project) {
        super(project)
    }

    /** Instantiates a component of type {@link LocalRevealJSPlugin}.
     *
     * @param name Name of component
     * @return New component source description
     */
    @Override
    protected LocalRevealJSPlugin instantiateComponentSource(String name) {
        new LocalRevealJSPlugin()
    }

    /** Create a closure that will convert an instance of a {@code LocalRevealsJSPlugin}
     * into a {@code ResolvedRevealJsPlugin}.
     *
     * @param component Component to be converted.
     * @return Converting closure.
     */
    @Override
    protected Closure convertible(final LocalRevealJSPlugin component) {
        { Project project1 ->
            new ResolvedRevealJSPlugin() {
                @Override
                File getLocation() {
                    project1.file(component.location)
                }

                @Override
                String getName() {
                    StringUtils.stringize(component.name)
                }
            }
        }.curry(project)
    }

    /** Instantiates a resolved component.
     *
     * @param name Name of component
     * @param path Path to where component is located on local filesystem.
     * @return Instantiated component.
     */
    @Override
    protected ResolvedRevealJSPlugin instantiateResolvedComponent(final String componentName, final File path) {
        new ResolvedRevealJSPlugin() {
            @Override
            File getLocation() {
                path
            }

            @Override
            String getName() {
                componentName
            }
        }
    }
}
