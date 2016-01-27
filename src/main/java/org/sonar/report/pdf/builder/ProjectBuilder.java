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
import java.io.Serializable;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.report.pdf.PDFResources;
import org.sonar.report.pdf.entity.EntityUtils;
import org.sonar.report.pdf.entity.FileInfo;
import org.sonar.report.pdf.entity.FileInfoTypes;
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

/**
 * Builder for the whole project
 *
 */
public class ProjectBuilder extends AbstractBuilder {

    /**
     * 
     */
    private static final long serialVersionUID = -2813326260092889127L;

    private static final Logger LOG = LoggerFactory.getLogger("org.sonar.PDF");

    private static ProjectBuilder builder;

    private WSClient sonar;

    private Integer tableLimit;
    private Integer detailsLimit;

    /**
     * Default constructor
     * 
     * @param sonar
     *            sonar
     */
    private ProjectBuilder(final WSClient sonar) {
        this.sonar = sonar;
        URL resourceText = this.getClass().getClassLoader().getResource(PDFResources.REPORT_PROPERTIES);
        Properties config = new Properties();
        try {
            config.load(resourceText.openStream());
        } catch (IOException e) {
            LOG.error("\nProblem loading report.properties.", e);
        }
        tableLimit = Integer.valueOf(config.getProperty(PDFResources.SONAR_TABLE_LIMIT));
        detailsLimit = Integer.valueOf(config.getProperty(PDFResources.SONAR_DETAILS_LIMIT));
    }

    public static ProjectBuilder getInstance(final WSClient sonar) {
        if (builder == null) {
            builder = new ProjectBuilder(sonar);
        }

        return builder;
    }

    /**
     * Initialize: <br>
     * - Project basic data <br>
     * - Project measures <br>
     * - Project categories violations <br>
     * - Project most violated rules<br>
     * - Project most violated files<br>
     * - Project most duplicated files<br>
     * 
     * @param projectKey
     *            projectKey
     * 
     * @throws ReportException
     *             ReportException
     */
    public Project initializeProject(final String projectKey) throws ReportException {
        Project project = new Project(projectKey);

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
                if (PDFResources.PROJECT_SCOPE.equals(scope)) {
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
     * 
     * @param project
     *            project
     * @param resourceNode
     *            resourceNode
     */
    private void initFromNode(Project project, final Resource resourceNode) {
        project.setName(resourceNode.getName());
        project.setDescription(resourceNode.getDescription());
        project.setSubprojects(new LinkedList<Project>());
        project.setMostViolatedRules(new LinkedList<Rule>());
        project.setMostComplexFiles(new LinkedList<FileInfo>());
        project.setMostDuplicatedFiles(new LinkedList<FileInfo>());
        project.setMostViolatedFiles(new LinkedList<FileInfo>());
    }

    /**
     * Initialize measures
     * 
     * @param project
     *            project
     * @throws ReportException
     *             ReportException
     */
    private void initMeasures(final Project project) throws ReportException {
        LOG.info("    Retrieving measures");
        MeasuresBuilder measuresBuilder = MeasuresBuilder.getInstance(sonar);
        Measures measures = measuresBuilder.initMeasuresByProjectKey(project.getKey());
        project.setMeasures(measures);
    }

    private void initMostViolatedRules(final Project project) throws ReportException {
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

    /**
     * Define Rule from sonar Rules
     * 
     * @param entry
     *            entry
     * @param rules
     *            rules
     * @return Rule
     */
    private Rule defineRule(Entry<String, IssueBean> entry, org.sonarqube.ws.model.Rules rules) {
        org.sonarqube.ws.model.Rule ruleNode = rules.getRules().get(0);
        Rule rule = new Rule();
        rule.setKey(ruleNode.getKey());
        rule.setName(ruleNode.getName());
        rule.setSeverity(entry.getValue().getSeverity());
        rule.setViolationsNumber(Integer.toString(entry.getValue().getIssues().size()));
        // setTopViolations
        List<Violation> violations = new ArrayList<>();
        for (Issue issue : entry.getValue().getIssues()) {
            String line;
            if (issue.getLine() == null) {
                line = EntityUtils.NA_METRICS.getKey();
            } else {
                line = "" + issue.getLine();
            }
            Violation violation = new Violation(line, issue.getComponent());
            violations.add(violation);
        }
        rule.setTopViolations(violations);
        return rule;
    }

    /**
     * Initialize most violated files
     * 
     * @param project
     *            project
     * @throws ReportException
     *             ReportException
     */
    private void initMostViolatedFiles(final Project project) throws ReportException {
        LOG.info("    Retrieving most violated files");
        LOG.debug("Accessing Sonar: getting most violated files");

        ResourceQuery resourceQuery = ResourceQuery.createForMetrics(project.getKey(), MetricKeys.VIOLATIONS);
        resourceQuery.setScopes(PDFResources.FILE_SCOPE);
        resourceQuery.setDepth(-1);
        resourceQuery.setLimit(tableLimit);
        List<Resource> resources = sonar.findAll(resourceQuery);
        List<FileInfo> fileInfoList = FileInfoBuilder.initFromDocument(resources, FileInfoTypes.VIOLATIONS_CONTENT);
        project.setMostViolatedFiles(fileInfoList);

    }

    /**
     * Initialize most complex files
     * 
     * @param project
     *            project
     * @throws ReportException
     *             ReportException
     */
    private void initMostComplexElements(final Project project) throws ReportException {
        LOG.info("    Retrieving most complex elements");
        LOG.debug("Accessing Sonar: getting most complex elements");

        ResourceQuery resourceQuery = ResourceQuery.createForMetrics(project.getKey(), MetricKeys.COMPLEXITY);
        resourceQuery.setScopes(PDFResources.FILE_SCOPE);
        resourceQuery.setDepth(-1);
        resourceQuery.setLimit(tableLimit);
        List<Resource> resources = sonar.findAll(resourceQuery);
        project.setMostComplexFiles(FileInfoBuilder.initFromDocument(resources, FileInfoTypes.CCN_CONTENT));
    }

    /**
     * Initialize most duplicated files
     * 
     * @param project
     *            project
     * @throws ReportException
     *             ReportException
     */
    private void initMostDuplicatedFiles(final Project project) throws ReportException {
        LOG.info("    Retrieving most duplicated files");
        LOG.debug("Accessing Sonar: getting most duplicated files");

        ResourceQuery resourceQuery = ResourceQuery.createForMetrics(project.getKey(), MetricKeys.DUPLICATED_LINES);
        resourceQuery.setScopes(PDFResources.FILE_SCOPE);
        resourceQuery.setDepth(-1);
        resourceQuery.setLimit(tableLimit);
        List<Resource> resources = sonar.findAll(resourceQuery);
        project.setMostDuplicatedFiles(FileInfoBuilder.initFromDocument(resources, FileInfoTypes.DUPLICATIONS_CONTENT));
    }

    /**
     * Initialize most violated rules
     * 
     * @param issuesByLevel
     *            issuesByLevel
     * @param issues
     *            issues map
     * @return number of fules added
     * @throws ReportException
     */
    private int initMostViolatedRulesFromNode(final List<Issue> issuesByLevel, Map<String, IssueBean> issues)
            throws ReportException {
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
                bean.setSeverity(Severity.get(issue.getSeverity()));
                issuesForKey.add(issue);
                bean.setIssues(issuesForKey);
                issues.put(ruleKey, bean);
                added++;
            }
        }

        return added;
    }

    /**
     * Container of issues
     *
     */
    static class IssueBean {
        private Severity severity;
        private List<Issue> issues;

        public Severity getSeverity() {
            return severity;
        }

        public void setSeverity(Severity severity) {
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

    /**
     * Comparator to sort issues
     *
     */
    static class ValueComparator implements Comparator<String>, Serializable {
        /**
         * 
         */
        private static final long serialVersionUID = -8156307906104648499L;
        transient Map<String, IssueBean> base;

        public ValueComparator(Map<String, IssueBean> base) {
            this.base = base;
        }

        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(String ruleKey1, String ruleKey2) {
            IssueBean beanA = base.get(ruleKey1);
            IssueBean beanB = base.get(ruleKey2);
            if (beanA.getSeverity().equals(beanB.getSeverity())) {
                // return by size of the list of components
                if (beanA.getIssues().size() > beanB.getIssues().size()) {
                    return -1;
                } else {
                    return 1;
                }
            } else {
                // sort by severity
                return beanA.getSeverity().compareTo(beanB.getSeverity());
            }
        }
    }

}
