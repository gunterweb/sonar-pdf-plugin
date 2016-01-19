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
package org.sonarqube.ws.model;

import java.util.Collections;
import java.util.List;

public class Metrics extends Model {
	private List<Metric> metrics;

	public List<Metric> getMetrics() {
		if (metrics == null) {
			return Collections.emptyList();
		}
		return metrics;
	}

	public Metric getMetric(String metricKey) {
		for (Metric metric : getMetrics()) {
			if (metricKey.equals(metric.getKey())) {
				return metric;
			}
		}
		return null;
	}

	public String getMetricName(String metricKey) {
		Metric metric = getMetric(metricKey);
		if (metric != null) {
			return metric.getName();
		}
		return null;
	}

	public void setMetrics(List<Metric> metrics) {
		this.metrics = metrics;
	}
}
