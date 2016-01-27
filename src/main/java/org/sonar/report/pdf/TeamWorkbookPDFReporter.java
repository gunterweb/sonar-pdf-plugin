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
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.sonar.report.pdf.entity.Project;
import org.sonar.report.pdf.entity.Rule;
import org.sonar.report.pdf.entity.Violation;
import org.sonar.report.pdf.entity.exception.ReportException;
import org.sonar.report.pdf.util.Credentials;

import com.lowagie.text.Chapter;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Section;
import com.lowagie.text.pdf.PdfPTable;

/**
 * 
 * Workbook for Team PDF Reporter
 */
public class TeamWorkbookPDFReporter extends ExecutivePDFReporter {

    /**
     * 
     */
    private static final long serialVersionUID = 4994742577755351762L;

    public TeamWorkbookPDFReporter(final Credentials credentials, final URL logo, final String projectKey,
            final Properties configProperties, final Properties langProperties) {
        super(credentials, logo, projectKey, configProperties, langProperties);
    }

    /**
     * @see org.sonar.report.pdf.ExecutivePDFReporter#printSpecificData(org.sonar.report.pdf.entity.Project,
     *      com.lowagie.text.Chapter)
     */
    @Override
    protected void printSpecificData(Project project, Chapter chapter) throws ReportException {
        printMostViolatedRulesDetails(project, chapter);
    }

    /**
     * Print details for most violated rules
     * 
     * @param project
     *            current project
     * @param chapter
     *            current chapter
     */
    private void printMostViolatedRulesDetails(final Project project, final Chapter chapter) {
        if (project.getMostViolatedRules() != null && !project.getMostViolatedRules().isEmpty()) {
            Section section = chapter.addSection(
                    new Paragraph(getTextProperty(PDFResources.GENERAL_VIOLATIONS_DETAILS), Style.TITLE_FONT));
            for (Rule rule : project.getMostViolatedRules()) {
                List<String> files = new LinkedList<>();
                List<String> lines = new LinkedList<>();
                addViolation(rule, files, lines);
                section.add(createViolationsDetailedTable(rule.getName(), files, lines));
            }
        }
    }

    /**
     * Add violation
     * 
     * @param rule
     *            rule
     * @param files
     *            files
     * @param files
     *            files
     */
    private void addViolation(Rule rule, List<String> files, List<String> lines) {
        if (rule.getTopViolations() != null) {
            for (Violation violation : rule.getTopViolations()) {
                String[] components = violation.getResource().split("/");
                files.add(components[components.length - 1]);
                lines.add(violation.getLine());
            }
        }
    }

    /**
     * Create table for violation details
     * 
     * @param ruleName
     *            name of the rules
     * @param files
     *            violated files
     * @param lines
     *            violated lines
     * @return The table (iText table) ready to add to the document
     */
    private PdfPTable createViolationsDetailedTable(final String ruleName, final List<String> files,
            final List<String> lines) {

        PdfPTable table = new PdfPTable(10);
        table.getDefaultCell().setColspan(1);
        table.getDefaultCell().setBackgroundColor(new Color(255, 228, 181));
        table.addCell(new Phrase(getTextProperty(PDFResources.GENERAL_RULE), Style.NORMAL_FONT));
        table.getDefaultCell().setColspan(9);
        table.getDefaultCell().setBackgroundColor(Color.WHITE);
        table.addCell(new Phrase(ruleName, Style.NORMAL_FONT));
        table.getDefaultCell().setColspan(10);
        table.getDefaultCell().setBackgroundColor(Color.GRAY);
        table.addCell("");
        table.getDefaultCell().setColspan(7);
        table.getDefaultCell().setBackgroundColor(new Color(255, 228, 181));
        table.addCell(new Phrase(getTextProperty(PDFResources.GENERAL_FILE), Style.NORMAL_FONT));
        table.getDefaultCell().setColspan(3);
        table.addCell(new Phrase(getTextProperty(PDFResources.GENERAL_LINE), Style.NORMAL_FONT));
        table.getDefaultCell().setBackgroundColor(Color.WHITE);

        int i = 0;
        String lineNumbers = "";
        if (!files.isEmpty()) {
            while (i < files.size() - 1) {
                if (("").equals(lineNumbers)) {
                    lineNumbers += lines.get(i);
                } else {
                    lineNumbers += ", " + lines.get(i);
                }

                if (!files.get(i).equals(files.get(i + 1))) {
                    table.getDefaultCell().setColspan(7);
                    table.addCell(files.get(i));
                    table.getDefaultCell().setColspan(3);
                    table.addCell(lineNumbers);
                    lineNumbers = "";
                }
                i++;
            }
        }

        if (!files.isEmpty()) {
            table.getDefaultCell().setColspan(7);
            table.addCell(files.get(files.size() - 1));
            table.getDefaultCell().setColspan(3);
            if (("").equals(lineNumbers)) {
                lineNumbers += lines.get(i);
            } else {
                lineNumbers += ", " + lines.get(lines.size() - 1);
            }
            table.addCell(lineNumbers);
        }

        table.setSpacingBefore(20);
        table.setSpacingAfter(20);
        table.setLockedWidth(false);
        table.setWidthPercentage(90);
        return table;
    }

    /**
     * @see org.sonar.report.pdf.ExecutivePDFReporter#getReportType()
     */
    @Override
    public String getReportType() {
        return PDFResources.WORKBOOK_REPORT_TYPE;
    }
}