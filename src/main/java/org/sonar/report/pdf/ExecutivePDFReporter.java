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
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.report.pdf.entity.FileInfo;
import org.sonar.report.pdf.entity.Project;
import org.sonar.report.pdf.entity.Rule;
import org.sonar.report.pdf.entity.exception.ReportException;
import org.sonar.report.pdf.util.Credentials;
import org.sonar.report.pdf.util.MetricKeys;

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
 * 
 *
 */
public class ExecutivePDFReporter extends PDFReporter {

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

	@Override
	protected void printFrontPage(final Document frontPageDocument, final PdfWriter frontPageWriter)
			throws ReportException {
		try {
			URL largeLogo;
			if (super.getConfigProperty(FRONT_PAGE_LOGO).startsWith(HTTP_PATTERN)) {
				largeLogo = new URL(super.getConfigProperty(FRONT_PAGE_LOGO));
			} else {
				largeLogo = this.getClass().getClassLoader().getResource(super.getConfigProperty(FRONT_PAGE_LOGO));
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
			title.addCell(new Phrase(super.getProject().getMeasure(MetricKeys.PROFILE).getDataValue(),
					Style.FRONTPAGE_FONT_3));
			title.addCell(new Phrase(dateRow, Style.FRONTPAGE_FONT_3));
			title.setTotalWidth(pageSize.getWidth() - frontPageDocument.leftMargin() - frontPageDocument.rightMargin());
			title.writeSelectedRows(0, -1, frontPageDocument.leftMargin(), Style.FRONTPAGE_LOGO_POSITION_Y - 150,
					frontPageWriter.getDirectContent());

		} catch (IOException | DocumentException e) {
			LOG.error("Can not generate front page", e);
		}
	}

	@Override
	protected void printPdfBody(final Document document) throws DocumentException, IOException, ReportException {
		Project project = super.getProject();
		// Chapter 1: Report Overview (Parent project)
		ChapterAutoNumber chapter1 = new ChapterAutoNumber(new Paragraph(project.getName(), Style.CHAPTER_FONT));
		chapter1.add(new Paragraph(getTextProperty(MAIN_TEXT_MISC_OVERVIEW), Style.NORMAL_FONT));
		printDetailsForProject(project, chapter1);

		document.add(chapter1);
		for (Project subProject : project.getSubprojects()) {
			ChapterAutoNumber chapterN = new ChapterAutoNumber(new Paragraph(subProject.getName(), Style.CHAPTER_FONT));
			printDetailsForProject(subProject, chapterN);
			document.add(chapterN);
		}
	}

	private void printDetailsForProject(Project subproject, Chapter chapter) throws DocumentException {
		Section sectionN1 = chapter
				.addSection(new Paragraph(getTextProperty(GENERAL_REPORT_OVERVIEW), Style.TITLE_FONT));
		printDashboard(subproject, sectionN1);

		Section sectionN2 = chapter
				.addSection(new Paragraph(getTextProperty(GENERAL_VIOLATIONS_ANALYSIS), Style.TITLE_FONT));
		printMostViolatedRules(subproject, sectionN2);
		printMostViolatedFiles(subproject, sectionN2);
		printMostComplexFiles(subproject, sectionN2);
		printMostDuplicatedFiles(subproject, sectionN2);

		printSpecificData(subproject, chapter);

		printCCNDistribution(subproject, chapter);
	}

	protected void printSpecificData(Project project, Chapter chapter) {
		// nothing to do here; used for inherited class to insert specific data

	}

	protected void printDashboard(final Project project, final Section section) throws DocumentException {

		// Static Analysis
		Paragraph staticAnalysis = new Paragraph(getTextProperty(GENERAL_STATIC_ANALYSIS), Style.UNDERLINED_FONT);
		PdfPTable staticAnalysisTable = new PdfPTable(3);
		staticAnalysisTable.getDefaultCell().setBorderColor(Color.WHITE);

		PdfPTable linesOfCode = new PdfPTable(1);
		Style.noBorderTable(linesOfCode);
		linesOfCode.addCell(new Phrase(getTextProperty(GENERAL_LINES_OF_CODE), Style.DASHBOARD_TITLE_FONT));
		PdfPTable linesOfCodeTendency = new PdfPTable(2);
		Style.noBorderTable(linesOfCodeTendency);
		linesOfCodeTendency.getDefaultCell().setFixedHeight(Style.TENDENCY_ICONS_HEIGHT);
		linesOfCodeTendency
				.addCell(new Phrase(project.getMeasure(MetricKeys.NCLOC).getFormatValue(), Style.DASHBOARD_DATA_FONT));
		linesOfCodeTendency.addCell(getTendencyImage(project.getMeasure(MetricKeys.NCLOC).getQualitativeTendency(),
				project.getMeasure(MetricKeys.NCLOC).getQuantitativeTendency()));

		linesOfCode.addCell(linesOfCodeTendency);
		linesOfCode.addCell(new Phrase(
				project.getMeasure(MetricKeys.DIRECTORIES).getFormatValue() + " " + getTextProperty(GENERAL_PACKAGES),
				Style.DASHBOARD_DATA_FONT_2));
		linesOfCode.addCell(new Phrase(
				project.getMeasure(MetricKeys.CLASSES).getFormatValue() + " " + getTextProperty(GENERAL_CLASSES),
				Style.DASHBOARD_DATA_FONT_2));
		linesOfCode.addCell(new Phrase(
				project.getMeasure(MetricKeys.FUNCTIONS).getFormatValue() + " " + getTextProperty(GENERAL_METHODS),
				Style.DASHBOARD_DATA_FONT_2));
		linesOfCode.addCell(new Phrase(project.getMeasure(MetricKeys.DUPLICATED_LINES_DENSITY).getFormatValue() + " "
				+ getTextProperty(GENERAL_DUPLICATED_LINES), Style.DASHBOARD_DATA_FONT_2));

		PdfPTable comments = new PdfPTable(1);
		Style.noBorderTable(comments);
		comments.addCell(new Phrase(getTextProperty(GENERAL_COMMENTS), Style.DASHBOARD_TITLE_FONT));
		PdfPTable commentsTendency = new PdfPTable(2);
		commentsTendency.getDefaultCell().setFixedHeight(Style.TENDENCY_ICONS_HEIGHT);
		Style.noBorderTable(commentsTendency);
		commentsTendency.addCell(new Phrase(project.getMeasure(MetricKeys.COMMENT_LINES_DENSITY).getFormatValue(),
				Style.DASHBOARD_DATA_FONT));
		commentsTendency
				.addCell(getTendencyImage(project.getMeasure(MetricKeys.COMMENT_LINES_DENSITY).getQualitativeTendency(),
						project.getMeasure(MetricKeys.COMMENT_LINES_DENSITY).getQuantitativeTendency()));
		comments.addCell(commentsTendency);
		comments.addCell(new Phrase(project.getMeasure(MetricKeys.COMMENT_LINES).getFormatValue() + " "
				+ getTextProperty(GENERAL_COMMENT_LINES), Style.DASHBOARD_DATA_FONT_2));

		PdfPTable complexity = new PdfPTable(1);
		Style.noBorderTable(complexity);
		complexity.addCell(new Phrase(getTextProperty(GENERAL_COMPLEXITY), Style.DASHBOARD_TITLE_FONT));
		PdfPTable complexityTendency = new PdfPTable(2);
		complexityTendency.getDefaultCell().setFixedHeight(Style.TENDENCY_ICONS_HEIGHT);
		Style.noBorderTable(complexityTendency);
		complexityTendency.addCell(new Phrase(project.getMeasure(MetricKeys.FUNCTION_COMPLEXITY).getFormatValue(),
				Style.DASHBOARD_DATA_FONT));
		complexityTendency
				.addCell(getTendencyImage(project.getMeasure(MetricKeys.FUNCTION_COMPLEXITY).getQualitativeTendency(),
						project.getMeasure(MetricKeys.FUNCTION_COMPLEXITY).getQuantitativeTendency()));
		complexity.addCell(complexityTendency);
		complexity.addCell(new Phrase(project.getMeasure(MetricKeys.CLASS_COMPLEXITY).getFormatValue() + " "
				+ getTextProperty(GENERAL_PER_CLASS), Style.DASHBOARD_DATA_FONT_2));
		complexity.addCell(new Phrase(project.getMeasure(MetricKeys.COMPLEXITY).getFormatValue() + " "
				+ getTextProperty(GENERAL_DECISION_POINTS), Style.DASHBOARD_DATA_FONT_2));

		staticAnalysisTable.setSpacingBefore(10);
		staticAnalysisTable.addCell(linesOfCode);
		staticAnalysisTable.addCell(comments);
		staticAnalysisTable.addCell(complexity);
		staticAnalysisTable.setSpacingAfter(20);

		// Dynamic Analysis
		Paragraph dynamicAnalysis = new Paragraph(getTextProperty(GENERAL_DYNAMIC_ANALYSIS), Style.UNDERLINED_FONT);
		PdfPTable dynamicAnalysisTable = new PdfPTable(3);
		Style.noBorderTable(dynamicAnalysisTable);

		PdfPTable codeCoverage = new PdfPTable(1);
		Style.noBorderTable(codeCoverage);
		codeCoverage.addCell(new Phrase(getTextProperty(GENERAL_CODE_COVERAGE), Style.DASHBOARD_TITLE_FONT));
		PdfPTable codeCoverageTendency = new PdfPTable(2);
		Style.noBorderTable(codeCoverageTendency);
		codeCoverageTendency.getDefaultCell().setFixedHeight(Style.TENDENCY_ICONS_HEIGHT);
		codeCoverageTendency.addCell(new Phrase(
				project.getMeasure(MetricKeys.COVERAGE).getFormatValue() + " " + getTextProperty(GENERAL_COVERAGE),
				Style.DASHBOARD_DATA_FONT));
		codeCoverageTendency.addCell(getTendencyImage(project.getMeasure(MetricKeys.COVERAGE).getQualitativeTendency(),
				project.getMeasure(MetricKeys.COVERAGE).getQuantitativeTendency()));
		codeCoverage.addCell(codeCoverageTendency);
		codeCoverage.addCell(
				new Phrase(project.getMeasure(MetricKeys.TESTS).getFormatValue() + " " + getTextProperty(GENERAL_TESTS),
						Style.DASHBOARD_DATA_FONT_2));

		PdfPTable testSuccess = new PdfPTable(1);
		Style.noBorderTable(testSuccess);
		testSuccess.addCell(new Phrase(getTextProperty(GENERAL_TEST_SUCCESS), Style.DASHBOARD_TITLE_FONT));
		PdfPTable testSuccessTendency = new PdfPTable(2);
		Style.noBorderTable(testSuccessTendency);
		testSuccessTendency.getDefaultCell().setFixedHeight(Style.TENDENCY_ICONS_HEIGHT);
		testSuccessTendency.addCell(new Phrase(project.getMeasure(MetricKeys.TEST_SUCCESS_DENSITY).getFormatValue(),
				Style.DASHBOARD_DATA_FONT));
		testSuccessTendency
				.addCell(getTendencyImage(project.getMeasure(MetricKeys.TEST_SUCCESS_DENSITY).getQualitativeTendency(),
						project.getMeasure(MetricKeys.TEST_SUCCESS_DENSITY).getQuantitativeTendency()));
		testSuccess.addCell(testSuccessTendency);
		testSuccess.addCell(new Phrase(
				project.getMeasure(MetricKeys.TEST_FAILURES).getFormatValue() + " " + getTextProperty(GENERAL_FAILURES),
				Style.DASHBOARD_DATA_FONT_2));
		testSuccess.addCell(new Phrase(
				project.getMeasure(MetricKeys.TEST_ERRORS).getFormatValue() + " " + getTextProperty(GENERAL_ERRORS),
				Style.DASHBOARD_DATA_FONT_2));

		dynamicAnalysisTable.setSpacingBefore(10);
		dynamicAnalysisTable.addCell(codeCoverage);
		dynamicAnalysisTable.addCell(testSuccess);
		dynamicAnalysisTable.addCell("");
		dynamicAnalysisTable.setSpacingAfter(20);

		Paragraph codingRulesViolations = new Paragraph(getTextProperty(GENERAL_CODING_RULES_VIOLATIONS),
				Style.UNDERLINED_FONT);
		PdfPTable codingRulesViolationsTable = new PdfPTable(3);
		Style.noBorderTable(codingRulesViolationsTable);

		PdfPTable technicalDebt = new PdfPTable(1);
		Style.noBorderTable(technicalDebt);
		technicalDebt.addCell(new Phrase(getTextProperty(GENERAL_TECHNICAL_DEBT), Style.DASHBOARD_TITLE_FONT));
		PdfPTable technicalDebtTendency = new PdfPTable(2);
		Style.noBorderTable(technicalDebtTendency);
		technicalDebtTendency.getDefaultCell().setFixedHeight(Style.TENDENCY_ICONS_HEIGHT);
		technicalDebtTendency.addCell(
				new Phrase(project.getMeasure(MetricKeys.TECHNICAL_DEBT).getFormatValue(), Style.DASHBOARD_DATA_FONT));

		// Workarround for avoid resizing
		Image tendencyTechnicalDebtResize = getTendencyImage(
				project.getMeasure(MetricKeys.TECHNICAL_DEBT).getQualitativeTendency(),
				project.getMeasure(MetricKeys.TECHNICAL_DEBT).getQuantitativeTendency());
		tendencyTechnicalDebtResize.scaleAbsolute(Style.TENDENCY_ICONS_HEIGHT, Style.TENDENCY_ICONS_HEIGHT);
		PdfPCell tendencyRulesCell = new PdfPCell(tendencyTechnicalDebtResize);
		tendencyRulesCell.setBorder(0);
		technicalDebtTendency.addCell(tendencyRulesCell);
		technicalDebt.addCell(technicalDebtTendency);

		PdfPTable violations = new PdfPTable(1);
		Style.noBorderTable(violations);
		violations.addCell(new Phrase(getTextProperty(GENERAL_VIOLATIONS), Style.DASHBOARD_TITLE_FONT));
		PdfPTable violationsTendency = new PdfPTable(2);
		Style.noBorderTable(violationsTendency);
		violationsTendency.getDefaultCell().setFixedHeight(Style.TENDENCY_ICONS_HEIGHT);
		violationsTendency.addCell(
				new Phrase(project.getMeasure(MetricKeys.VIOLATIONS).getFormatValue(), Style.DASHBOARD_DATA_FONT));

		// Workarround for avoid resizing
		Image tendencyResize = getTendencyImage(project.getMeasure(MetricKeys.VIOLATIONS).getQualitativeTendency(),
				project.getMeasure(MetricKeys.VIOLATIONS).getQuantitativeTendency());
		tendencyResize.scaleAbsolute(Style.TENDENCY_ICONS_HEIGHT, Style.TENDENCY_ICONS_HEIGHT);
		PdfPCell tendencyCell = new PdfPCell(tendencyResize);
		tendencyCell.setBorder(0);
		violationsTendency.addCell(tendencyCell);

		violations.addCell(violationsTendency);

		codingRulesViolationsTable.setSpacingBefore(10);
		codingRulesViolationsTable.addCell(technicalDebt);
		codingRulesViolationsTable.addCell(violations);
		codingRulesViolationsTable.addCell("");
		codingRulesViolationsTable.setSpacingAfter(20);

		section.add(Chunk.NEWLINE);
		section.add(staticAnalysis);
		section.add(staticAnalysisTable);
		section.add(dynamicAnalysis);
		section.add(dynamicAnalysisTable);
		section.add(codingRulesViolations);
		section.add(codingRulesViolationsTable);
	}

	protected void printMostDuplicatedFiles(final Project project, final Section section) {
		List<FileInfo> files = project.getMostDuplicatedFiles();
		List<String> left = new LinkedList<>();
		List<String> right = new LinkedList<>();
		for (FileInfo file : files) {
			left.add(file.getName());
			right.add(file.getDuplicatedLines());
		}

		PdfPTable mostDuplicatedFilesTable = Style.createSimpleTable(left, right,
				getTextProperty(GENERAL_MOST_DUPLICATED_FILES), getTextProperty(GENERAL_NO_DUPLICATED_FILES));
		section.add(mostDuplicatedFilesTable);
	}

	protected void printMostComplexFiles(final Project project, final Section section) {
		List<FileInfo> files = project.getMostComplexFiles();
		List<String> left = new LinkedList<>();
		List<String> right = new LinkedList<>();
		for (FileInfo file : files) {
			left.add(file.getName());
			right.add(file.getComplexity());
		}
		PdfPTable mostComplexFilesTable = Style.createSimpleTable(left, right,
				getTextProperty(GENERAL_MOST_COMPLEX_FILES), getTextProperty(GENERAL_NO_COMPLEX_FILES));
		section.add(mostComplexFilesTable);
	}

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
			colors.add(rule.getSeverity().toColor());
			limit++;
		}

		PdfPTable mostViolatedRulesTable = Style.createSimpleTable(left, right, colors,
				getTextProperty(GENERAL_MOST_VIOLATED_RULES), getTextProperty(GENERAL_NO_VIOLATED_RULES));
		section.add(mostViolatedRulesTable);
	}

	protected void printMostViolatedFiles(final Project project, final Section section) {
		List<FileInfo> files = project.getMostViolatedFiles();
		List<String> left = new LinkedList<>();
		List<String> right = new LinkedList<>();
		for (FileInfo file : files) {
			left.add(file.getName());
			right.add(file.getViolations());
		}

		PdfPTable mostViolatedFilesTable = Style.createSimpleTable(left, right,
				getTextProperty(GENERAL_MOST_VIOLATED_FILES), getTextProperty(GENERAL_NO_VIOLATED_FILES));
		section.add(mostViolatedFilesTable);
	}

	@Override
	protected void printTocTitle(final Toc tocDocument) throws com.lowagie.text.DocumentException {
		Paragraph tocTitle = new Paragraph(super.getTextProperty(MAIN_TABLE_OF_CONTENTS), Style.TOC_TITLE_FONT);
		tocTitle.setAlignment(Element.ALIGN_CENTER);
		tocDocument.getTocDocument().add(tocTitle);
		tocDocument.getTocDocument().add(Chunk.NEWLINE);
	}

	private void printCCNDistribution(Project project, Chapter chapter) {
		Image ccnDistGraph = getCCNDistribution(project);
		if (ccnDistGraph != null) {
			Section section = chapter
					.addSection(new Paragraph(getTextProperty(GENERAL_VIOLATIONS_DASHBOARD), Style.TITLE_FONT));
			section.add(ccnDistGraph);
			Paragraph imageFoot = new Paragraph(getTextProperty(METRICS_CCN_CLASSES_COUNT_DISTRIBUTION),
					Style.FOOT_FONT);
			imageFoot.setAlignment(Paragraph.ALIGN_CENTER);
			section.add(imageFoot);
		}

	}

	@Override
	public String getReportType() {
		return EXECUTIVE_REPORT_TYPE;
	}
}
