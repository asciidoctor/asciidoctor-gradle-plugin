/*
 * Copyright 2013-2014 the original author or authors.
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
package org.asciidoctor.gradle

import org.apache.commons.io.FileUtils
import spock.lang.Specification

/**
 *
 * @author Rob Winch
 */
class ZipUtilsSpec extends Specification {
    def "extract zip works"() {
        setup:
            InputStream zip = ZipUtils.class.classLoader.getResourceAsStream('testzip.zip')
            File outputDir = createTempDir('extract-zip-works')
        when:
            ZipUtils.unzip(zip, outputDir)
        then:
            mapRelativeFiles(outputDir) == ['testzip/' : 0,
                    'testzip/a' : 395018792,
                    'testzip/adir/' : 0,
                    'testzip/adir/aa' : 658915406,
                    'testzip/adir/ab' : 2821240995,
                    'testzip/b' : 3610934050,
                    'testzip/bdir/' : 0,
                    'testzip/bdir/ba' : 787856160,
                    'testzip/bdir/badir/' : 0,
                    'testzip/bdir/badir/baa' : 3183817691,
                    'testzip/bdir/badir/bab' : 798054536,
                    'testzip/bdir/bb' : 3444995677,
                    'testzip/bdir/bbdir/' : 0,
                    'testzip/bdir/bbdir/bba' : 3243363788]
        cleanup:
            FileUtils.deleteDirectory(outputDir)
    }

    def "extract zip strip root dir works"() {
        setup:
            InputStream zip = ZipUtils.class.classLoader.getResourceAsStream('testzip.zip')
            File outputDir = createTempDir('extract-zip-works')
        when:
            ZipUtils.unzip(zip, new ZipUtils.StripRootDirectoryZipFileFactory(outputDir))
        then:
            mapRelativeFiles(outputDir) == [
                'a' : 395018792,
                'adir/' : 0,
                'adir/aa' : 658915406,
                'adir/ab' : 2821240995,
                'b' : 3610934050,
                'bdir/' : 0,
                'bdir/ba' : 787856160,
                'bdir/badir/' : 0,
                'bdir/badir/baa' : 3183817691,
                'bdir/badir/bab' : 798054536,
                'bdir/bb' : 3444995677,
                'bdir/bbdir/' : 0,
                'bdir/bbdir/bba' : 3243363788]
        cleanup:
            FileUtils.deleteDirectory(outputDir)
    }

    def mapRelativeFiles(File dir) {
        Map<String,Long> result = [:]
        URI dirUri = dir.toURI()
        dir.eachFileRecurse { file ->
            long checksum = file.directory ? 0L : FileUtils.checksumCRC32(file)
            String relativePath = dirUri.relativize(file.toURI()).path
            result.put(relativePath, checksum)
        }
        result
    }

    def createTempDir(String name) {
        String tempDirLocation = System.getProperty('java.io.tmpdir')
        File temp = new File(tempDirLocation, "asciidoctor/tests/ZipUtilsSpec/${name}-${System.currentTimeMillis()}")
        temp.mkdirs()
        temp
    }
}
