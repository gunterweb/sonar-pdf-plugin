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

import java.awt.Color;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.report.pdf.entity.FileInfo;
import org.sonar.report.pdf.entity.Project;
import org.sonar.report.pdf.entity.Rule;
import org.sonar.report.pdf.entity.exception.ReportException;
import org.sonar.report.pdf.util.Credentials;
import org.sonar.report.pdf.util.MetricKeys;
import org.sonarqube.ws.client.JdkUtils;

import com.lowagie.text.Chapter;
import com.lowagie.text.ChapterAutoNumber;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.Section;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Executive PDF Reporter
 *
 */
public class ExecutivePDFReporter extends PDFReporter {

    /**
     * 
     */
    private static final long serialVersionUID = -5378403769337739685L;

    private static final String QUALITY_PROFILE_NAME = "name";

    private static final Logger LOG = LoggerFactory.getLogger(ExecutivePDFReporter.class);

    private URL logo;
    private String projectKey;
    private Properties configProperties;
    private Properties langProperties;

    public ExecutivePDFReporter(final Credentials credentials, final URL logo, final String projectKey,
            final Properties configProperties, final Properties langProperties) {
        super(credentials);
        this.logo = logo;
        this.projectKey = projectKey;
        this.configProperties = configProperties;
        this.langProperties = langProperties;
    }

    @Override
    protected URL getLogo() {
        return this.logo;
    }

    @Override
    protected String getProjectKey() {
        return this.projectKey;
    }

    @Override
    protected Properties getLangProperties() {
        return langProperties;
    }

    @Override
    protected Properties getReportProperties() {
        return configProperties;
    }

    /**
     * @see org.sonar.report.pdf.PDFReporter#printFrontPage(com.lowagie.text.Document,
     *      com.lowagie.text.pdf.PdfWriter)
     */
    @Override
    protected void printFrontPage(final Document frontPageDocument, final PdfWriter frontPageWriter)
            throws ReportException {
        try {
            URL largeLogo;
            if (super.getConfigProperty(PDFResources.FRONT_PAGE_LOGO).startsWith(PDFResources.HTTP_PATTERN)) {
                largeLogo = new URL(super.getConfigProperty(PDFResources.FRONT_PAGE_LOGO));
            } else {
                largeLogo = this.getClass().getClassLoader()
                        .getResource(super.getConfigProperty(PDFResources.FRONT_PAGE_LOGO));
            }
            Image logoImage = Image.getInstance(largeLogo);
            logoImage.scaleAbsolute(360, 200);
            Rectangle pageSize = frontPageDocument.getPageSize();
            logoImage.setAbsolutePosition(Style.FRONTPAGE_LOGO_POSITION_X, Style.FRONTPAGE_LOGO_POSITION_Y);
            frontPageDocument.add(logoImage);

            PdfPTable title = new PdfPTable(1);
            title.getDefaultCell().setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
            title.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            String projectRow = super.getProject().getName();
            String versionRow = super.getProject().getMeasures().getVersion();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            String dateRow = df.format(super.getProject().getMeasures().getDate());
            String descriptionRow = super.getProject().getDescription();

            title.addCell(new Phrase(projectRow, Style.FRONTPAGE_FONT_1));
            title.addCell(new Phrase(versionRow, Style.FRONTPAGE_FONT_1));
            title.addCell(new Phrase(descriptionRow, Style.FRONTPAGE_FONT_2));
            String qualityProfile = super.getProject().getMeasure(MetricKeys.PROFILE).getDataValue();
            JSONParser parser = new JSONParser();
            JSONArray json = (JSONArray) parser.parse(qualityProfile);
            if (!json.isEmpty()) {
                Map<String, String> properties = JdkUtils.getInstance().getFieldsWithValues(json.get(0));
                if (properties.containsKey(QUALITY_PROFILE_NAME)) {
                    title.addCell(new Phrase(properties.get(QUALITY_PROFILE_NAME), Style.FRONTPAGE_FONT_3));
                }

            }
            title.addCell(new Phrase(dateRow, Style.FRONTPAGE_FONT_3));
            title.setTotalWidth(pageSize.getWidth() - frontPageDocument.leftMargin() - frontPageDocument.rightMargin());
            title.writeSelectedRows(0, -1, frontPageDocument.leftMargin(), Style.FRONTPAGE_LOGO_POSITION_Y - 150,
                    frontPageWriter.getDirectContent());

        } catch (IOException | DocumentException | ParseException e) {
            LOG.error("Can not generate front page", e);
        }
    }

    /**
     * @see org.sonar.report.pdf.PDFReporter#printPdfBody(com.lowagie.text.Document)
     */
    @Override
    protected void printPdfBody(final Document document) throws ReportException {
        Project project = super.getProject();
        // Chapter 1: Report Overview (Parent project)
        ChapterAutoNumber chapter1 = new ChapterAutoNumber(new Paragraph(project.getName(), Style.CHAPTER_FONT));
        chapter1.add(new Paragraph(getTextProperty(PDFResources.MAIN_TEXT_MISC_OVERVIEW), Style.NORMAL_FONT));
        printDetailsForProject(project, chapter1);
        try {
            document.add(chapter1);
            for (Project subProject : project.getSubprojects()) {
                ChapterAutoNumber chapterN = new ChapterAutoNumber(
                        new Paragraph(subProject.getName(), Style.CHAPTER_FONT));
                printDetailsForProject(subProject, chapterN);
                document.add(chapterN);
            }
        } catch (DocumentException e) {
            throw new ReportException("Error printing PDF Body", e);
        }

    }

    /**
     * Print details for Project
     * 
     * @param project
     *            project
     * @param chapter
     *            chapter
     * @throws ReportException
     *             ReportException
     */
    private void printDetailsForProject(Project project, Chapter chapter) throws ReportException {
        Section sectionN1 = chapter
                .addSection(new Paragraph(getTextProperty(PDFResources.GENERAL_REPORT_OVERVIEW), Style.TITLE_FONT));
        printDashboard(project, sectionN1);
        Section sectionN2 = chapter
                .addSection(new Paragraph(getTextProperty(PDFResources.GENERAL_VIOLATIONS_ANALYSIS), Style.TITLE_FONT));
        printMostViolatedRules(project, sectionN2);
        printMostViolatedFiles(project, sectionN2);
        printMostComplexFiles(project, sectionN2);
        printMostDuplicatedFiles(project, sectionN2);
        printSpecificData(project, chapter);
        printCCNDistribution(project, chapter);
    }

    /**
     * Print specific data for project (use this to implement specific rendering
     * section)
     * 
     * @param project
     *            project
     * @param chapter
     *            chapter
     * @throws ReportException
     *             ReportException
     */
    protected void printSpecificData(Project project, Chapter chapter) throws ReportException {
        // nothing to do here; used for inherited class to insert specific data

    }

    /**
     * Print dashboard for project
     * 
     * @param project
     *            project
     * @param section
     *            section
     * @throws ReportException
     *             ReportException
     */
    protected void printDashboard(final Project project, final Section section) throws ReportException {
        section.add(Chunk.NEWLINE);
        // Static Analysis
        section.add(createStaticAnalysis(project));
        // Dynamic Analysis
        section.add(createDynamicAnalysis(project));
        // Coding issues analysis
        section.add(createCodingRuleViolations(project));
        // Coding issues details
        section.add(createCodingRuleViolationsDetails(project));
    }

    /**
     * Create rule violations details table
     * 
     * @param project
     *            project
     * @return The table (iText table) ready to add to the document
     */
    private PdfPTable createCodingRuleViolationsDetails(final Project project) {
        PdfPTable detailCodingRulesViolationsTable = new PdfPTable(3);
        Style.noBorderTable(detailCodingRulesViolationsTable);
        detailCodingRulesViolationsTable.setSpacingBefore(10);
        // blocker violations
        detailCodingRulesViolationsTable.addCell(
                createTable(project, PDFResources.GENERAL_BLOCKER_VIOLATIONS, MetricKeys.BLOCKER_VIOLATIONS, false));
        // critical violations
        detailCodingRulesViolationsTable.addCell(
                createTable(project, PDFResources.GENERAL_CRITICAL_VIOLATIONS, MetricKeys.CRITICAL_VIOLATIONS, false));
        // major violations
        detailCodingRulesViolationsTable.addCell(
                createTable(project, PDFResources.GENERAL_MAJOR_VIOLATIONS, MetricKeys.MAJOR_VIOLATIONS, false));
        detailCodingRulesViolationsTable.setSpacingAfter(20);
        return detailCodingRulesViolationsTable;
    }

    /**
     * Create a table for a project, a mesure and a text
     * 
     * @param project
     *            project
     * @param text
     *            text
     * @param measure
     *            measure
     * @return The table (iText table) ready to add to the document
     */
    private PdfPTable createTable(final Project project, String text, MetricKeys measure, boolean okWhenGrows) {
        PdfPTable criticalViolations = new PdfPTable(1);
        Style.noBorderTable(criticalViolations);
        criticalViolations.addCell(new Phrase(getTextProperty(text), Style.DASHBOARD_TITLE_FONT));
        PdfPTable criticalViolationsTendency = new PdfPTable(2);
        Style.noBorderTable(criticalViolationsTendency);
        criticalViolationsTendency.getDefaultCell().setFixedHeight(Style.TENDENCY_ICONS_HEIGHT);
        criticalViolationsTendency
                .addCell(new Phrase(project.getMeasure(measure).getFormatValue(), Style.DASHBOARD_DATA_FONT));
        criticalViolationsTendency
                .addCell(getTendencyImage(project.getMeasure(measure).getQualitativeTendency(), okWhenGrows));

        criticalViolations.addCell(criticalViolationsTendency);
        return criticalViolations;
    }

    /**
     * Create coding rule violations for a project
     * 
     * @param project
     *            project
     * @return The paragraph (iText paragraph) ready to add to the document
     */
    private Paragraph createCodingRuleViolations(final Project project) {
        Paragraph codingRulesViolations = new Paragraph(getTextProperty(PDFResources.GENERAL_CODING_RULES_VIOLATIONS),
                Style.UNDERLINED_FONT);
        PdfPTable codingRulesViolationsTable = new PdfPTable(3);
        Style.noBorderTable(codingRulesViolationsTable);
        codingRulesViolationsTable.setSpacingBefore(10);
        codingRulesViolationsTable
                .addCell(createTable(project, PDFResources.GENERAL_TECHNICAL_DEBT, MetricKeys.TECHNICAL_DEBT, false));
        codingRulesViolationsTable
                .addCell(createTable(project, PDFResources.GENERAL_VIOLATIONS, MetricKeys.VIOLATIONS, false));
        codingRulesViolationsTable.addCell("");
        codingRulesViolationsTable.setSpacingAfter(20);
        codingRulesViolations.add(codingRulesViolationsTable);
        return codingRulesViolations;
    }

    /**
     * Create dynamic analysis content for a project
     * 
     * @param project
     *            project
     * @return The paragraph (iText paragraph) ready to add to the document
     */
    private Paragraph createDynamicAnalysis(final Project project) {
        Paragraph dynamicAnalysis = new Paragraph(getTextProperty(PDFResources.GENERAL_DYNAMIC_ANALYSIS),
                Style.UNDERLINED_FONT);
        PdfPTable dynamicAnalysisTable = new PdfPTable(3);
        Style.noBorderTable(dynamicAnalysisTable);

        PdfPTable codeCoverage = new PdfPTable(1);
        Style.noBorderTable(codeCoverage);
        codeCoverage
                .addCell(new Phrase(getTextProperty(PDFResources.GENERAL_CODE_COVERAGE), Style.DASHBOARD_TITLE_FONT));
        PdfPTable codeCoverageTendency = new PdfPTable(2);
        Style.noBorderTable(codeCoverageTendency);
        codeCoverageTendency.getDefaultCell().setFixedHeight(Style.TENDENCY_ICONS_HEIGHT);
        codeCoverageTendency.addCell(new Phrase(project.getMeasure(MetricKeys.COVERAGE).getFormatValue() + " "
                + getTextProperty(PDFResources.GENERAL_COVERAGE), Style.DASHBOARD_DATA_FONT));
        codeCoverageTendency
                .addCell(getTendencyImage(project.getMeasure(MetricKeys.COVERAGE).getQualitativeTendency(), true));
        codeCoverage.addCell(codeCoverageTendency);
        codeCoverage.addCell(new Phrase(project.getMeasure(MetricKeys.TESTS).getFormatValue() + " "
                + getTextProperty(PDFResources.GENERAL_TESTS), Style.DASHBOARD_DATA_FONT_2));

        PdfPTable testSuccess = new PdfPTable(1);
        Style.noBorderTable(testSuccess);
        testSuccess.addCell(new Phrase(getTextProperty(PDFResources.GENERAL_TEST_SUCCESS), Style.DASHBOARD_TITLE_FONT));
        PdfPTable testSuccessTendency = new PdfPTable(2);
        Style.noBorderTable(testSuccessTendency);
        testSuccessTendency.getDefaultCell().setFixedHeight(Style.TENDENCY_ICONS_HEIGHT);
        testSuccessTendency.addCell(new Phrase(project.getMeasure(MetricKeys.TEST_SUCCESS_DENSITY).getFormatValue(),
                Style.DASHBOARD_DATA_FONT));
        testSuccessTendency.addCell(
                getTendencyImage(project.getMeasure(MetricKeys.TEST_SUCCESS_DENSITY).getQualitativeTendency(), true));
        testSuccess.addCell(testSuccessTendency);
        testSuccess.addCell(new Phrase(project.getMeasure(MetricKeys.TEST_FAILURES).getFormatValue() + " "
                + getTextProperty(PDFResources.GENERAL_FAILURES), Style.DASHBOARD_DATA_FONT_2));
        testSuccess.addCell(new Phrase(project.getMeasure(MetricKeys.TEST_ERRORS).getFormatValue() + " "
                + getTextProperty(PDFResources.GENERAL_ERRORS), Style.DASHBOARD_DATA_FONT_2));

        dynamicAnalysisTable.setSpacingBefore(10);
        dynamicAnalysisTable.addCell(codeCoverage);
        dynamicAnalysisTable.addCell(testSuccess);
        dynamicAnalysisTable.addCell("");
        dynamicAnalysisTable.setSpacingAfter(20);
        dynamicAnalysis.add(dynamicAnalysisTable);
        return dynamicAnalysis;
    }

    /**
     * Create static analysis content for a project
     * 
     * @param project
     *            project
     * @return The paragraph (iText paragraph) ready to add to the document
     */
    private Paragraph createStaticAnalysis(final Project project) {
        Paragraph staticAnalysis = new Paragraph(getTextProperty(PDFResources.GENERAL_STATIC_ANALYSIS),
                Style.UNDERLINED_FONT);
        PdfPTable staticAnalysisTable = new PdfPTable(3);
        staticAnalysisTable.getDefaultCell().setBorderColor(Color.WHITE);

        PdfPTable linesOfCode = new PdfPTable(1);
        Style.noBorderTable(linesOfCode);
        linesOfCode
                .addCell(new Phrase(getTextProperty(PDFResources.GENERAL_LINES_OF_CODE), Style.DASHBOARD_TITLE_FONT));
        PdfPTable linesOfCodeTendency = new PdfPTable(2);
        Style.noBorderTable(linesOfCodeTendency);
        linesOfCodeTendency.getDefaultCell().setFixedHeight(Style.TENDENCY_ICONS_HEIGHT);
        linesOfCodeTendency
                .addCell(new Phrase(project.getMeasure(MetricKeys.NCLOC).getFormatValue(), Style.DASHBOARD_DATA_FONT));
        linesOfCodeTendency.addCell(
                getTendencyImage(project.getMeasure(MetricKeys.DUPLICATED_LINES).getQualitativeTendency(), false));

        linesOfCode.addCell(linesOfCodeTendency);
        linesOfCode.addCell(new Phrase(project.getMeasure(MetricKeys.DIRECTORIES).getFormatValue() + " "
                + getTextProperty(PDFResources.GENERAL_PACKAGES), Style.DASHBOARD_DATA_FONT_2));
        linesOfCode.addCell(new Phrase(project.getMeasure(MetricKeys.CLASSES).getFormatValue() + " "
                + getTextProperty(PDFResources.GENERAL_CLASSES), Style.DASHBOARD_DATA_FONT_2));
        linesOfCode.addCell(new Phrase(project.getMeasure(MetricKeys.FUNCTIONS).getFormatValue() + " "
                + getTextProperty(PDFResources.GENERAL_METHODS), Style.DASHBOARD_DATA_FONT_2));
        linesOfCode.addCell(new Phrase(project.getMeasure(MetricKeys.DUPLICATED_LINES_DENSITY).getFormatValue() + " "
                + getTextProperty(PDFResources.GENERAL_DUPLICATED_LINES), Style.DASHBOARD_DATA_FONT_2));

        PdfPTable comments = new PdfPTable(1);
        Style.noBorderTable(comments);
        comments.addCell(new Phrase(getTextProperty(PDFResources.GENERAL_COMMENTS), Style.DASHBOARD_TITLE_FONT));
        PdfPTable commentsTendency = new PdfPTable(2);
        commentsTendency.getDefaultCell().setFixedHeight(Style.TENDENCY_ICONS_HEIGHT);
        Style.noBorderTable(commentsTendency);
        commentsTendency.addCell(new Phrase(project.getMeasure(MetricKeys.COMMENT_LINES_DENSITY).getFormatValue(),
                Style.DASHBOARD_DATA_FONT));
        commentsTendency.addCell(
                getTendencyImage(project.getMeasure(MetricKeys.COMMENT_LINES_DENSITY).getQualitativeTendency(), true));
        comments.addCell(commentsTendency);
        comments.addCell(new Phrase(project.getMeasure(MetricKeys.COMMENT_LINES).getFormatValue() + " "
                + getTextProperty(PDFResources.GENERAL_COMMENT_LINES), Style.DASHBOARD_DATA_FONT_2));

        PdfPTable complexity = new PdfPTable(1);
        Style.noBorderTable(complexity);
        complexity.addCell(new Phrase(getTextProperty(PDFResources.GENERAL_COMPLEXITY), Style.DASHBOARD_TITLE_FONT));
        PdfPTable complexityTendency = new PdfPTable(2);
        complexityTendency.getDefaultCell().setFixedHeight(Style.TENDENCY_ICONS_HEIGHT);
        Style.noBorderTable(complexityTendency);
        complexityTendency.addCell(new Phrase(project.getMeasure(MetricKeys.FUNCTION_COMPLEXITY).getFormatValue(),
                Style.DASHBOARD_DATA_FONT));
        complexityTendency.addCell(
                getTendencyImage(project.getMeasure(MetricKeys.FUNCTION_COMPLEXITY).getQualitativeTendency(), false));
        complexity.addCell(complexityTendency);
        complexity.addCell(new Phrase(project.getMeasure(MetricKeys.CLASS_COMPLEXITY).getFormatValue() + " "
                + getTextProperty(PDFResources.GENERAL_PER_CLASS), Style.DASHBOARD_DATA_FONT_2));
        complexity.addCell(new Phrase(project.getMeasure(MetricKeys.COMPLEXITY).getFormatValue() + " "
                + getTextProperty(PDFResources.GENERAL_DECISION_POINTS), Style.DASHBOARD_DATA_FONT_2));

        staticAnalysisTable.setSpacingBefore(10);
        staticAnalysisTable.addCell(linesOfCode);
        staticAnalysisTable.addCell(comments);
        staticAnalysisTable.addCell(complexity);
        staticAnalysisTable.setSpacingAfter(20);
        staticAnalysis.add(staticAnalysisTable);
        return staticAnalysis;
    }

    /**
     * Print most duplicated files for a project, and the target section
     * 
     * @param project
     *            project
     * @param section
     *            section
     * @return The paragraph (iText paragraph) ready to add to the document
     */
    protected void printMostDuplicatedFiles(final Project project, final Section section) {
        List<FileInfo> files = project.getMostDuplicatedFiles();
        List<String> left = new LinkedList<>();
        List<String> right = new LinkedList<>();
        for (FileInfo file : files) {
            left.add(file.getName());
            right.add(file.getDuplicatedLines());
        }

        PdfPTable mostDuplicatedFilesTable = Style.createSimpleTable(left, right,
                getTextProperty(PDFResources.GENERAL_MOST_DUPLICATED_FILES),
                getTextProperty(PDFResources.GENERAL_NO_DUPLICATED_FILES));
        section.add(mostDuplicatedFilesTable);
    }

    /**
     * Print most complex files for a project, and the target section
     * 
     * @param project
     *            project
     * @param section
     *            section
     * @return The paragraph (iText paragraph) ready to add to the document
     */
    protected void printMostComplexFiles(final Project project, final Section section) {
        List<FileInfo> files = project.getMostComplexFiles();
        List<String> left = new LinkedList<>();
        List<String> right = new LinkedList<>();
        for (FileInfo file : files) {
            left.add(file.getName());
            right.add(file.getComplexity());
        }
        PdfPTable mostComplexFilesTable = Style.createSimpleTable(left, right,
                getTextProperty(PDFResources.GENERAL_MOST_COMPLEX_FILES),
                getTextProperty(PDFResources.GENERAL_NO_COMPLEX_FILES));
        section.add(mostComplexFilesTable);
    }

    /**
     * Print most violated rules for a project, and the target section
     * 
     * @param project
     *            project
     * @param section
     *            section
     * @return The paragraph (iText paragraph) ready to add to the document
     */
    protected void printMostViolatedRules(final Project project, final Section section) {
        List<Rule> mostViolatedRules = project.getMostViolatedRules();
        Iterator<Rule> it = mostViolatedRules.iterator();
        List<String> left = new LinkedList<>();
        List<String> right = new LinkedList<>();
        List<Color> colors = new LinkedList<>();
        int limit = 0;
        while (it.hasNext() && limit < 5) {
            Rule rule = it.next();
            left.add(rule.getName());
            right.add(rule.getViolationsNumber());
            colors.add(rule.getSeverity().getColor());
            limit++;
        }

        PdfPTable mostViolatedRulesTable = Style.createSimpleTable(left, right, colors,
                getTextProperty(PDFResources.GENERAL_MOST_VIOLATED_RULES),
                getTextProperty(PDFResources.GENERAL_NO_VIOLATED_RULES));
        section.add(mostViolatedRulesTable);
    }

    /**
     * Print most violated files for a project, and the target section
     * 
     * @param project
     *            project
     * @param section
     *            section
     * @return The paragraph (iText paragraph) ready to add to the document
     */
    protected void printMostViolatedFiles(final Project project, final Section section) {
        List<FileInfo> files = project.getMostViolatedFiles();
        List<String> left = new LinkedList<>();
        List<String> right = new LinkedList<>();
        for (FileInfo file : files) {
            left.add(file.getName());
            right.add(file.getViolations());
        }

        PdfPTable mostViolatedFilesTable = Style.createSimpleTable(left, right,
                getTextProperty(PDFResources.GENERAL_MOST_VIOLATED_FILES),
                getTextProperty(PDFResources.GENERAL_NO_VIOLATED_FILES));
        section.add(mostViolatedFilesTable);
    }

    /**
     * @see org.sonar.report.pdf.PDFReporter#printTocTitle(org.sonar.report.pdf.Toc)
     */
    @Override
    protected void printTocTitle(final Toc tocDocument) throws ReportException {
        Paragraph tocTitle = new Paragraph(super.getTextProperty(PDFResources.MAIN_TABLE_OF_CONTENTS),
                Style.TOC_TITLE_FONT);
        tocTitle.setAlignment(Element.ALIGN_CENTER);
        try {
            tocDocument.getTocDocument().add(tocTitle);
            tocDocument.getTocDocument().add(Chunk.NEWLINE);
        } catch (DocumentException e) {
            throw new ReportException("Error printing TOC", e);
        }
    }

    /**
     * Prints the complexity distribution for a project
     * 
     * @param project
     *            project
     * @param chapter
     *            target chapter
     */
    private void printCCNDistribution(Project project, Chapter chapter) {
        Image ccnDistGraph = getCCNDistribution(project);
        if (ccnDistGraph != null) {
            Section section = chapter.addSection(
                    new Paragraph(getTextProperty(PDFResources.GENERAL_VIOLATIONS_DASHBOARD), Style.TITLE_FONT));
            section.add(ccnDistGraph);
            Paragraph imageFoot = new Paragraph(getTextProperty(PDFResources.METRICS_CCN_CLASSES_COUNT_DISTRIBUTION),
                    Style.FOOT_FONT);
            imageFoot.setAlignment(Paragraph.ALIGN_CENTER);
            section.add(imageFoot);
        }

    }

    /**
     * @see org.sonar.report.pdf.PDFReporter#getReportType()
     */
    @Override
    public String getReportType() {
        return PDFResources.EXECUTIVE_REPORT_TYPE;
    }
}
