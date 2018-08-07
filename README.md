lyca-xslt
=========

[![Build Status](https://travis-ci.org/lyca/lyca-xslt.svg?branch=codemodel)](https://travis-ci.org/lyca/lyca-xslt)
[![Coverage Status](https://coveralls.io/repos/github/lyca/lyca-xslt/badge.svg?branch=codemodel)](https://coveralls.io/github/lyca/lyca-xslt?branch=codemodel)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

This repository contains a major rewrite of Apache Xalan Java.

It originated from some performance problems and ended up in self education.

The major areas of changes that were made:
* Removed synchronizations in Transformer creation
* Instead of generating bytecode with BCEL, the translets are now generated
  as source code with JCodeModel and then compiled with the Java Compiler API
* The interpretive mode was completely removed, only xsltc as compiler was
  retained
* Gradle is used as build system
* The conformance test suite was imported and added as JUnit tests
* The use of synchronized data structures like Vector/Stack/Hashtable/StringBuffer
  has been adjusted to the usage of ArrayList/ArrayDeque/LinkedList/HashMap/TreeMap
  and StringBuilder
* The prefix handling from namespaces in xsltc was improved
* The actual source needs Java 8 as baseline, Java 1.2 is not sufficient anymore...
* To be continued...

**Gradle/Grails**

```groovy
compile 'de.lyca.xslt:lyca-xslt:0.9.5'
```

**Apache Maven**

```xml
<dependency>
    <groupId>de.lyca.xslt</groupId>
    <artifactId>lyca-xslt</artifactId>
    <version>0.9.5</version>
</dependency>
```
