lyca-xslt
=========

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
