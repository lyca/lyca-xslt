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

package de.lyca.xalan.xsltc.compiler.util;

/**
 * @version $Id: $
 */
public interface MyErrorMessages {

  String multipleStylesheetErr();

  String templateRedefErr(Object templateName);

  String templateUndefErr(Object templateName);

  String variableRedefErr(Object variableName);

  String variableUndefErr(Object variableName);

  String classNotFoundErr(Object className);

  String methodNotFoundErr(Object methodName);

  String argumentConversionErr(Object methodName);

  String fileNotFoundErr(Object fileName);

  String invalidUriErr(Object uri);

  String fileAccessErr(Object uriOrFileName);

  String missingRootErr();

  String namespaceUndefErr(Object prefix);

  String functionResolveErr(Object functionName);

  String needLiteralErr(Object functionName);

  String xpathParserErr(Object expression);

  String requiredAttrErr(Object attributeName);

  String illegalCharErr(Object offendingCharacter);

  String illegalPiErr(Object piName);

  String strayAttributeErr(Object attributeName);

  String illegalAttributeErr(Object attributeName);

  String circularIncludeErr(Object stylesheetName);

  String resultTreeSortErr();

  String symbolsRedefErr(Object decimalFormatting);

  String xslVersionErr(Object version);

  String circularVariableErr(Object stylesheetName);

  String illegalBinaryOpErr();

  String illegalArgErr();

  String documentArgErr();

  String missingWhenErr();

  String multipleOtherwiseErr();

  String strayOtherwiseErr();

  String strayWhenErr();

  String whenElementErr();

  String unnamedAttribsetErr();

  String illegalChildErr();

  String illegalElemNameErr(Object elementName);

  String illegalAttrNameErr(Object attributeName);

  String illegalTextNodeErr();

  String saxParserConfigErr();

  String internalErr(Object error);

  String unsupportedXslErr(Object elementName);

  String unsupportedExtErr(Object extensionName);

  String missingXsltUriErr();

  String missingXsltTargetErr(Object stylesheetName);

  String notImplementedErr(Object className);

  String notStylesheetErr();

  String elementParseErr(Object elementName);

  String keyUseAttrErr();

  String outputVersionErr();

  String illegalRelatOpErr();

  String attribsetUndefErr(Object attributeSetName);

  String attrValTemplateErr(Object expression);

  String unknownSigTypeErr(Object className);

  String dataConversionErr(Object sourceType, Object targetType);

  String noTransletClassErr();

  String noMainTransletErr(Object className);

  String transletClassErr(Object className);

  String transletObjectErr();

  String errorListenerNullErr(Object methodName);

  String jaxpUnknownSourceErr();

  String jaxpNoSourceErr(Object methodName);

  String jaxpCompileErr();

  String jaxpInvalidAttrErr(Object attributeName);

  String jaxpSetResultErr();

  String jaxpNoTransletErr();

  String jaxpNoHandlerErr();

  String jaxpNoResultErr(Object methodName);

  String jaxpUnknownPropErr(Object propertyName);

  String sax2domAdapterErr(Object error);

  String xsltcSourceErr();

  String erResultNull();

  String jaxpInvalidSetParamValue(Object parameterName);

  String compileStdinErr();

  String compileUsageStr();

  String transformUsageStr();

  String straySortErr();

  String unsupportedEncoding(Object encoding);

  String syntaxErr(Object expression);

  String constructorNotFound(Object className);

  String noJavaFunctThisRef(Object functionName);

  String typeCheckErr(Object expression);

  String typeCheckUnkLocErr();

  String illegalCmdlineOptionErr(Object option);

  String cmdlineOptMissingArgErr(Object option);

  String warningPlusWrappedMsg(Object warning, Object message);

  String warningMsg(Object warning);

  String fatalErrPlusWrappedMsg(Object error, Object message);

  String fatalErrMsg(Object error);

  String errorPlusWrappedMsg(Object error, Object message);

  String errorMsg(Object error);

  String transformWithTransletStr(Object className);

  String transformWithJarStr(Object className, Object jarFile);

  String couldNotCreateTransFact(Object className);

  String transletNameJavaConflict(Object transletName, Object substituteName);

  String compilerErrorKey();

  String compilerWarningKey();

  String runtimeErrorKey();

  String invalidQnameErr(Object value);

  String invalidNcnameErr(Object value);

  String invalidMethodInOutput(Object value);

  String jaxpGetFeatureNullName();

  String jaxpSetFeatureNullName();

  String jaxpUnsupportedFeature(Object feature);

  String outlineErrTryCatch();

  String outlineErrUnbalancedMarkers();

  String outlineErrDeletedTarget();

  String outlineErrMethodTooBig();

}
