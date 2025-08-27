package org.asciidoctor.gradle.model5.js.engines

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.model5.core.AsciidoctorCorePlugin
import org.asciidoctor.gradle.model5.core.AsciidoctorLauncher
import org.asciidoctor.gradle.model5.engines.AsciidoctorEngine
import org.asciidoctor.gradle.model5.js.internal.DefaultLauncher
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.ysb33r.gradle.nodejs.NodeJSExecSpec
import org.ysb33r.gradle.nodejs.NodeJSExtension
import org.ysb33r.gradle.nodejs.NpmDependencyGroup
import org.ysb33r.gradle.nodejs.NpmExtension
import org.ysb33r.gradle.nodejs.SimpleNpmPackageDescriptor
import org.ysb33r.gradle.nodejs.utils.npm.NpmExecutor
import org.ysb33r.grolifant5.api.core.ConfigCacheSafeOperations

import javax.inject.Inject

import static org.asciidoctor.gradle.model5.core.AsciidoctorCorePlugin.INTERMEDIATE_RESOURCE_PATH

/**
 * The core engine for running {@code asciidoctor.js}.
 *
 * @author Schalk W. Cronjé
 *
 * @since 5.0
 */
@CompileStatic
class AsciidoctorJsNodeEngine implements AsciidoctorEngine {

    private final NodeJSExtension nodejs
    private final NpmExtension npm
    private final ConfigCacheSafeOperations ccso
    private final Property<String> asciidoctorjsLocation
    private final Property<String> asciidoctorjsVersion

    private final Provider<DefaultLauncher> launcherProvider

    final String name

    @Inject
    AsciidoctorJsNodeEngine(String name, Project tempProjectReference) {
        this.name = name
        this.ccso = ConfigCacheSafeOperations.from(tempProjectReference)
        this.nodejs = new NodeJSExtension(tempProjectReference)
        this.npm = new NpmExtension(tempProjectReference, this.nodejs).tap {
            homeDirectory = ccso.fsOperations().buildDirDescendant(
                    "tmp/asciidoctorjs-engine/${ccso.fsOperations().toSafeFileName(name)}")
        }

        final props = ccso.fsOperations().loadPropertiesFromResource(
                "${INTERMEDIATE_RESOURCE_PATH}/asciidoctor5-js-core-plugin.properties",
                this.class.classLoader
        )

        this.asciidoctorjsVersion = tempProjectReference.objects.property(String)
                .convention(props['asciidoctorjs'].toString())
        this.asciidoctorjsLocation = tempProjectReference.objects.property(String)
                .convention(resolveAsciidoctorjsAsProvider())
        this.asciidoctorjsLocation.finalizeValueOnRead()

        this.launcherProvider = createLauncher(tempProjectReference)
    }

    /**
     * Something that can execute Asciidoctor conversions.
     *
     * @return Provider to a runnable Asciidoctor engine.
     */
    @Override
    Provider<? extends AsciidoctorLauncher> getLauncher() {
        this.launcherProvider
    }

    void setAsciidoctorjVersion(Object ver) {
        ccso.stringTools().updateStringProperty(this.asciidoctorjsVersion, ver)
    }

    void setNodeVersion(Object ver) {
        nodejs.executableByVersion(ver)
    }

    private Provider<String> resolveAsciidoctorjsAsProvider() {
        final fakeVer = ccso.providerTools().provider { -> '0.0.1' }
        final npmExecutor = new NpmExecutor(ccso, nodejs, npm)
        final installArgs = ['--no-bin-links', '--no-package-lock', '--loglevel=error']
        final env = npmExecutor.environmentFromExtensions(nodejs, npm)

        asciidoctorjsVersion.zip(npm.homeDirectoryProvider) { ver, npmHome ->
            File packageJson = new File(npmHome, 'package.json')
            if (!packageJson.exists()) {
                npmHome.mkdirs()
                npmExecutor.initPkgJson(name, fakeVer)
            }
            final descriptor = new SimpleNpmPackageDescriptor('asciidoctor', 'cli', ver)
            npmExecutor.installNpmPackage(descriptor, NpmDependencyGroup.DEVELOPMENT, installArgs, env)
            'node_modules/@asciidoctor/cli/bin/asciidoctor'
        }
    }

    private NodeJSExecSpec createExecSpec() {
        final env = NpmExecutor.environmentFromExtensions(nodejs,npm)
        nodejs.createExecSpec().tap { spec ->
            entrypoint {
                workingDir(this.npm.homeDirectoryProvider)
                environment(env)
            }
            runnerSpec {
                args(this.asciidoctorjsLocation)
            }
        }
    }

    private Provider<DefaultLauncher> createLauncher(Project tempProjectReference) {
        final execSpec = createExecSpec()
        final jsLauncher = tempProjectReference.objects.newInstance(DefaultLauncher, execSpec)
        tempProjectReference.provider { -> jsLauncher }
    }
}
