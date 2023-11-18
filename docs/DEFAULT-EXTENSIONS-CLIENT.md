# default-extensions-client

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Project status](https://img.shields.io/badge/Project%20status-Maintenance-orange.svg)](https://img.shields.io/badge/Project%20status-Maintenance-orange.svg)

# About

This is Web scraper to update extensions file, like PMD rule set files.
Basically this terminal application go to tools site to check the configs and update the 

# Project status

I change this project constantly improving and adding new configurations, click [here](../docs/STATUS.md) to follow up.

# Technologies 

Technologies used on this project:
 
- Java
- Maven
- Htmlunit
- JSoup
- Httpclient*

# Requirements

The requirements are the same from the [root project](../README.md).

# Install

To install do the same from the [root project](../README.md).

# Configuration

In order to use this application you need to create a 'application.properties' and set the tool's config:

```properties
#----- pmd configs
pmd.folder=/home/fernando/Development/configs/tools/pmd/java
pmd.version=6.55.0
pmd.rules.file.name=pmd-ruleset-${pmd.version}.xml
pmd.rules.file.path=${pmd.folder}/${pmd.rules.file.name}

#----- checkstyle configs
checkstyle.folder=/home/fernando/Development/configs/tools/checkstyle
checkstyle.version=10.12.4
checkstyle.checks.file.name=checkstyle-checks-${checkstyle.version}.xml
checkstyle.checks.file.path=${checkstyle.folder}/${checkstyle.checks.file.name}

#----- spotbugs configs
spotbugs.folder=/home/fernando/Development/configs/tools/spotbugs
spotbugs.version=4.8.0
spotbugs.excludes.file.name=spotbugs-excludes-${spotbugs.version}.xml
spotbugs.excludes.file.path=${spotbugs.folder}/${spotbugs.excludes.file.name}
 ```

If you omit this file, the tool will use the default configuration, that means my personal path folder.

# How to Use

Run the application (linux): 

```bash
java -jar default-extensions-client/target/default-extensions-client-0.0.1-SNAPSHOT.jar
```