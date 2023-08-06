# default-extensions

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Project status](https://img.shields.io/badge/Project%20status-Maintenance-orange.svg)](https://img.shields.io/badge/Project%20status-Maintenance-orange.svg)

# About

Maven extensions are a list of artifacts that are to be used in this build. 
They will be included in the running build's classpath.
The idea of this project is to reuse these artifacts in all projects that make sense.
So far, I create artifcats for Spotbugs, Checkstyle and PMD.

# Technologies 

- Java
- Ant
- Maven
- Spotbugs
- Checkstyle
- PMD

# Install

requirements (environment variables configured): 
 - Java 8
 - Maven 3
 
```bash
# clone it
git clone https://github.com/fernando-romulo-silva/default-extensions

# access the project folder
cd default-extensions

# execute
mvn install
```

# How to Use

In pom.xml, add the following xml between `<build> ... </build>`

```xml
<extensions>
	<extension>
		<groupId>org.default.extensions</groupId>
		<artifactId>default-extensions-files</artifactId>
		<version>${default-extensions.version}</version>
	</extension>
</extensions>
```
