/*
 * ============================================================================
 * (C) Copyright Schalk W. Cronje 2013 - 2024
 *
 * This software is licensed under the Apache License 2.0
 * See http://www.apache.org/licenses/LICENSE-2.0 for license details
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *
 * ============================================================================
 */
package org.asciidoctor.gradle.testfixtures

import org.gradle.testkit.runner.GradleRunner
import org.ysb33r.grolifant5.api.core.OperatingSystem
import spock.lang.Specification
import spock.lang.TempDir

import static org.asciidoctor.gradle.testfixtures.DslType.GROOVY_DSL
import static org.asciidoctor.gradle.testfixtures.DslType.GROOVY_DSL
import static org.asciidoctor.gradle.testfixtures.FunctionalTestSetup.getOfflineRepositoriesGroovyDsl
import static org.asciidoctor.gradle.testfixtures.FunctionalTestSetup.getOfflineRepositoriesKotlinDsl

class IntegrationSpecification extends Specification {
    public static final boolean IS_KOTLIN_DSL = false
    public static final boolean IS_GROOVY_DSL = true
    public static final OperatingSystem OS = OperatingSystem.current()

    @TempDir
    File testProjectDir

    File projectDir
    File buildDir
    File projectCacheDir
    File buildFile
    File buildFileKts
    File settingsFile
    File testKitDir

    void setup() {
        projectDir = new File(testProjectDir, 'test-project')
        projectDir.mkdirs()
        buildDir = new File(projectDir, 'build')
        projectCacheDir = new File(projectDir, '.gradle')
        buildFile = new File(projectDir, 'build.gradle')
        buildFileKts = new File(projectDir, 'build.gradle.kts')
        settingsFile = new File(projectDir, 'settings.gradle')
        settingsFile.text = ''

        testKitDir = new File(testProjectDir, ".testkit-${UUID.randomUUID()}")
        testKitDir.mkdirs()
    }

    void cleanup() {
        if (testKitDir && testKitDir.exists()) {
            testKitDir.deleteDir()
        }
    }

    GradleRunner getGradleRunner(
        boolean groovyDsl,
        String taskName
    ) {
        getGradleRunner(groovyDsl, [taskName])
    }

    GradleRunner getGradleRunner(
        boolean groovyDsl,
        List<String> args
    ) {
        GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments(args)
            .forwardOutput()
            .withDebug(groovyDsl)
            .withPluginClasspath()
            .withTestKitDir(testKitDir)
    }

    GradleRunner getGradleRunnerConfigCache(
        boolean groovyDsl,
        List<String> args
    ) {
        getGradleRunner(groovyDsl, args + ['--configuration-cache', '--configuration-cache-problems=fail'])
            .withGradleVersion('8.9')
            .withDebug(false)
    }

    static String getEscapedEnvPathString() {
        if (OS.windows) {
            System.getenv(OS.pathVar).replace(BACKSLASH, DOUBLE_BACKSLASH)
        } else {
            System.getenv(OS.pathVar)
        }
    }

    static String getEscapedPathString(String path) {
        if (OS.windows) {
            path.replace('/', BACKSLASH)
        } else {
            path
        }
    }

    static String getOfflineRepositories(DslType dslType = GROOVY_DSL) {
        dslType == GROOVY_DSL ? getOfflineRepositoriesGroovyDsl() : offlineRepositoriesKotlinDsl
    }

    static private final String BACKSLASH = '\\'
    static private final String DOUBLE_BACKSLASH = BACKSLASH * 2
}