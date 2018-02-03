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

package de.lyca.xpath.res;

public interface XPathErrorMessages {

  String error0000(Object a);

  String currentNotAllowedInMatch();

  String currentTakesNoArgs();

  String documentReplaced();

  String contextHasNoOwnerdoc();

  String localnameHasTooManyArgs();

  String namespaceuriHasTooManyArgs();

  String normalizespaceHasTooManyArgs();

  String numberHasTooManyArgs();

  String nameHasTooManyArgs();

  String stringHasTooManyArgs();

  String stringlengthHasTooManyArgs();

  String translateTakes3Args();

  String unparsedentityuriTakes1Arg();

  String namespaceaxisNotImplemented();

  String unknownAxis(Object a);

  String unknownMatchOperation();

  String incorrectArgLength();

  String cantConvertToNumber(Object a);

  String cantConvertToNodelist(Object a);

  String cantConvertToMutablenodelist(Object a);

  String cantConvertToType(Object a, Object b);

  String expectedMatchPattern();

  String couldnotGetVarNamed(Object a);

  String unknownOpcode(Object a);

  String extraIllegalTokens(Object a);

  String expectedDoubleQuote();

  String expectedSingleQuote();

  String emptyExpression();

  String expectedButFound(Object a, Object b);

  String incorrectProgrammerAssertion(Object a);

  String booleanArgNoLongerOptional();

  String foundCommaButNoPrecedingArg();

  String foundCommaButNoFollowingArg();

  String predicateIllegalSyntax();

  String illegalAxisName(Object a);

  String unknownNodetype(Object a);

  String patternLiteralNeedsBeQuoted(Object a);

  String couldnotBeFormattedToNumber(Object a);

  String couldnotCreateXmlprocessorliaison(Object a);

  String didnotFindXpathSelectExp();

  String couldnotFindEndopAfterOplocationpath();

  String errorOccured();

  String illegalVariableReference(Object a);

  String axesNotAllowed(Object a);

  String keyHasTooManyArgs();

  String countTakes1Arg();

  String couldnotFindFunction(Object a);

  String unsupportedEncoding(Object a);

  String problemInDtmNextsibling();

  String cannotWriteToEmptynodelistimpl();

  String setdomfactoryNotSupported();

  String prefixMustResolve(Object a);

  String parseNotSupported(Object a);

  String saxApiNotHandled();

  String ignorableWhitespaceNotHandled();

  String dtmCannotHandleNodes(Object a);

  String xercesCannotHandleNodes(Object a);

  String xercesParseErrorDetails(Object a, Object b);

  String xercesParseError();

  String invalidUtf16Surrogate(Object a);

  String oierror();

  String cannotCreateUrl(Object a);

  String xpathReadobject(Object a);

  String functionTokenNotFound();

  String cannotDealXpathType(Object a);

  String nodesetNotMutable();

  String nodesetdtmNotMutable();

  String varNotResolvable(Object a);

  String nullErrorHandler();

  String progAssertUnknownOpcode(Object a);

  String zeroOrOne();

  String rtfNotSupportedXrtreefragselectwrapper();

  String asnodeiteratorNotSupportedXrtreefragselectwrapper();

  String detachNotSupportedXrtreefragselectwrapper();

  String numNotSupportedXrtreefragselectwrapper();

  String xstrNotSupportedXrtreefragselectwrapper();

  String strNotSupportedXrtreefragselectwrapper();

  String fsbNotSupportedXstringforchars();

  String couldNotFindVar(Object a);

  String xstringforcharsCannotTakeString();

  String faststringbufferCannotBeNull();

  String twoOrThree();

  String variableAccessedBeforeBind();

  String fsbCannotTakeString();

  String settingWalkerRootToNull();

  String nodesetdtmCannotIterate();

  String nodesetCannotIterate();

  String nodesetdtmCannotIndex();

  String nodesetCannotIndex();

  String cannotCallSetshouldcachenode();

  String onlyAllows(Object a, Object b);

  String unknownStep(Object a);

  String expectedRelLocPath();

  String expectedLocPath(Object a);

  String expectedLocPathAtEndExpr();

  String expectedLocStep();

  String expectedNodeTest();

  String expectedStepPattern();

  String expectedRelPathPattern();

  String cantConvertToBoolean(Object a, Object b);

  String cantConvertToSinglenode(Object a, Object b);

  String cantGetSnapshotLength(Object a, Object b);

  String nonIteratorType(Object a, Object b);

  String docMutated();

  String invalidXpathType(Object a);

  String emptyXpathResult();

  String incompatibleTypes(Object a, Object b, Object c);

  String nullResolver();

  String cantConvertToString(Object a, Object b);

  String nonSnapshotType(Object a, Object b);

  String wrongDocument();

  String wrongNodetype();

  String xpathError();

  String cantConvertXpathresulttypeToNumber(Object a, Object b);

  String extensionFunctionCannotBeInvoked(Object a);

  String resolveVariableReturnsNull(Object a);

  String unsupportedReturnType(Object a);

  String sourceReturnTypeCannotBeNull();

  String argCannotBeNull(Object a);

  String objectModelNull(Object a);

  String objectModelEmpty(Object a);

  String featureNameNull(Object a, Object b);

  String featureUnknown(Object a, Object b, Object c);

  String gettingNullFeature(Object a);

  String gettingUnknownFeature(Object a, Object b);

  String nullXpathFunctionResolver(Object a);

  String nullXpathVariableResolver(Object a);

  String localeNameNotHandled();

  String propertyNotSupported(Object a);

  String dontDoAnythingWithNs(Object a, Object b);

  String securityException(Object a);

  String quoNoLongerDefined();

  String needDerivedObjectToImplementNodetest();

  String cannotMakeUrlFrom(Object a);

  String expandEntitiesNotSupported();

  String uiLanguage();

  String helpLanguage();

  String language();

  String formatFailed();

  String version();

  String version2();

  String yes();

  String line();

  String column();

  String xsldone();

  String xpathOption();

  String optionin();

  String optionselect();

  String optionmatch();

  String optionanyexpr();

  String noparsermsg1();

  String noparsermsg2();

  String noparsermsg3();

  String noparsermsg4();

  String noparsermsg5();

  String gtone();

  String zero();

  String one();

  String two();

  String three();

}
