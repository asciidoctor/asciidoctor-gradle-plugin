package org.asciidoctor.gradle.base

import spock.lang.Specification

import static org.asciidoctor.gradle.base.SafeMode.SAFE
import static org.asciidoctor.gradle.base.SafeMode.SECURE
import static org.asciidoctor.gradle.base.SafeMode.SERVER
import static org.asciidoctor.gradle.base.SafeMode.UNSAFE
import static org.asciidoctor.gradle.base.SafeMode.safeMode

class SafeModeSpec extends Specification {

    void 'Integers map to correct safe modes'() {
        expect:
        safeMode(0) == UNSAFE
        safeMode(1) == SAFE
        safeMode(10) == SERVER
        safeMode(20) == SECURE
        safeMode(35) == SECURE

        SAFE.level == 1
    }
}