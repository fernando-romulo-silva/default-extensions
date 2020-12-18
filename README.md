# default-extensions

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Project status](https://img.shields.io/badge/Project%20status-Maintenance-orange.svg)](https://img.shields.io/badge/Project%20status-Maintenance-orange.svg)

# About

A project with some tool configurations that I use in my projects. 
These configurations can be reused in diverse projects.

# Usage
Add the following section into your `pom.xml` file.

```xml
 <build>
 	<extensions>
		<extension>
			<groupId>org.default.extensions</groupId>
			<artifactId>default-extensions</artifactId>
			<version>${allset.config.extensions.version}</version>
		</extension>
	</extensions>
</build>   
```
