Sonar PDF Report Plugin
=========================

Version compatbility : 5.3+. Use maven to build from repository

## Description / Features

Generate a project quality report in PDF format with the most relevant information from SonarQube web interface. The report aims to be a deliverable as part of project documentation.

The report contains:

* Dashboard
* Violations by categories
* Hotspots:
  * Most violated rules
  * Most violated files
  * Most complex classes
  * Most duplicated files
* Dashboard, violations and hotspots for all child modules (if they exists)

## Installation

1. Install the plugin into the SONARQUBE_HOME/extensions/plugins directory
1. Restart SonarQube

## Usage

SonarQube PDF works as a post-job task. In this way, a PDF report is generated after each analysis in SonarQube.

### Configuration

You can skip report generation or select report type (executive or workbook) globally or at the project level. You can also provide an username/password if your project is secured by SonarQube user management:

![Plugin Configuration](configuration.jpg?raw=true "Plugin Configuration")

### Download the report

PDF report can be downloaded from the SonarQube GUI:
![PDF Report example](output.jpg?raw=true "PDF Report example")

Issue tracking:
https://trello.com/sonarpdf

CI builds:
https://travis-ci.org/gunterweb/sonar-pdf-plugin
https://jenkins-sonarpdfreport.rhcloud.com/
