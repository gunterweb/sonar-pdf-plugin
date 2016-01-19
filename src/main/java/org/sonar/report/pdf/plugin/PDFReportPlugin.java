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
package org.sonar.report.pdf.plugin;

import java.util.Arrays;
import java.util.List;

import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.PropertyType;
import org.sonar.api.SonarPlugin;
import org.sonar.report.pdf.batch.PDFPostJob;

@Properties({
		@Property(key = PDFPostJob.SKIP_PDF_KEY, name = "Skip", description = "Skip generation of PDF report.", defaultValue = ""
				+ PDFPostJob.SKIP_PDF_DEFAULT_VALUE, global = true, project = true, module = false, type = PropertyType.BOOLEAN),
		@Property(key = PDFPostJob.REPORT_TYPE, name = "Type", description = "Report type.", defaultValue = PDFPostJob.REPORT_TYPE_DEFAULT_VALUE, global = true, project = true, module = false, type = PropertyType.SINGLE_SELECT_LIST, options = {
				"executive", "workbook" }),
		@Property(key = PDFPostJob.USERNAME, name = "Username", description = "Username for WS API access.", defaultValue = PDFPostJob.USERNAME_DEFAULT_VALUE, global = true, project = true, module = false),
		@Property(key = PDFPostJob.PASSWORD, name = "Password", description = "Password for WS API access.", defaultValue = PDFPostJob.PASSWORD_DEFAULT_VALUE, global = true, project = true, module = false, type = PropertyType.PASSWORD) })
public class PDFReportPlugin extends SonarPlugin {

	@Override
	public List getExtensions() {
		return Arrays.asList(new Class[] { PDFPostJob.class, PdfReportWidget.class });
	}
}
