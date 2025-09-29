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
package org.asciidoctor.gradle.model5.jvm.internal.engines

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.jvm.engines.ErbEngine
import org.asciidoctor.gradle.model5.jvm.engines.EngineOptions
import org.gradle.api.Project
import org.gradle.api.provider.Provider

import javax.inject.Inject

/**
 * Implementation of engine options.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class DefaultEngineOptions implements EngineOptions {

    final Provider<LauncherEngineOptions> engineOptionsProvider

    private ErbEngine erubySetting
    private boolean catalogAssetsSetting
    private boolean sourceMapSetting

    @Inject
    DefaultEngineOptions(Project tempProjectReference) {
        this.catalogAssetsSetting = false
        this.sourceMapSetting = true
        this.erubySetting = ErbEngine.ERB

        engineOptionsProvider = tempProjectReference.provider {
            new LauncherEngineOptions().tap {
                it.eruby = erubySetting.optionValue
                it.catalogAssets = catalogAssetsSetting
                it.sourceMap = sourceMapSetting
            }
        }
    }

    /**
     * Sets the eRuby engine.
     *
     * @param engine Engine value
     */
    @Override
    void setEruby(ErbEngine engine) {
        this.erubySetting = engine
    }

    /**
     * Whether to capture images and links in the reference table.
     *
     * @param flag {@code true} to capture images and links.
     */
    @Override
    void setCatalogAssets(boolean flag) {
        this.catalogAssetsSetting = flag
    }

    /**
     * Whether to track file and line numbers for parsed blocks.
     *
     * @param flag {@code true} to perform tracking.
     */
    @Override
    void setSourceMap(boolean flag) {
        this.sourceMapSetting = flag
    }
}
