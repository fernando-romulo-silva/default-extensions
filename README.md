# default-extensions

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Project status](https://img.shields.io/badge/Project%20status-Maintenance-orange.svg)](https://img.shields.io/badge/Project%20status-Maintenance-orange.svg)

# Project Status

I change this project constantly improving and adding new configurations, click [here](docs/STATUS.md) to follow up.

# The Problem

Great, you are using excellent tools like PMD, Spotbugs, Checkstyle, etc., but this tools also have configurations files, and you are spreading this files in ALL projects that has the same configurations. <br />
You could centralize this configurations files in one place...

# About

Build tools extensions are a list of artifacts to be used in build operations, usually XMLs configurations files:

![Model](https://github.com/fernando-romulo-silva/default-extensions/blob/main/docs/Artifacts.png)

They will be included in the running build's classpath.

The idea of this project is to reuse these artifacts in all projects that make sense. 
So far, I have created artifacts for Spotbugs, Checkstyle, etc.

# Technologies 

Technologies used on this project:
 
- Git
- Java
- Ant (optional)
- Maven

# Requirements

The requirements are: 

 - Java >= 17

```bash
# check the Java version
java --version
```
 - Maven >= 3.8.8

```bash
# check the Maven version
mvn --version
```

# Modules

This project has these modules:

- default-extensions-client
- default-extensions-files

The **default-extensions-files** module has the artifact files, no code, only files.

The **default-extensions-client** module is a web crawler that updates the artifact files with the newest configuration on its respective web page.
Click [here](docs/DEFAULT-EXTENSIONS-CLIENT.md) for more information.

# Install

To install just execute on terminal:
 
```bash
git clone https://github.com/fernando-romulo-silva/default-extensions
```

Access the project folder:

```bash
cd default-extensions
```

Then execute execute:

```bash
mvn install
```

# How to Use with Maven

In your pom.xml project, add the following xml between `<build> ... </build>`

```xml
<extensions>
	<extension>
		<groupId>org.default.extensions</groupId>
		<artifactId>default-extensions-files</artifactId>
		<version>${default-extensions.version}</version>
	</extension>
</extensions>
```
