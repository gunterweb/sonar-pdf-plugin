/*
 * SonarQube PDF Report
 * Copyright (C) 2010-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.report.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.report.pdf.builder.ProjectBuilder;
import org.sonar.report.pdf.entity.Project;
import org.sonar.report.pdf.entity.exception.ReportException;
import org.sonar.report.pdf.util.Credentials;
import org.sonar.report.pdf.util.MetricKeys;
import org.sonarqube.ws.client.WSClient;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;

/**
 * This is the superclass of concrete reporters. It provides the access to Sonar
 * data (project, measures, graphics) and report config data.
 * 
 * The concrete reporter class will provide: sonar base URL, logo (it will be
 * used in yhe PDF document), the project key and the implementation of
 * printPdfBody method.
 */
public abstract class PDFReporter implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -923944005149556486L;

    private static final Logger LOG = LoggerFactory.getLogger(PDFReporter.class);

    private Credentials credentials;

    private Project project = null;

    public PDFReporter(final Credentials credentials) {
        this.credentials = credentials;
    }

    /**
     * Get Report
     * 
     * @return ByteArrayOutputStream
     * @throws ReportException
     *             ReportException
     */
    public ByteArrayOutputStream getReport() throws ReportException {
        // Creation of documents
        Document mainDocument = new Document(PageSize.A4, 50, 50, 110, 50);
        Toc tocDocument = new Toc();
        Document frontPageDocument = new Document(PageSize.A4, 50, 50, 110, 50);
        ByteArrayOutputStream mainDocumentBaos = new ByteArrayOutputStream();
        ByteArrayOutputStream frontPageDocumentBaos = new ByteArrayOutputStream();
        PdfWriter mainDocumentWriter = null;
        PdfWriter frontPageDocumentWriter = null;
        try {
            mainDocumentWriter = PdfWriter.getInstance(mainDocument, mainDocumentBaos);
            frontPageDocumentWriter = PdfWriter.getInstance(frontPageDocument, frontPageDocumentBaos);
        } catch (DocumentException e) {
            throw new ReportException("Error instantiating PDFWriters", e);
        }

        // Events for TOC, header and pages numbers
        Events events = new Events(tocDocument, new Header(this.getLogo(), this.getProject()));
        mainDocumentWriter.setPageEvent(events);

        mainDocument.open();
        tocDocument.getTocDocument().open();
        frontPageDocument.open();

        LOG.info("Generating PDF report...");
        printFrontPage(frontPageDocument, frontPageDocumentWriter);
        printTocTitle(tocDocument);
        printPdfBody(mainDocument);
        mainDocument.close();
        tocDocument.getTocDocument().close();
        frontPageDocument.close();

        // Return the final document (with TOC)
        return createFinalReport(tocDocument, mainDocumentBaos, frontPageDocumentBaos);
    }

    /**
     * Create final report
     * 
     * @param tocDocument
     *            TOC
     * @param mainDocumentBaos
     *            main document
     * @param frontPageDocumentBaos
     *            front page
     * @return ByteArrayOutputStream
     * @throws ReportException
     *             ReportException
     */
    private ByteArrayOutputStream createFinalReport(Toc tocDocument, ByteArrayOutputStream mainDocumentBaos,
            ByteArrayOutputStream frontPageDocumentBaos) throws ReportException {
        ByteArrayOutputStream finalBaos = new ByteArrayOutputStream();
        try {
            // Get Readers
            PdfReader mainDocumentReader = new PdfReader(mainDocumentBaos.toByteArray());
            PdfReader tocDocumentReader = new PdfReader(tocDocument.getTocOutputStream().toByteArray());
            PdfReader frontPageDocumentReader = new PdfReader(frontPageDocumentBaos.toByteArray());

            // New document
            Document documentWithToc = new Document(tocDocumentReader.getPageSizeWithRotation(1));
            PdfCopy copy = new PdfCopy(documentWithToc, finalBaos);
            documentWithToc.open();
            copy.addPage(copy.getImportedPage(frontPageDocumentReader, 1));
            for (int i = 1; i <= tocDocumentReader.getNumberOfPages(); i++) {
                copy.addPage(copy.getImportedPage(tocDocumentReader, i));
            }
            for (int i = 1; i <= mainDocumentReader.getNumberOfPages(); i++) {
                copy.addPage(copy.getImportedPage(mainDocumentReader, i));
            }
            documentWithToc.close();
        } catch (IOException | DocumentException e) {
            throw new ReportException("Error creating final report", e);
        }
        return finalBaos;
    }

    /**
     * Gets current project
     * 
     * @return Project
     * @throws ReportException
     *             ReportException
     */
    public Project getProject() throws ReportException {
        if (project == null) {
            WSClient sonar = WSClient.create(credentials.getUrl(), credentials.getUsername(),
                    credentials.getPassword());
            ProjectBuilder projectBuilder = ProjectBuilder.getInstance(sonar);
            project = projectBuilder.initializeProject(getProjectKey());
        }
        return project;
    }

    /**
     * Gets complexity distribution
     * 
     * @param project
     *            project
     * @return Image
     */
    protected Image getCCNDistribution(final Project project) {
        String data;
        if (project.getMeasure(MetricKeys.FILE_COMPLEXITY_DISTRIBUTION).getDataValue() != null) {
            data = project.getMeasure(MetricKeys.FILE_COMPLEXITY_DISTRIBUTION).getDataValue();
        } else {
            return null;
        }
        // not usable from 6.0 version
        /*
        ComplexityDistributionBuilder complexityDistributionBuilder = ComplexityDistributionBuilder
                .getInstance(credentials.getUrl());
        ComplexityDistribution ccnDist = new ComplexityDistribution(data);
        return complexityDistributionBuilder.getGraphic(ccnDist);
        */
        return null;
    }

    public String getTextProperty(final String key) {
        return getLangProperties().getProperty(key);
    }

    public String getConfigProperty(final String key) {
        return getReportProperties().getProperty(key);
    }

    /**
     * Gets image from tendency
     * 
     * @param tendencyQualitative
     *            qualitative tendency
     * @param tendencyQuantitative
     *            quantitative tendency
     * @return Image
     */
    protected Image getTendencyImage(final int trend, boolean okWhenGrows) {
        // tendency parameters are t_qual and t_quant tags returned by
        // webservices api
        String iconName;
        if (okWhenGrows) {
            iconName = defineIconForIncreasingAwaitedTendency(trend);
        } else {
            iconName = defineIconForDecreasingAwaitedTendency(trend);
        }
        Image tendencyImage = null;
        try {
            tendencyImage = Image.getInstance(this.getClass().getResource(PDFResources.TENDENCY_DIR + iconName));
        } catch (BadElementException | IOException e) {
            LOG.error("Can not generate tendency image", e);
        }
        return tendencyImage;
    }

    private String defineIconForIncreasingAwaitedTendency(final int trend) {
        String iconName;
        switch (trend) {
        case -1:
            iconName = "-1-red.png";
            break;
        case 1:
            iconName = "1-green.png";
            break;
        default:
            iconName = "none.png";
        }
        return iconName;
    }

    private String defineIconForDecreasingAwaitedTendency(final int trend) {
        String iconName;
        switch (trend) {
        case -1:
            iconName = "-1-green.png";
            break;
        case 1:
            iconName = "1-red.png";
            break;
        default:
            iconName = "none.png";
        }
        return iconName;
    }

    /**
     * Print PDF body
     * 
     * @param document
     *            document
     * @throws ReportException
     *             ReportException
     */
    protected abstract void printPdfBody(Document document) throws ReportException;

    /**
     * Pring TOC
     * 
     * @param document
     *            document
     * @throws ReportException
     *             ReportException
     */
    protected abstract void printTocTitle(Toc document) throws ReportException;

    /**
     * Get Logo
     * 
     * @return URL
     * @throws ReportException
     *             ReportException
     */
    protected abstract URL getLogo();

    /**
     * Get project key
     * 
     * @return String
     */
    protected abstract String getProjectKey();

    /**
     * Pring front page
     * 
     * @param frontPageDocument
     *            front page document
     * @param frontPageWriter
     *            front page writer
     * @throws ReportException
     *             ReportException
     */
    protected abstract void printFrontPage(Document frontPageDocument, PdfWriter frontPageWriter)
            throws ReportException;

    /**
     * Get report properties
     * 
     * @return Properties
     */
    protected abstract Properties getReportProperties();

    /**
     * Get lang properties
     * 
     * @return Properties
     */
    protected abstract Properties getLangProperties();

    public abstract String getReportType();

}
