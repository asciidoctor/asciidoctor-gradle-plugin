package org.asciidoctor.gradle

/**
 * Supported backends.
 *
 * @author Benjamin Muschko
 */
enum AsciidoctorBackend {
    HTML5('html5'), DOCBOOK('docbook')

    private final static Map<String, AsciidoctorBackend> ALL_BACKENDS
    private final String id

    static {
        ALL_BACKENDS = new HashMap<String, AsciidoctorBackend>()

        values().each {
            ALL_BACKENDS[it.id] = it
        }

        ALL_BACKENDS.asImmutable()
    }

    private AsciidoctorBackend(String id) {
        this.id = id
    }

    String getId() {
        id
    }

    static boolean isSupported(String name) {
        ALL_BACKENDS.containsKey(name)
    }
}