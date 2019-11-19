phoenix-hibernate-dialect
=========================

### Description

An Apache Phoenix Hibernate dialect.

### Build


[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.ruesga.phoenix/phoenix-hibernate-dialect/badge.svg?style=flat)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.ruesga.phoenix%22%20AND%20a%3A%22phoenix-hibernate-dialect%22) [![Bintray](https://img.shields.io/bintray/v/jruesga/maven/phoenix-hibernate-dialect.svg?maxAge=2592000)](https://bintray.com/jruesga/maven/phoenix-hibernate-dialect/_latestVersion) [![Build Status](https://travis-ci.org/jruesga/phoenix-hibernate-dialect.svg?branch=master)](https://travis-ci.org/jruesga/phoenix-hibernate-dialect/branches) [![GitHub release](https://img.shields.io/github/release/jruesga/phoenix-hibernate-dialect.svg)](https://github.com/jruesga/phoenix-hibernate-dialect/releases/latest) [![Apache 2.0](https://img.shields.io/github/license/jruesga/phoenix-hibernate-dialect.svg)](http://www.apache.org/licenses/LICENSE-2.0)

The project builds with maven. Just type the next command in the project root directory.

    mvn clean install

To deploy to Bintray, just type the next command in the project root directory. Replace <gpg.passphrase>
with your current Gpg passphrase.

    mvn -Pdeploy -Dgpg.passphrase=<gpg.passphrase> clean deploy


### Requirements

Build againsts Hibernate 5 requires Java 8 and AspectJ 1.8.x.


### Usage

Add the following dependency to your pom.xml.

```xml
    <dependencies>
        ...
        <dependency>
            <groupId>com.ruesga.phoenix</groupId>
            <artifactId>phoenix-hibernate-dialect</artifactId>
            <version>0.0.4-hibernate5</version>
        </dependency>
        ...
    </dependencies>
```

Register the dialect in your persistence unit in the persistence.xml file.

```xml
    <persistence-unit name="jpa" transaction-type="RESOURCE_LOCAL">
        <provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>
        <properties>
            ...
            <property name="javax.persistence.jdbc.driver" value="org.apache.phoenix.jdbc.PhoenixDriver" />
            <property name="javax.persistence.jdbc.url" value="jdbc:phoenix:<server:ip>:<path>" />
            <property name="javax.persistence.jdbc.user" value="" />
            <property name="javax.persistence.jdbc.password" value="" />
            <property name="hibernate.dialect" value="com.timebusker.phoenix.dialect.PhoenixDialect" />
            ...
        </properties>
    </persistence-unit>
```

If you want to use indexes, just add the following properties to your client and server
hbase-site.xml configuration.

```xml
    <configuration>
        ...
        <property>
            <name>hbase.regionserver.wal.codec</name>
            <value>org.apache.hadoop.hbase.regionserver.wal.IndexedWALEditCodec</value>
        </property>
        ...
    </configuration>
```

##### Using SpringBoot?

If you want to use the library inside an SpringBoot app, you must ensure that the context in the
aspects is updated by importing the one responsible for initializing the load time weaving into
the spring-boot configuration:

```java
@SpringBootApplication
@ImportResource(locations = "classpath:/META-INF/ctx.spring.weaving.xml")
public class MySpringBootApplication {
    public static void main(final String[] args) {
        DynamicInstrumentationLoader.waitForInitialized();
        DynamicInstrumentationLoader.initLoadTimeWeavingContext();
        SpringApplication.run(...);
    }
}
```

### Want to contribute?

Just file new issues/feature request or send pull requests.

### Licenses

This source was released under the terms of [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0.html) license.

The test database is a partial subset of the [MySQL Employees Sample Database](https://dev.mysql.com/doc/employee/en/)
released under the [Creative Commons Attribution-Share Alike 3.0 Unported License](http://creativecommons.org/licenses/by-sa/3.0/)


```
Copyright (C) 2017 Jorge Ruesga

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
