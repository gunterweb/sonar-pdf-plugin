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
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.utils.HttpDownloader.HttpException;
import org.sonar.report.pdf.entity.EntityUtils;
import org.sonar.report.pdf.entity.FileInfo;
import org.sonar.report.pdf.entity.Measures;
import org.sonar.report.pdf.entity.Project;
import org.sonar.report.pdf.entity.Rule;
import org.sonar.report.pdf.entity.Severity;
import org.sonar.report.pdf.entity.Violation;
import org.sonar.report.pdf.entity.exception.ReportException;
import org.sonar.report.pdf.util.MetricKeys;
import org.sonarqube.ws.client.WSClient;
import org.sonarqube.ws.model.Issue;
import org.sonarqube.ws.model.Issues;
import org.sonarqube.ws.model.Resource;
import org.sonarqube.ws.query.IssueQuery;
import org.sonarqube.ws.query.ResourceQuery;
import org.sonarqube.ws.query.RuleQuery;

public class ProjectBuilder extends AbstractBuilder {

	private static final Logger LOG = LoggerFactory.getLogger("org.sonar.PDF");

	private static ProjectBuilder builder;

	private WSClient sonar;

	private int tableLimit;
	private int detailsLimit;

	public ProjectBuilder(final WSClient sonar) {
		this.sonar = sonar;
		URL resourceText = this.getClass().getClassLoader().getResource(REPORT_PROPERTIES);
		Properties config = new Properties();
		try {
			config.load(resourceText.openStream());
		} catch (IOException e) {
			LOG.error("\nProblem loading report.properties.", e);
		}
		tableLimit = Integer.valueOf(config.getProperty(SONAR_TABLE_LIMIT));
		detailsLimit = Integer.valueOf(config.getProperty(SONAR_DETAILS_LIMIT));
	}

	public static ProjectBuilder getInstance(final WSClient sonar) {
		if (builder == null) {
			return new ProjectBuilder(sonar);
		}

		return builder;
	}

	/**
	 * Initialize: - Project basic data - Project measures - Project categories
	 * violations - Project most violated rules - Project most violated files -
	 * Project most duplicated files
	 * 
	 * @param sonarAccess
	 * @throws HttpException
	 * @throws IOException
	 * @throws DocumentException
	 * @throws ReportException
	 */
	public Project initializeProject(final String key) throws IOException, ReportException {
		Project project = new Project(key);

		LOG.info("Retrieving project info for " + project.getKey());

		ResourceQuery rq = ResourceQuery.create(project.getKey());
		rq.setDepth(0);
		List<Resource> resources = sonar.findAll(rq);

		if (resources != null && !resources.isEmpty()) {
			initFromNode(project, resources.get(0));
			initMeasures(project);
			initMostViolatedRules(project);
			initMostViolatedFiles(project);
			initMostComplexElements(project);
			initMostDuplicatedFiles(project);
			LOG.debug("Accessing Sonar: getting child projects");

			ResourceQuery resourceQueryChild = ResourceQuery.create(project.getKey());
			resourceQueryChild.setDepth(1);
			List<Resource> childNodes = sonar.findAll(resourceQueryChild);

			Iterator<Resource> it = childNodes.iterator();
			project.setSubprojects(new ArrayList<Project>(0));
			if (!it.hasNext()) {
				LOG.debug(project.getKey() + " project has no childs");
			}
			while (it.hasNext()) {
				Resource childNode = it.next();

				String scope = childNode.getScope();
				if (PROJECT_SCOPE.equals(scope)) {
					Project childProject = initializeProject(childNode.getKey());
					project.getSubprojects().add(childProject);
				}
			}
		} else {
			LOG.info("Can't retrieve project info. Have you set username/password in Sonar settings?");
			throw new ReportException("Can't retrieve project info. Parent project node is empty. Authentication?");
		}

		return project;
	}

	/**
	 * Initialize project object and his childs (except categories violations).
	 */
	private void initFromNode(final Project project, final Resource resourceNode) {
		project.setName(resourceNode.getName());
		project.setDescription(resourceNode.getDescription());
		project.setLinks(new LinkedList<String>());
		project.setSubprojects(new LinkedList<Project>());
		project.setMostViolatedRules(new LinkedList<Rule>());
		project.setMostComplexFiles(new LinkedList<FileInfo>());
		project.setMostDuplicatedFiles(new LinkedList<FileInfo>());
		project.setMostViolatedFiles(new LinkedList<FileInfo>());
	}

	private void initMeasures(final Project project) throws IOException {
		LOG.info("    Retrieving measures");
		MeasuresBuilder measuresBuilder = MeasuresBuilder.getInstance(sonar);
		Measures measures = measuresBuilder.initMeasuresByProjectKey(project.getKey());
		project.setMeasures(measures);
	}

	private void initMostViolatedRules(final Project project) throws IOException, ReportException {
		LOG.info("    Retrieving most violated rules");
		LOG.debug("Accessing Sonar: getting most violated rules");
		String[] severities = Severity.getSeverityArray();

		Map<String, IssueBean> issues = new HashMap<>();
		ValueComparator bvc = new ValueComparator(issues);
		TreeMap<String, IssueBean> sortedMap = new TreeMap<>(bvc);
		// Reverse iteration to get violations with upper level first
		int limit = detailsLimit;
		for (int i = severities.length - 1; i >= 0 && limit > 0; i--) {
			IssueQuery query = IssueQuery.create();
			query.componentKeys(project.getKey());
			query.severities(severities[i]);
			Issues result = sonar.find(query);
			List<Issue> issuesByLevel = result.getIssues();
			if (issuesByLevel != null && !issuesByLevel.isEmpty()) {
				int count = initMostViolatedRulesFromNode(issuesByLevel, issues);
				LOG.debug("\t " + count + " " + severities[i] + " violations");
				limit = limit - count;
			} else {
				LOG.debug("There is not result on select //resources/resource");
				LOG.debug("There are no violations with level " + severities[i]);
			}
		}
		// sort the items of the map by list size
		LOG.debug("unsorted map: " + issues);
		sortedMap.putAll(issues);
		LOG.debug("sorted map: " + sortedMap);
		for (Entry<String, IssueBean> entry : sortedMap.entrySet()) {
			String ruleKey = entry.getKey();
			RuleQuery query = RuleQuery.create(ruleKey);
			org.sonarqube.ws.model.Rules rules = sonar.find(query);
			if (rules == null || rules.getRules() == null || rules.getRules().size() != 1) {
				LOG.error("There is not result on select rule from service");
			} else {
				project.getMostViolatedRules().add(defineRule(entry, rules));
			}
		}

	}

	private Rule defineRule(Entry<String, IssueBean> entry, org.sonarqube.ws.model.Rules rules) {
		org.sonarqube.ws.model.Rule ruleNode = rules.getRules().get(0);
		Rule rule = new Rule();
		rule.setKey(ruleNode.getKey());
		rule.setName(ruleNode.getName());
		rule.setSeverity(new Severity(entry.getValue().getSeverity()));
		rule.setViolationsNumber(Integer.toString(entry.getValue().getIssues().size()));
		// setTopViolations
		List<Violation> violations = new ArrayList<>();
		for (Issue issue : entry.getValue().getIssues()) {
			String line;
			if (issue.getLine() == null) {
				line = EntityUtils.NA_METRICS;
			} else {
				line = "" + issue.getLine();
			}
			Violation violation = new Violation(line, issue.getComponent());
			violations.add(violation);
		}
		rule.setTopViolations(violations);
		return rule;
	}

	private void initMostViolatedFiles(final Project project) throws IOException {
		LOG.info("    Retrieving most violated files");
		LOG.debug("Accessing Sonar: getting most violated files");

		ResourceQuery resourceQuery = ResourceQuery.createForMetrics(project.getKey(), MetricKeys.VIOLATIONS);
		resourceQuery.setScopes(FILE_SCOPE);
		resourceQuery.setDepth(-1);
		resourceQuery.setLimit(tableLimit);
		List<Resource> resources = sonar.findAll(resourceQuery);
		List<FileInfo> fileInfoList = FileInfoBuilder.initFromDocument(resources, FileInfo.VIOLATIONS_CONTENT);
		project.setMostViolatedFiles(fileInfoList);

	}

	private void initMostComplexElements(final Project project) throws IOException {
		LOG.info("    Retrieving most complex elements");
		LOG.debug("Accessing Sonar: getting most complex elements");

		ResourceQuery resourceQuery = ResourceQuery.createForMetrics(project.getKey(), MetricKeys.COMPLEXITY);
		resourceQuery.setScopes(FILE_SCOPE);
		resourceQuery.setDepth(-1);
		resourceQuery.setLimit(tableLimit);
		List<Resource> resources = sonar.findAll(resourceQuery);
		project.setMostComplexFiles(FileInfoBuilder.initFromDocument(resources, FileInfo.CCN_CONTENT));
	}

	private void initMostDuplicatedFiles(final Project project) throws IOException {
		LOG.info("    Retrieving most duplicated files");
		LOG.debug("Accessing Sonar: getting most duplicated files");

		ResourceQuery resourceQuery = ResourceQuery.createForMetrics(project.getKey(), MetricKeys.DUPLICATED_LINES);
		resourceQuery.setScopes(FILE_SCOPE);
		resourceQuery.setDepth(-1);
		resourceQuery.setLimit(tableLimit);
		List<Resource> resources = sonar.findAll(resourceQuery);
		project.setMostDuplicatedFiles(FileInfoBuilder.initFromDocument(resources, FileInfo.DUPLICATIONS_CONTENT));
	}

	private int initMostViolatedRulesFromNode(final List<Issue> issuesByLevel, Map<String, IssueBean> issues)
			throws ReportException, IOException {
		int added = 0;
		for (Issue issue : issuesByLevel) {
			String ruleKey = issue.getRule();
			if (issues.containsKey(ruleKey)) {
				// adds Issue to the List of current issues for the key
				IssueBean bean = issues.get(ruleKey);
				bean.getIssues().add(issue);
			} else {
				// adds Issue to a List for a new key
				List<Issue> issuesForKey = new ArrayList<>();
				IssueBean bean = new IssueBean();
				bean.setSeverity(issue.getSeverity());
				issuesForKey.add(issue);
				bean.setIssues(issuesForKey);
				issues.put(ruleKey, bean);
				added++;
			}
		}

		return added;
	}

	class IssueBean {
		private String severity;
		private List<Issue> issues;

		public String getSeverity() {
			return severity;
		}

		public void setSeverity(String severity) {
			this.severity = severity;
		}

		public List<Issue> getIssues() {
			return issues;
		}

		public void setIssues(List<Issue> issues) {
			this.issues = issues;
		}

		@Override
		public String toString() {
			return getSeverity() + " : size = " + issues.size();
		}

	}

	class ValueComparator implements Comparator<String> {
		Map<String, IssueBean> base;

		public ValueComparator(Map<String, IssueBean> base) {
			this.base = base;
		}

		@Override
		public int compare(String a, String b) {
			IssueBean beanA = base.get(a);
			IssueBean beanB = base.get(b);
			Severity sev1 = new Severity(beanA.getSeverity());
			Severity sev2 = new Severity(beanB.getSeverity());
			if (beanA.getSeverity().equals(beanB.getSeverity())) {
				// return by size of the list of components
				if (beanA.getIssues().size() > beanB.getIssues().size()) {
					return -1;
				} else {
					return 1;
				}
			} else {
				// sort by severity
				return sev1.compareTo(sev2);
			}
		}
	}

}
