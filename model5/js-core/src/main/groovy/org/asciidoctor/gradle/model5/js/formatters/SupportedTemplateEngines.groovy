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
package org.asciidoctor.gradle.model5.js.formatters

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.errors.ConfigurationNotSupportedException

import static org.ysb33r.grolifant5.api.core.StringTools.COMMA_SPACE

/**
 * Supported template engines for {@code asciidoctor.js}.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
enum SupportedTemplateEngines {
    TEMPLATE_JS('js'),
    EJS('ejs'),
    HANDLEBARS('handlebars'),
    NUNJUCKS('nunjucks'),
    PUG('pug')

    final String engineName
    final boolean hasPackage
    final String packageScope = null

    String getPackageName() {
        this.engineName
    }

    String getRequires() {
        this.engineName
    }

    static SupportedTemplateEngines fromEngine(String engineName) {
        final name = engineName.toLowerCase(Locale.US)
        final target = values().find { it.engineName == engineName}
        if(target == null) {
            throw new ConfigurationNotSupportedException(
                "'${engineName}' is not a supported engine name"
            )
        }
        target
    }
    private SupportedTemplateEngines(String engine) {
        this.engineName = engine
        this.hasPackage = engine != 'js'
    }
}
