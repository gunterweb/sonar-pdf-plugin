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
package org.sonarqube.ws.query;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.sonarqube.ws.client.services.Query;
import org.sonarqube.ws.internal.EncodingUtils;
import org.sonarqube.ws.model.Issues;

public class IssueQuery extends Query<Issues> {
	private final Map<String, Object> params = new HashMap<String, Object>();
	private static final String BASE_URL = "/api/issues/search";

	private IssueQuery() {
	}

	@Override
	public Class<Issues> getModelClass() {
		return Issues.class;
	}

	@Override
	public String getUrl() {
		StringBuilder url = new StringBuilder(BASE_URL);
		url.append('?');
		for (String key : params.keySet()) {
			appendUrlParameter(url, key, params.get(key));
		}
		return url.toString();
	}

	public static IssueQuery create() {
		return new IssueQuery();
	}

	/**
	 * URL query string, for internal use
	 */
	public Map<String, Object> urlParams() {
		return params;
	}

	public IssueQuery actionPlans(String... s) {
		return addParam("actionPlans", s);
	}

	public IssueQuery additionalFields(String... s) {
		return addParam("additionalFields", s);
	}

	public IssueQuery asc(boolean asc) {
		params.put("asc", asc);
		return this;
	}

	public IssueQuery assigned(Boolean assigned) {
		params.put("assigned", assigned);
		return this;
	}

	public IssueQuery assignees(String... s) {
		return addParam("assignees", s);
	}

	public IssueQuery authors(String... s) {
		return addParam("authors", s);
	}

	public IssueQuery componentKeys(String... s) {
		return addParam("componentKeys", s);
	}

	public IssueQuery createdAt(Date d) {
		params.put("createdAt", EncodingUtils.toQueryParam(d, true));
		return this;
	}

	public IssueQuery createdAfter(Date d) {
		params.put("createdAfter", EncodingUtils.toQueryParam(d, true));
		return this;
	}

	public IssueQuery createdBefore(Date d) {
		params.put("createdBefore", EncodingUtils.toQueryParam(d, true));
		return this;
	}

	public IssueQuery createdInLast(String range) {
		params.put("createdInLast", range);
		return this;
	}

	public IssueQuery directories(String... keys) {
		return addParam("directories", keys);
	}

	public IssueQuery facetMode(String... mode) {
		return addParam("facetMode", mode);
	}

	public IssueQuery facets(String... s) {
		return addParam("facets", s);
	}

	public IssueQuery fileUuids(String... s) {
		return addParam("fileUuids", s);
	}

	public IssueQuery issues(String... keys) {
		return addParam("issues", keys);
	}

	public IssueQuery languages(String... s) {
		return addParam("languages", s);
	}

	public IssueQuery moduleUuids(String... s) {
		return addParam("moduleUuids", s);
	}

	public IssueQuery onComponentOnly(boolean onComponentOnly) {
		params.put("onComponentOnly", onComponentOnly);
		return this;
	}

	public IssueQuery pageIndex(int pageIndex) {
		params.put("p", pageIndex);
		return this;
	}

	public IssueQuery planned(Boolean planned) {
		params.put("planned", planned);
		return this;
	}

	public IssueQuery projectKeys(String... s) {
		return addParam("projectKeys", s);
	}

	public IssueQuery projectUuids(String... s) {
		return addParam("projectUuids", s);
	}

	public IssueQuery pageSize(int pageSize) {
		params.put("ps", pageSize);
		return this;
	}

	public IssueQuery reporters(String... s) {
		return addParam("reporters", s);
	}

	public IssueQuery resolutions(String... resolutions) {
		return addParam("resolutions", resolutions);
	}

	public IssueQuery resolved(Boolean resolved) {
		params.put("resolved", resolved);
		return this;
	}

	public IssueQuery rules(String... s) {
		return addParam("rules", s);
	}

	public IssueQuery sort(String sort) {
		params.put("s", sort);
		return this;
	}

	public IssueQuery severities(String... severities) {
		return addParam("severities", severities);
	}

	public IssueQuery statuses(String... statuses) {
		return addParam("statuses", statuses);
	}

	public IssueQuery tags(String... tags) {
		return addParam("tags", tags);
	}

	private IssueQuery addParam(String key, String[] values) {
		if (values != null) {
			params.put(key, EncodingUtils.toQueryParam(values));
		}
		return this;
	}

}
