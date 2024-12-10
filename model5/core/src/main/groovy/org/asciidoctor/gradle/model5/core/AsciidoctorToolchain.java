package org.asciidoctor.gradle.model5.core;

import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer;
import org.gradle.api.Named;

/**
 *
 * Represents an Asciidoctor toolchain.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
public interface AsciidoctorToolchain extends Named {

    /**
     * Output formatters registered with this toolchain.
     *
     * @return
     */
    ExtensiblePolymorphicDomainObjectContainer<AsciidoctorOutputFormatter> getRegisteredOutputFormatters();

    /**
     * The interface this toolchain instance represents, not the actual instance itself.
     *
     * @return CLass type of the toolchain interface.
     */
    Class<?> getToolchainClass();
}
