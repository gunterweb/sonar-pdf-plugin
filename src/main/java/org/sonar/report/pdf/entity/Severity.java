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

	public static String[] getSeverityArray() {
		return new String[] { INFO, MINOR, MAJOR, CRITICAL, BLOCKER };
	}

	public Severity(String severity) {
		this.severity = severity;
	}

	private Integer getIntFromSeverity(Severity severity) {
		switch (severity.getSeverity()) {
		case INFO:
			return 5;

		case MINOR:
			return 4;

		case MAJOR:
			return 3;

		case CRITICAL:
			return 2;

		case BLOCKER:
			return 1;

		default:
			return 0;
		}
	}

	public Color toColor() {
		switch (severity) {
		case INFO:
			return new Color(51, 255, 51);
		case MINOR:
			return new Color(153, 255, 51);
		case MAJOR:
			return new Color(255, 255, 51);
		case CRITICAL:
			return new Color(255, 153, 51);
		case BLOCKER:
			return new Color(255, 51, 51);
		default:
			return Color.WHITE;
		}
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

}
