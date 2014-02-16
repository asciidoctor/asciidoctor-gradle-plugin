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
package org.asciidoctor.gradle;


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.cli.InputHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.Map;
import java.util.Vector;

/**
 * @author Rob Winch
 */
public class FopubFacade {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    private String docbookXmlUrl = "http://maven-us.nuxeo.org/nexus/content/repositories/public/docbook/docbook-xml/4.5/docbook-xml-4.5.jar";
    private String docbookXslUrl = "http://downloads.sourceforge.net/project/docbook/docbook-xsl-ns/1.78.1/docbook-xsl-ns-1.78.1.zip";
    private File downloadCacheDir = new File(System.getProperty("user.home"), ".fopub/downloads");

    private File imagesSource = new File("src/asciidoc/images");

    /**
     *
     * @param sourceDocument the docbook file that should be converted into a PDF
     * @param workingDir the directory to use for installing FOP, docbook, and the xsl files
     * @param distDir the directory to write the PDF to
     * @throws Exception
     */
    public void renderPdf(File sourceDocument, File workingDir, File distDir, FopubOptions options) throws Exception {
        setupWorkingDir(workingDir);

        Vector params = createParams(workingDir, options.getParams());

        String fileNamePattern = "\\..*";
        String documentName = sourceDocument.getName();
        File outputFile = new File(distDir, documentName.replaceAll(fileNamePattern, ".pdf"));
        File docbookFile = new File(distDir,documentName.replaceAll(fileNamePattern,".xml"));
        File xsltFile;
        if (options.getXsltFile()!=null) {
            xsltFile = options.getXsltFile();
        } else {
            xsltFile = new File(workingDir, "docbook-xsl/fo-pdf.xsl");
        }

        FopFactory fopFactory = FopFactory.newInstance(); // Reuse the FopFactory if possible!
        fopFactory.setUserConfig(new File(workingDir, "docbook-xsl/fop-config.xml"));
        // do the following for each new rendering run
        FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
        foUserAgent.setOutputFile(outputFile);

        InputHandler handler = new InputHandler(docbookFile, xsltFile, params);
        handler.createCatalogResolver(foUserAgent);

        OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile));
        try {
            handler.renderTo(foUserAgent, MimeConstants.MIME_PDF, out);
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

    public void setDocbookXmlUrl(String docbookXmlUrl) {
        this.docbookXmlUrl = docbookXmlUrl;
    }

    public void setDocbookXslUrl(String docbookXslUrl) {
        this.docbookXslUrl = docbookXslUrl;
    }

    public void setDownloadCacheDir(File downloadCacheDir) {
        this.downloadCacheDir = downloadCacheDir;
    }

    public void setImagesSource(File imagesSource) {
        this.imagesSource = imagesSource;
    }

    private void setupWorkingDir(File workingDir) throws IOException {
        workingDir.mkdirs();

        File docbookXmlFile = downloadFile(docbookXmlUrl);
        File docbookXslFile = downloadFile(docbookXslUrl);

        if(imagesSource.exists()) {
            FileUtils.copyDirectory(imagesSource, workingDir);
        }

        File outputFolder = new File(workingDir, "docbook");
        ZipUtils.unzip(new FileInputStream(docbookXmlFile), outputFolder);
        ZipUtils.unzip(new FileInputStream(docbookXslFile), new ZipUtils.StripRootDirectoryZipFileFactory(outputFolder));

        unzipDockbookXsl(workingDir);
    }

    private Vector createParams(File workingDir, Map<Object, Object> options) {
        String outputUri = workingDir.toURI().toASCIIString();

        Vector params = new Vector();
        params.add("highlight.xslthl.config");
        params.add(outputUri + "docbook-xsl/xslthl-config.xml");
        params.add("admon.graphics.path");
        params.add(outputUri + "docbook/images/");
        params.add("callout.graphics.path");
        params.add(outputUri + "docbook/images/callouts/");
        params.add("img.src.path");
        params.add(outputUri);
        params.add("fop-output-format");
        params.add("application/pdf");
        params.add("fop-version");
        params.add("1.1");
        if (options!=null) {
            for (Object key: options.keySet()) {
                params.add(key.toString());
                params.add(options.get(key).toString());
            }
        }
        return params;
    }

    private void unzipDockbookXsl(File installDir) throws IOException {
        String docbookXslResourceName = "org/apache/fop/cli/docbook-xsl.zip";
        InputStream docbookXslInputStream = getClass().getClassLoader().getResourceAsStream(docbookXslResourceName);
        if (docbookXslInputStream == null) {
            throw new IOException("could not find ${docbookXslResourceName} on the classpath");
        }

        ZipUtils.unzip(docbookXslInputStream, installDir);
    }

    private File downloadFile(String url) throws IOException {
        String[] urlParts = url.split("/");
        File destinationFile = new File(downloadCacheDir, urlParts[urlParts.length - 1]);
        destinationFile.getParentFile().mkdirs();

        if (!destinationFile.exists()) {
            LOGGER.info("Downloading $url to $destinationFile ...");

            IOUtils.copy(new URL(url).openStream(), new FileOutputStream(destinationFile));
        }
        return destinationFile;
    }
}
