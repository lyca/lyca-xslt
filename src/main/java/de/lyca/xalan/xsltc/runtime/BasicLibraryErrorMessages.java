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

package de.lyca.xalan.xsltc.runtime;

public interface BasicLibraryErrorMessages {

  String runTimeInternalErr(Object error);

  String runTimeCopyErr();

  String dataConversionErr(Object typeSource, Object typeTarget);

  String externalFuncErr(Object functionName);

  String equalityExprErr();

  String invalidArgumentErr(Object argumentType, Object functionName);

  String formatNumberErr(Object number, Object pattern);

  String iteratorCloneErr(Object iterator);

  String axisSupportErr(Object axis);

  String typedAxisSupportErr(Object axis);

  String strayAttributeErr(Object attribute);

  String strayNamespaceErr(Object namespace, Object prefix);

  String namespacePrefixErr(Object prefix);

  String domAdapterInitErr();

  String parserDtdSupportErr();

  String namespacesSupportErr();

  String cantResolveRelativeUriErr(Object  uri);

  String unsupportedXslErr(Object elementName);

  String unsupportedExtErr(Object extensionName);

  String unknownTransletVersionErr(Object version);

  String invalidQnameErr(Object qname);

  String invalidNcnameErr(Object ncname);

  String unallowedExtensionFunctionErr(Object functionName);

  String unallowedExtensionElementErr(Object extensionElementName);

}
