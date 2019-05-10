/*
 * Copyright 2013-2019 the original author or authors.
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
package org.asciidoctor.gradle.base.slides;

import java.util.Collections;
import java.util.Locale;

/** Profile names for various slide deck frameworks.
 *
 * @since 2.2.0
 * @author Schalk W. Cronjé
 */
public enum Profile {
    DECK_JS("deckjs", "Deck.js"),
    IMPRESS_JS("impressjs", "Impress.js"),
    DZ("dzslides","DZslides"),
    FLOWTIME_JS("flowtimejs","Flowtime.js"),
    GOOGLE_HTML5("googlehtml5","Goole HTML5 slides"),
    SPF("spf","Slide Presentation Framework"),
    REMARK_JS("remarkjs","Remark.js"),
    REVEAL_JS("revealjs", "reveal.js"),
    RUBAN("ruban","Ruban");

    private Profile(final String p, final String n) {
        this.name = n;
        this.profile = p;
    }

    public String getProfileShortName() {
        return profile;
    }
    public String getName() {
        return name;
    }

    private final String profile;
    private final String name;
}
