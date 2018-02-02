/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.lyca.xml.res;

public interface XmlErrorMessages {

  String functionNotSupported();

  String cannotOverwriteCause();

  String noDefaultImpl();

  String chunkedintarrayNotSupported(Object a);

  String offsetBiggerThanSlot();

  String coroutineNotAvail(Object a);

  String coroutineCoExit();

  String cojoinroutinesetFailed();

  String typedIteratorAxisNotImplemented(Object axisName);

  String iteratorCloneNotSupported();

  String unknownAxisType(Object axisName);

  String noDtmidsAvail();

  String notSupported(Object a);

  String nodeNonNull();

  String couldNotResolveNode();

  String startparseWhileParsing();

  String startparseNeedsSaxparser();

  String couldNotInitParser();

  String exceptionCreatingPool();

  String pathContainsInvalidEscapeSequence();

  String schemeRequired();

  String noSchemeInUri(Object a);

  String noSchemeInuri();

  String pathInvalidChar(Object a);

  String schemeFromNullString();

  String schemeNotConformant();

  String hostAddressNotWellformed();

  String portWhenHostNull();

  String invalidPort();

  String fragForGenericUri();

  String fragWhenPathNull();

  String fragInvalidChar();

  String selfCausationNotPermitted();

  String noUserinfoIfNoHost();

  String noPortIfNoHost();

  String noQueryStringInPath();

  String noFragmentStringInPath();

  String cannotInitUriEmptyParms();

  String methodNotSupported();

  String incrsaxsrcfilterNotRestartable();

  String xmlrdrNotBeforeStartparse();

  String errorhandlerCreatedWithNullPrintwriter();

  String systemidUnknown();

  String locationUnknown();

  String prefixMustResolve(Object a);

  String childHasNoOwnerDocumentElement();

  String cantOutputTextBeforeDoc();

  String cantHaveMoreThanOneRoot();

  String argLocalnameNull();

  String argLocalnameInvalid();

  String argPrefixInvalid();

  String nameCantStartWithColon();

  String line();

  String column();

}
