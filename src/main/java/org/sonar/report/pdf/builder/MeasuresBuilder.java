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
package org.sonar.report.pdf.builder;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.report.pdf.entity.Measure;
import org.sonar.report.pdf.entity.Measures;
import org.sonar.report.pdf.entity.exception.ReportException;
import org.sonar.report.pdf.util.MetricKeys;
import org.sonarqube.ws.client.WSClient;
import org.sonarqube.ws.model.Metric;
import org.sonarqube.ws.model.Metrics;
import org.sonarqube.ws.model.Resource;
import org.sonarqube.ws.query.MetricQuery;
import org.sonarqube.ws.query.ResourceQuery;

/**
 * Builder for a set of measures
 *
 */
public class MeasuresBuilder extends AbstractBuilder {

    /**
     * 
     */
    private static final long serialVersionUID = 6613369345856603442L;

    private static final Logger LOG = LoggerFactory.getLogger(MeasuresBuilder.class);

    private static MeasuresBuilder builder;

    private WSClient sonar;

    private List<String> measuresKeys = null;

    private static final Integer DEFAULT_SPLIT_LIMIT = 20;

    public MeasuresBuilder(final WSClient sonar) {
        this.sonar = sonar;
    }

    public static MeasuresBuilder getInstance(final WSClient sonar) {
        if (builder == null) {
            builder = new MeasuresBuilder(sonar);
        }
        return builder;
    }

    /**
     * Get the metric keys
     * 
     * @return List of Keys
     * @throws ReportException
     *             ReportException
     */
    public List<String> getAllMetricKeys() throws ReportException {

        MetricQuery query = MetricQuery.all();
        Metrics metrics = sonar.find(query);
        List<String> allMetricKeys = new ArrayList<>();
        Iterator<Metric> it = metrics.getMetrics().iterator();
        while (it.hasNext()) {
            allMetricKeys.add(it.next().getKey());
        }
        return allMetricKeys;
    }

    /**
     * Initialization of measures of a project
     * 
     * @param projectKey
     *            key of the project
     * @return Measures
     * @throws ReportException
     *             ReportException
     */
    public Measures initMeasuresByProjectKey(final String projectKey) throws ReportException {

        Measures measures = new Measures();
        if (measuresKeys == null) {
            measuresKeys = getAllMetricKeys();
        }
        // Avoid "Post too large"
        if (measuresKeys.size() > DEFAULT_SPLIT_LIMIT) {
            initMeasuresSplittingRequests(measures, projectKey);
        } else {
            this.addMeasures(measures, measuresKeys, projectKey);
        }

        return measures;

    }

    /**
     * This method does the required requests to get all measures from Sonar,
     * but taking care to avoid too large requests (measures are taken by 20).
     * 
     * @param measures
     *            measures
     * @param projectKey
     *            projectKey
     * @throws ReportException
     *             ReportException
     */
    private void initMeasuresSplittingRequests(final Measures measures, final String projectKey)
            throws ReportException {
        Iterator<String> it = measuresKeys.iterator();
        LOG.debug("Getting " + measuresKeys.size() + " metric measures from Sonar by splitting requests");
        List<String> twentyMeasures = new ArrayList<>(20);
        int i = 0;
        while (it.hasNext()) {
            twentyMeasures.add(it.next());
            i++;
            if (i % DEFAULT_SPLIT_LIMIT == 0) {
                LOG.debug("Split request for: " + twentyMeasures);
                addMeasures(measures, twentyMeasures, projectKey);
                i = 0;
                twentyMeasures.clear();
            }
        }
        if (i != 0) {
            LOG.debug("Split request for remain metric measures: " + twentyMeasures);
            addMeasures(measures, twentyMeasures, projectKey);
        }
    }

    /**
     * Add measures to this.
     *
     * @param measures
     *            measures
     * @param measuresAsString
     *            measuresAsString
     * @param projectKey
     *            projectKey
     * @throws ReportException
     *             ReportException
     */
    private void addMeasures(final Measures measures, final List<String> measuresAsString, final String projectKey)
            throws ReportException {

        String[] measuresAsArray = measuresAsString.toArray(new String[measuresAsString.size()]);
        LOG.debug(Arrays.toString(measuresAsArray));
        ResourceQuery query = ResourceQuery.createForMetrics(projectKey, measuresAsArray);
        query.setDepth(0);
        query.setIncludeTrends(true);
        List<Resource> resources = sonar.findAll(query);
        if (resources != null && resources.size() == 1) {
            this.addAllMeasuresFromDocument(projectKey, measures, resources.get(0));
        } else {
            LOG.debug("Wrong response when looking for measures: " + measuresAsString.toString());
        }
    }

    /**
     * Add all measures from a document
     * 
     * @param projectKey
     *            projectKey
     * @param measures
     *            measures
     * @param resource
     *            resource
     * @throws ReportException
     *             ReportException
     */
    private void addAllMeasuresFromDocument(final String projectKey, final Measures measures, final Resource resource)
            throws ReportException {

        List<org.sonarqube.ws.model.Measure> allNodes = resource.getMsr();
        Iterator<org.sonarqube.ws.model.Measure> it = allNodes.iterator();
        while (it.hasNext()) {
            addMeasureFromNode(projectKey, measures, it.next());
        }
        try {

            Date dateNode = resource.getDate();
            if (dateNode != null) {
                measures.setDate(dateNode);
            }

            String versionNode = resource.getVersion();
            if (versionNode != null) {
                measures.setVersion(versionNode);
            }
        } catch (ParseException e) {
            LOG.error("Can not parse date", e);
        }
    }

    /**
     * Add a measure from a node
     * 
     * @param projectKey
     *            projectKey
     * @param measures
     *            measures
     * @param measureNode
     *            measureNode
     * @throws ReportException
     *             ReportException
     */
    private void addMeasureFromNode(final String projectKey, final Measures measures,
            final org.sonarqube.ws.model.Measure measureNode) throws ReportException {
        Measure measure = MeasureBuilder.initFromNode(measureNode);
        if (MetricKeys.isMetricNeeded(measure.getKey())) {
            Integer trendNode = HistoryBuilder.getInstance(sonar, projectKey).computeTrend(measureNode);
            if (trendNode != null) {
                measure.setQualitativeTendency(trendNode);
            } else {
                measure.setQualitativeTendency(0);
            }
        }
        measures.addMeasure(measure.getKey(), measure);
    }

}
