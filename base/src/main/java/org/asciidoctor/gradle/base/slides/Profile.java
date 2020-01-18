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
package org.asciidoctor.gradle.base.slides;

/**
 * Profile names for various slide deck frameworks.
 *
 * @author Schalk W. Cronjé
 * @since 2.2.0
 */
public enum Profile {
    BESPOKE("bespokejs","bespoke", "Bespoke.js"),
    DECK_JS("deckjs", "deck", "Deck.js"),
    DZ("dzslides", "dzslides", "DZslides"),
    FLOWTIME_JS("flowtimejs", "flowtime", "Flowtime.js"),
    GENERIC( "generic", "generic","Generic slides framework"),
    GOOGLE_HTML5("googlehtml5", null, "Google HTML5 slides"),
    IMPRESS_JS("impressjs", "impress", "Impress.js"),
    REMARK_JS("remarkjs", "remark", "Remark.js"),
    REVEAL_JS("revealjs", "reveal", "reveal.js"),
    RUBAN("ruban", null,"Ruban"),
    SPF("spf", null,"Slide Presentation Framework");

    // csss, slidy, shower, webslides

    private final String profile;
    private final String decktapeProfile;
    private final String name;

    private Profile(final String p, final String altProfile, final String n) {
        this.name = n;
        this.profile = p;
        this.decktapeProfile = altProfile;
    }

    public String getProfileShortName() {
        return profile;
    }

    public String getDeckTapeShortName() {
        return decktapeProfile;
    }

    public String getName() {
        return name;
    }
}
