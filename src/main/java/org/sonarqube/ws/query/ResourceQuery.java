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

import org.sonarqube.ws.client.services.Query;
import org.sonarqube.ws.model.Resource;

public class ResourceQuery extends Query<Resource> {

	public static final String BASE_URL = "/api/resources";

	public static final int DEPTH_UNLIMITED = -1;

	private Integer depth;
	private String format;
	private Boolean includeTrends = null;
	private Boolean includeAlerts = null;
	private Integer limit;
	private String[] metrics;
	private String[] qualifiers;
	private String resourceKeyOrId;
	private String[] rules;
	private String[] scopes;

	private Boolean verbose = Boolean.FALSE;

	public ResourceQuery() {
	}

	public ResourceQuery(String resourceKeyOrId) {
		this.resourceKeyOrId = resourceKeyOrId;
	}

	public ResourceQuery(long resourceId) {
		this.resourceKeyOrId = String.valueOf(resourceId);
	}

	public Integer getDepth() {
		return depth;
	}

	public ResourceQuery setDepth(Integer depth) {
		this.depth = depth;
		return this;
	}

	public ResourceQuery setAllDepths() {
		return setDepth(DEPTH_UNLIMITED);
	}

	public String getResourceKeyOrId() {
		return resourceKeyOrId;
	}

	public ResourceQuery setResourceKeyOrId(String resourceKeyOrId) {
		this.resourceKeyOrId = resourceKeyOrId;
		return this;
	}

	public ResourceQuery setResourceId(int resourceId) {
		this.resourceKeyOrId = Integer.toString(resourceId);
		return this;
	}

	public Integer getLimit() {
		return limit;
	}

	public ResourceQuery setLimit(Integer limit) {
		this.limit = limit;
		return this;
	}

	public String[] getScopes() {
		return scopes;
	}

	public ResourceQuery setScopes(String... scopes) {
		this.scopes = scopes;
		return this;
	}

	public String[] getQualifiers() {
		return qualifiers;
	}

	public ResourceQuery setQualifiers(String... qualifiers) {
		this.qualifiers = qualifiers;
		return this;
	}

	public String[] getMetrics() {
		return metrics;
	}

	public ResourceQuery setMetrics(String... metrics) {
		this.metrics = metrics;
		return this;
	}

	public String[] getRules() {
		return rules;
	}

	public ResourceQuery setRules(String... rules) {
		this.rules = rules;
		return this;
	}

	public Boolean isVerbose() {
		return verbose;
	}

	public ResourceQuery setVerbose(Boolean verbose) {
		this.verbose = verbose;
		return this;
	}

	public Boolean isIncludeTrends() {
		return includeTrends;
	}

	public ResourceQuery setIncludeTrends(Boolean includeTrends) {
		this.includeTrends = includeTrends;
		return this;
	}

	public Boolean isIncludeAlerts() {
		return includeAlerts;
	}

	public ResourceQuery setIncludeAlerts(Boolean includeAlerts) {
		this.includeAlerts = includeAlerts;
		return this;
	}

	@Override
	public String getUrl() {
		StringBuilder url = new StringBuilder(BASE_URL);
		url.append('?');
		appendUrlParameter(url, "depth", depth);
		appendUrlParameter(url, "format", format);
		appendUrlParameter(url, "includealerts", includeAlerts);
		appendUrlParameter(url, "includetrends", includeTrends);
		appendUrlParameter(url, "limit", limit);
		appendUrlParameter(url, "metrics", metrics);
		appendUrlParameter(url, "qualifiers", qualifiers);
		appendUrlParameter(url, "resource", resourceKeyOrId);
		appendUrlParameter(url, "rules", rules);
		appendUrlParameter(url, "scopes", scopes);
		appendUrlParameter(url, "verbose", verbose);
		return url.toString();
	}

	public String getFormat() {
		return format;

	}

	public ResourceQuery setFormat(String format) {
		this.format = format;
		return this;
	}

	@Override
	public final Class<Resource> getModelClass() {
		return Resource.class;
	}

	public static ResourceQuery createForMetrics(String resourceKeyOrId, String... metricKeys) {
		return new ResourceQuery(resourceKeyOrId).setMetrics(metricKeys).setVerbose(true).setFormat("json");
	}

	public static ResourceQuery createForResource(Resource resource, String... metricKeys) {
		Integer id = resource.getId();
		if (id == null) {
			throw new IllegalArgumentException("id must be set");
		}
		return new ResourceQuery(id.toString()).setMetrics(metricKeys);
	}

	/**
	 * @since 2.10
	 */
	public static ResourceQuery create(String resourceKey) {
		return new ResourceQuery(resourceKey);
	}
}