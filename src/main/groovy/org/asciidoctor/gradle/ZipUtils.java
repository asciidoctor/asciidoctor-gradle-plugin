/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.asciidoctor.gradle;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.zip.*;

/**
 *
 * @author Rob Winch
 */
class ZipUtils {

    /**
     * Unzips a file from an input stream to a given folder location
     *
     * @param zip the InputStream of the zip file
     * @param outputFolder the directory to write the zip to
     * @throws IOException
     */
    static void unzip(InputStream zip, File outputFolder) throws IOException {
        unzip(zip, new DirectoryZipFileFactory(outputFolder));
    }

    /**
     * Unzips a file from an input stream to a location specified by the ZipFileFactory interface.
     * @param zip
     * @param zipFileFactory
     * @throws IOException
     */
    static void unzip(InputStream zip, ZipFileFactory zipFileFactory) throws IOException {
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(zip));
        ZipEntry zipEntry = zis.getNextEntry();

        while (zipEntry != null) {

            File unzippedFile = zipFileFactory.createUnzippedFile(zipEntry);

            if(zipEntry.isDirectory()) {
                unzippedFile.mkdirs();
            } else {
                unzippedFile.getParentFile().mkdirs();

                FileOutputStream fos = new FileOutputStream(unzippedFile);

                IOUtils.copy(zis, fos);
            }
            zipEntry = zis.getNextEntry();
        }

        zis.closeEntry();
        zis.close();
    }

    interface ZipFileFactory {
        File createUnzippedFile(ZipEntry zipEntry);
    }

    private static class DirectoryZipFileFactory implements ZipFileFactory {
        private File outputFolder;

        private DirectoryZipFileFactory(File outputFolder) {
            if(!outputFolder.exists()) {
                outputFolder.mkdirs();
            }
            this.outputFolder = outputFolder;
        }

        @Override
        public final File createUnzippedFile(ZipEntry zipEntry) {
            String relativePath = createRelativePath(zipEntry);
            return new File(outputFolder, relativePath);
        }

        protected String createRelativePath(ZipEntry zipEntry) {
            return zipEntry.getName();
        }
    }

    public static final class StripRootDirectoryZipFileFactory extends DirectoryZipFileFactory {

        public StripRootDirectoryZipFileFactory(File outputFolder) {
            super(outputFolder);
        }

        @Override
        protected String createRelativePath(ZipEntry zipEntry) {
            return zipEntry.getName().replaceFirst(".*?(/|\\\\)","");
        }
    }
}
