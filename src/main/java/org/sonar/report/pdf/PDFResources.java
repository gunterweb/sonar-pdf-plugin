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

/**
 * 
 *
 */
public interface PDFResources {

	public static final String RESOURCE_NAME = "report-texts";
	public static final String TENDENCY_DIR = "/tendency/";
	public static final String FRONT_PAGE_LOGO = "front.page.logo";
	public static final String SONAR_PNG_FILE = "/sonar.png";
	public static final String SONAR_BASE_URL = "sonar.base.url";

	public static final String REPORT_PROPERTIES = "report.properties";

	public static final String WORKBOOK_REPORT_TYPE = "workbook";

	public static final String EXECUTIVE_REPORT_TYPE = "executive";

	public static final String HTTP_PATTERN = "http://";

	public static final String SONAR_PDF_REPORT = "Sonar PDF Report";

	public static final String MAIN_TABLE_OF_CONTENTS = "main.table.of.contents";

	public static final String GENERAL_NO_VIOLATED_FILES = "general.no_violated_files";

	public static final String GENERAL_MOST_VIOLATED_FILES = "general.most_violated_files";

	public static final String GENERAL_NO_VIOLATED_RULES = "general.no_violated_rules";

	public static final String GENERAL_MOST_VIOLATED_RULES = "general.most_violated_rules";

	public static final String GENERAL_NO_COMPLEX_FILES = "general.no_complex_files";

	public static final String GENERAL_MOST_COMPLEX_FILES = "general.most_complex_files";

	public static final String GENERAL_NO_DUPLICATED_FILES = "general.no_duplicated_files";

	public static final String GENERAL_MOST_DUPLICATED_FILES = "general.most_duplicated_files";

	public static final String GENERAL_VIOLATIONS = "general.violations";

	public static final String GENERAL_TECHNICAL_DEBT = "general.technical_debt";

	public static final String GENERAL_CODING_RULES_VIOLATIONS = "general.coding_rules_violations";

	public static final String GENERAL_ERRORS = "general.errors";

	public static final String GENERAL_FAILURES = "general.failures";

	public static final String GENERAL_TEST_SUCCESS = "general.test_success";

	public static final String GENERAL_TESTS = "general.tests";

	public static final String GENERAL_COVERAGE = "general.coverage";

	public static final String GENERAL_CODE_COVERAGE = "general.code_coverage";

	public static final String GENERAL_DYNAMIC_ANALYSIS = "general.dynamic_analysis";

	public static final String GENERAL_DECISION_POINTS = "general.decisionPoints";

	public static final String GENERAL_PER_CLASS = "general.perClass";

	public static final String GENERAL_COMPLEXITY = "general.complexity";

	public static final String GENERAL_COMMENT_LINES = "general.commentLines";

	public static final String GENERAL_COMMENTS = "general.comments";

	public static final String GENERAL_DUPLICATED_LINES = "general.duplicatedLines";

	public static final String GENERAL_METHODS = "general.methods";

	public static final String GENERAL_CLASSES = "general.classes";

	public static final String GENERAL_PACKAGES = "general.packages";

	public static final String GENERAL_LINES_OF_CODE = "general.lines_of_code";

	public static final String GENERAL_STATIC_ANALYSIS = "general.static_analysis";

	public static final String GENERAL_VIOLATIONS_DASHBOARD = "general.violations_dashboard";

	public static final String GENERAL_VIOLATIONS_ANALYSIS = "general.violations_analysis";

	public static final String GENERAL_REPORT_OVERVIEW = "general.report_overview";

	public static final String MAIN_TEXT_MISC_OVERVIEW = "main.text.misc.overview";

	public static final String GENERAL_VIOLATIONS_DETAILS = "general.violations_details";

	public static final String METRICS_CCN_CLASSES_COUNT_DISTRIBUTION = "metrics.ccn_classes_count_distribution";

	public static final String FILE_SCOPE = "FIL";

	public static final String PROJECT_SCOPE = "PRJ";

	public static final String SONAR_DETAILS_LIMIT = "sonar.details.limit";

	public static final String SONAR_TABLE_LIMIT = "sonar.table.limit";

	public static final String PDF_REPORT_STORE_PATH = "/pdf_report/store";

}
