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

package org.sonar.report.pdf.batch;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.ExtensionPoint;
import org.sonar.api.batch.CheckProject;
import org.sonar.api.batch.PostJob;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;
import org.sonar.report.pdf.PDFResources;
import org.sonar.report.pdf.util.FileUploader;

/**
 * Extension point for PDF Job
 *
 */
@ExtensionPoint
public class PDFPostJob implements PostJob, CheckProject {

    private static final String PDF_EXTENSION = ".pdf";

    private static final Logger LOG = LoggerFactory.getLogger(PDFPostJob.class);

    public static final String SKIP_PDF_KEY = "sonar.pdf.skip";
    public static final boolean SKIP_PDF_DEFAULT_VALUE = false;

    public static final String REPORT_TYPE = "report.type";
    public static final String REPORT_TYPE_DEFAULT_VALUE = PDFResources.WORKBOOK_REPORT_TYPE;

    public static final String USERNAME = "sonar.pdf.username";
    public static final String USERNAME_DEFAULT_VALUE = "";

    public static final String SONAR_P_KEY = "sonar.pdf.password";
    public static final String SONAR_P_DEFAULT_VALUE = "";

    public static final String SONAR_HOST_URL = "sonar.host.url";
    public static final String SONAR_HOST_URL_DEFAULT_VALUE = "http://localhost:9000";

    private final Settings settings;
    private final FileSystem fs;

    public PDFPostJob(Settings settings, FileSystem fs) {
        this.settings = settings;
        this.fs = fs;
    }

    /**
     * @see org.sonar.api.batch.CheckProject#shouldExecuteOnProject(org.sonar.api.resources.Project)
     */
    @Override
    public boolean shouldExecuteOnProject(final Project project) {
        return settings.hasKey(SKIP_PDF_KEY) ? !settings.getBoolean(SKIP_PDF_KEY) : !SKIP_PDF_DEFAULT_VALUE;
    }

    /**
     * @see org.sonar.api.batch.PostJob#executeOn(org.sonar.api.resources.Project,
     *      org.sonar.api.batch.SensorContext)
     */
    @Override
    public void executeOn(final Project project, final SensorContext context) {
        LOG.info("Executing decorator: PDF Report");
        String sonarHostUrl = settings.hasKey(SONAR_HOST_URL) ? settings.getString(SONAR_HOST_URL)
                : SONAR_HOST_URL_DEFAULT_VALUE;
        String username = settings.hasKey(USERNAME) ? settings.getString(USERNAME) : USERNAME_DEFAULT_VALUE;
        String password = settings.hasKey(SONAR_P_KEY) ? settings.getString(SONAR_P_KEY) : SONAR_P_DEFAULT_VALUE;
        String reportType = settings.hasKey(REPORT_TYPE) ? settings.getString(REPORT_TYPE) : REPORT_TYPE_DEFAULT_VALUE;
        PDFGenerator generator = new PDFGenerator(project, fs, sonarHostUrl, username, password, reportType);

        generator.execute();

        String path = fs.workDir().getAbsolutePath() + "/" + project.getEffectiveKey().replace(':', '-') + PDF_EXTENSION;

        File pdf = new File(path);
        if (pdf.exists()) {
            FileUploader.upload(pdf, sonarHostUrl + PDFResources.PDF_REPORT_STORE_PATH, username, password);
        } else {
            LOG.error("PDF file not found in local filesystem. Report could not be sent to server.");
        }
    }

}
