# default-extensions

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Project status](https://img.shields.io/badge/Project%20status-Maintenance-orange.svg)](https://img.shields.io/badge/Project%20status-Maintenance-orange.svg)

# About

A project with some tool configurations that I use in my projects. 
These configurations can be reused in diverse projects.

# Technologies 

- Java
- Maven
- Spotbugs
- Checkstyle

# Usage
In pom.xml, add the following xml between <build> ... </build>

```xml
<extensions>
	<extension>
		<groupId>org.default.extensions</groupId>
		<artifactId>default-extensions</artifactId>
		<version>${default-extensions.version}</version>
	</extension>
</extensions>
```
