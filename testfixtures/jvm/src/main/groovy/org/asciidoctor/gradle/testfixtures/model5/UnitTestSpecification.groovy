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
package org.asciidoctor.gradle.testfixtures.model5

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.ysb33r.grolifant5.api.core.ProjectOperations
import org.ysb33r.grolifant5.api.core.plugins.GrolifantServicePlugin
import spock.lang.Specification
import spock.lang.TempDir

class UnitTestSpecification extends Specification {
    @TempDir
    File projectDir
    Project project = ProjectBuilder.builder().withProjectDir(projectDir)build()
    ProjectOperations projectOperations

    void setup() {
        project.pluginManager.apply(GrolifantServicePlugin)
        projectOperations = ProjectOperations.find(project)
    }
}