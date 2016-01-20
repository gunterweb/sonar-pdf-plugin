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

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.utils.HttpDownloader.HttpException;
import org.sonar.report.pdf.entity.Measure;
import org.sonar.report.pdf.entity.Measures;
import org.sonarqube.ws.client.WSClient;
import org.sonarqube.ws.model.Metric;
import org.sonarqube.ws.model.Metrics;
import org.sonarqube.ws.model.Resource;
import org.sonarqube.ws.query.MetricQuery;
import org.sonarqube.ws.query.ResourceQuery;

public class MeasuresBuilder extends AbstractBuilder {

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
			return new MeasuresBuilder(sonar);
		}
		return builder;
	}

	public List<String> getAllMetricKeys() throws HttpException, IOException {

		MetricQuery query = MetricQuery.all();
		Metrics metrics = sonar.find(query);
		List<String> allMetricKeys = new ArrayList<>();
		Iterator<Metric> it = metrics.getMetrics().iterator();
		while (it.hasNext()) {
			allMetricKeys.add(it.next().getKey());
		}
		return allMetricKeys;
	}

	public Measures initMeasuresByProjectKey(final String projectKey) throws HttpException, IOException {

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
	 */
	private void initMeasuresSplittingRequests(final Measures measures, final String projectKey)
			throws HttpException, IOException {
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
	 */
	private void addMeasures(final Measures measures, final List<String> measuresAsString, final String projectKey)
			throws HttpException, IOException {

		String[] measuresAsArray = measuresAsString.toArray(new String[measuresAsString.size()]);
		LOG.debug(Arrays.toString(measuresAsArray));
		ResourceQuery query = ResourceQuery.createForMetrics(projectKey, measuresAsArray);
		query.setDepth(0);
		query.setIncludeTrends(true);
		List<Resource> resources = sonar.findAll(query);
		if (resources != null && resources.size() == 1) {
			this.addAllMeasuresFromDocument(measures, resources.get(0));
		} else {
			LOG.debug("Wrong response when looking for measures: " + measuresAsString.toString());
		}
	}

	private void addAllMeasuresFromDocument(final Measures measures, final Resource resource) {

		List<org.sonarqube.ws.model.Measure> allNodes = resource.getMsr();
		Iterator<org.sonarqube.ws.model.Measure> it = allNodes.iterator();
		while (it.hasNext()) {
			addMeasureFromNode(measures, it.next());
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

	private void addMeasureFromNode(final Measures measures, final org.sonarqube.ws.model.Measure measureNode) {
		Measure measure = MeasureBuilder.initFromNode(measureNode);
		measures.addMeasure(measure.getKey(), measure);
	}
}
