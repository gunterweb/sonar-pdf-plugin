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
package org.sonar.report.pdf.entity;

import java.awt.Color;

/**
 * Priorities.
 */
public class Severity implements Comparable<Severity> {
	private String severity;

	public static final String INFO = "INFO";
	public static final String MINOR = "MINOR";
	public static final String MAJOR = "MAJOR";
	public static final String CRITICAL = "CRITICAL";
	public static final String BLOCKER = "BLOCKER";

	public Severity(String severity) {
		this.severity = severity;
	}

	public static String[] getSeverityArray() {
		return new String[] { INFO, MINOR, MAJOR, CRITICAL, BLOCKER };
	}

	private Integer getIntFromSeverity(Severity severity) {
		int intValue;
		switch (severity.getSeverity()) {
		case INFO:
			intValue = 5;
			break;
		case MINOR:
			intValue = 4;
			break;

		case MAJOR:
			intValue = 3;
			break;

		case CRITICAL:
			intValue = 2;
			break;

		case BLOCKER:
			intValue = 1;
			break;

		default:
			intValue = 0;
		}
		return intValue;
	}

	public Color toColor() {
		Color color;
		switch (severity) {
		case INFO:
			color = new Color(51, 255, 51);
			break;
		case MINOR:
			color = new Color(153, 255, 51);
			break;
		case MAJOR:
			color = new Color(255, 255, 51);
			break;
		case CRITICAL:
			color = new Color(255, 153, 51);
			break;
		case BLOCKER:
			color = new Color(255, 51, 51);
			break;
		default:
			color = Color.WHITE;
		}
		return color;
	}

	public String getSeverity() {
		return severity;
	}

	public void setSeverity(String severity) {
		this.severity = severity;
	}

	@Override
	public int compareTo(Severity o) {
		if (this == null || o == null) {
			return 0;
		} else {
			return getIntFromSeverity(this).compareTo(getIntFromSeverity(o));
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Severity) {
			return getIntFromSeverity(this) == getIntFromSeverity((Severity) obj);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

}
