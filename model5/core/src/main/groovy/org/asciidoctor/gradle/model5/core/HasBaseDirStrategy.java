package org.asciidoctor.gradle.model5.core;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;

public interface HasBaseDirStrategy {

    /**
     * Direct access to basedir configuration.
     *
     * @return Base dirrectory configuration.
     */
    BaseDirConfiguration getBaseDir();

    /**
     * Configure the base directory.
     *
     * @param configurator Configurator
     */
    void baseDir(Action<BaseDirConfiguration> configurator);

    /**
     * Configure the base directory.
     *
     * @param configurator Configurator
     */
    void baseDir(@DelegatesTo(BaseDirConfiguration.class) Closure<?> configurator);
}
