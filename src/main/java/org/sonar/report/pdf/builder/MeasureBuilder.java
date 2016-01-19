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

import org.sonar.report.pdf.entity.Measure;

public class MeasureBuilder {

	/**
	 * Init measure from XML node. The root node must be "msr".
	 * 
	 * @param measureNode
	 * @return
	 */
	public static Measure initFromNode(final org.sonarqube.ws.model.Measure measureNode) {
		Measure measure = new Measure();
		measure.setKey(measureNode.getKey());

		String formatValueNode = measureNode.getFrmt_val();
		if (formatValueNode != null) {
			measure.setFormatValue(formatValueNode);
			measure.setValue(String.valueOf(measureNode.getVal()));
		}

		Integer trendNode = measureNode.getTrend();
		if (trendNode != null) {
			measure.setQualitativeTendency(trendNode);
		} else {
			measure.setQualitativeTendency(0);
		}

		Integer varNode = measureNode.getVar();
		if (varNode != null) {
			measure.setQuantitativeTendency(varNode);
		} else {
			measure.setQuantitativeTendency(0);
		}

		Double valueNode = measureNode.getVal();

		if (valueNode != null) {
			measure.setTextValue(String.valueOf(valueNode));
		} else {
			measure.setTextValue("");
		}

		measure.setDataValue(measureNode.getData());

		return measure;
	}
}