/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the  "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * $Id: SAXResultAPITest.java 470101 2006-11-01 21:03:00Z minchau $
 */

/*
 *
 * SAXResultAPITest.java
 *
 */
package de.lyca.xslt.api.trax.sax;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

import org.junit.Test;
import org.xml.sax.ContentHandler;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

//-------------------------------------------------------------------------

/**
 * API Coverage test for the SAXResult class of TRAX.
 * 
 * @author shane_curcuru@lotus.com
 * @version $Id: SAXResultAPITest.java 470101 2006-11-01 21:03:00Z minchau $
 */
public class SAXResultAPITest {

  private static final String PACKAGE = '/' + SAXResultAPITest.class.getPackage().getName().replace('.', '/') + "/";

  /** Nonsense systemId for various tests. */
  public static final String NONSENSE_SYSTEMID = "file:///nonsense-system-id";

  // Note these are initialized as strings denoting filenames,
  // and *not* as URL/URI's
  // testFileInfo.inputName = testBasePath + "SAXTest.xsl";
  // testFileInfo.xmlName = testBasePath + "SAXTest.xml";
  // testFileInfo.goldName = goldBasePath + "SAXTest.out";
  //
  // impInclFileInfo.inputName = testBasePath + "SAXImpIncl.xsl";
  // impInclFileInfo.xmlName = testBasePath + "SAXImpIncl.xml";
  // impInclFileInfo.goldName = goldBasePath + "SAXImpIncl.out";
  //
  // dtdFileInfo.inputName = testBasePath + "SAXdtd.xsl";
  // dtdFileInfo.xmlName = testBasePath + "SAXdtd.xml";
  // dtdFileInfo.goldName = goldBasePath + "SAXdtd.out";

  /**
   * Basic API coverage, constructor and set/get methods.
   */
  @Test
  public void testCase1() throws Exception {
    // Default no-arg ctor sets nothing
    SAXResult defaultSAX = new SAXResult();
    assertNull("Default SAXResult should have null Handler.", defaultSAX.getHandler());
    assertNull("Default SAXResult should have null LexicalHandler.", defaultSAX.getLexicalHandler());
    assertNull("Default SAXResult should have null SystemId.", defaultSAX.getSystemId());

    SAXTransformerFactory saxTransformerFactory = (SAXTransformerFactory) TransformerFactory.newInstance();

    // ctor(Handler) with identity transformer, which is both a ContentHandler
    // and LexicalHandler with identity transformer
    TransformerHandler transformerHandler = saxTransformerFactory.newTransformerHandler();
    SAXResult handlerSAX = new SAXResult(transformerHandler);
    assertEquals("SAXResult(handler) should have Handler.", transformerHandler, handlerSAX.getHandler());
    assertNull("SAXResult(handler) should have null LexicalHandler", handlerSAX.getLexicalHandler());
    assertNull("SAXResult(handler) should have null SystemId", handlerSAX.getSystemId());

    // ctor(Handler) with LoggingContentHandler, which not a LexicalHandler, so
    // it can't be cast
    ContentHandler nonLexHandler = new DefaultHandler();
    SAXResult otherHandlerSAX = new SAXResult(nonLexHandler);
    assertEquals("SAXResult(non-lexhandler) should have Handler.", nonLexHandler, otherHandlerSAX.getHandler());
    assertNull("SAXResult(non-lexhandler) should have null LexicalHandler when ContentHandler!=LexicalHandler",
            otherHandlerSAX.getLexicalHandler());
    assertNull("SAXResult(non-lexhandler) should have null SystemId", otherHandlerSAX.getSystemId());

    // Note the Javadoc in SAXResult which talks about automatically casting the
    // ContentHandler into a LexicalHandler: this cannot be tested alone here,
    // since it's the Transformer that does that internally if necessary, and it
    // may not get set back into the SAXResult object itself

    // set/getHandler API coverage
    SAXResult wackySAX = new SAXResult();
    wackySAX.setHandler(transformerHandler); // isa LexicalHandler also
    assertEquals("SAXResult() set/getHandler API coverage.", transformerHandler, wackySAX.getHandler());
    assertNull("SAXResult() getLexicalHandler after set/getHandler.", wackySAX.getLexicalHandler());

    // set/getLexicalHandler API coverage
    LexicalHandler lexHandler = new DefaultHandler2();
    wackySAX.setLexicalHandler(lexHandler);
    assertEquals("SAXResult() set/getLexicalHandler API coverage.", lexHandler, wackySAX.getLexicalHandler());
    assertEquals("SAXResult() set/getHandler API coverage is not affected by LexicalHandler.", transformerHandler,
            wackySAX.getHandler());

    // set/getHandler API coverage, setting to null, which should work here but
    // can't be used legally
    wackySAX.setHandler(null);
    assertNull("SAXResult() setHandler(null).", wackySAX.getHandler());
    assertNotNull("SAXResult() getLexicalHandler().", wackySAX.getLexicalHandler());

    wackySAX.setLexicalHandler(null);
    assertNull("SAXResult() setLexicalHandler(null).", wackySAX.getLexicalHandler());

    // set/getSystemId API coverage
    wackySAX.setSystemId(NONSENSE_SYSTEMID);
    assertEquals("SAXResult() set/getSystemId API coverage.", NONSENSE_SYSTEMID, wackySAX.getSystemId());
    wackySAX.setSystemId(null);
    assertNull("SAXResult() setSystemId(null).", wackySAX.getSystemId());
  }

  /**
   * Basic functionality of SAXResults.
   * 
   * @return false if we should abort the test; true otherwise
   */
  // public boolean testCase2() {
  // reporter.testCaseInit("Basic functionality of SAXResults");
  // // Provide local copies of URLized filenames, so that we can
  // // later run tests with either Strings or URLs
  // String xslURI = QetestUtils.filenameToURL(testFileInfo.inputName);
  // String xmlURI = QetestUtils.filenameToURL(testFileInfo.xmlName);
  // String xslImpInclURI =
  // QetestUtils.filenameToURL(impInclFileInfo.inputName);
  // String xmlImpInclURI = QetestUtils.filenameToURL(impInclFileInfo.xmlName);
  //
  // TransformerFactory factory = null;
  // SAXTransformerFactory saxFactory = null;
  // Templates streamTemplates;
  // try {
  // factory = TransformerFactory.newInstance();
  // factory.setErrorListener(new DefaultErrorHandler());
  // saxFactory = (SAXTransformerFactory) factory;
  // // Process a simple stylesheet for use later
  // reporter.logTraceMsg("factory.newTemplates(new StreamSource(" + xslURI +
  // "))");
  // streamTemplates = factory.newTemplates(new StreamSource(xslURI));
  // } catch (Throwable t) {
  // reporter.checkFail("Problem creating factory; can't continue testcase");
  // reporter.logThrowable(reporter.ERRORMSG, t,
  // "Problem creating factory; can't continue testcase");
  // return true;
  // }
  // try {
  // // Verify very simple use of just a SAXResult
  // // Use simple Xalan serializer for disk output, setting
  // // the stylesheet's output properties into it
  // Properties outProps = streamTemplates.getOutputProperties();
  // // Use a TransformerHandler for serialization: this
  // // supports ContentHandler and can replace the
  // // Xalan/Xerces specific Serializers we used to use
  // TransformerHandler tHandler = saxFactory.newTransformerHandler();
  // FileOutputStream fos = new FileOutputStream(outNames.nextName());
  // // Serializer serializer = SerializerFactory.getSerializer(outProps);
  // // reporter.logTraceMsg("serializer.setOutputStream(new FileOutputStream("
  // // + outNames.currentName() + ")");
  // // serializer.setOutputStream(fos);
  // // SAXResult saxResult = new SAXResult(serializer.asContentHandler()); //
  // // use other ContentHandler
  // Result realResult = new StreamResult(fos);
  // tHandler.setResult(realResult);
  // SAXResult saxResult = new SAXResult(tHandler);
  //
  // // Just do a normal transform to this result
  // Transformer transformer = streamTemplates.newTransformer();
  // transformer.setErrorListener(new DefaultErrorHandler());
  // reporter.logTraceMsg("transform(new StreamSource(" + xmlURI +
  // "), saxResult)");
  // transformer.transform(new StreamSource(xmlURI), saxResult);
  // fos.close(); // must close ostreams we own
  // fileChecker.check(reporter, new File(outNames.currentName()), new
  // File(testFileInfo.goldName),
  // "simple transform into SAXResult into: " + outNames.currentName());
  //
  // } catch (Throwable t) {
  // reporter.checkFail("Basic functionality of SAXResults threw: " +
  // t.toString());
  // reporter.logThrowable(reporter.ERRORMSG, t,
  // "Basic functionality of SAXResults");
  // }
  //
  // try {
  // // Negative test: SAXResult without a handler should throw
  // SAXResult saxResult = new SAXResult();
  //
  // // Just do a normal transform to this result
  // Transformer transformer = streamTemplates.newTransformer();
  // transformer.setErrorListener(new DefaultErrorHandler());
  // reporter.logTraceMsg("transform(..., nullsaxResult)");
  // transformer.transform(new StreamSource(xmlURI), saxResult);
  // reporter.checkFail("transform(..., nullsaxResult) should have thrown exception");
  // } catch (IllegalArgumentException iae) {
  // // This is the exception we expect, so pass (and don't
  // // bother displaying the full logThrowable)
  // reporter.checkPass("transform(..., nullsaxResult) properly threw: " +
  // iae.toString());
  // } catch (Throwable t) {
  // reporter.checkFail("transform(..., nullsaxResult) unexpectedly threw: " +
  // t.toString());
  // reporter.logThrowable(reporter.ERRORMSG, t,
  // "transform(..., nullsaxResult) threw");
  // }
  // reporter.testCaseClose();
  // return true;
  // }

  /**
   * Detailed functionality of SAXResults: setLexicalHandler.
   * 
   * @return false if we should abort the test; true otherwise
   */
  // public boolean testCase3() {
  // reporter.testCaseInit("Detailed functionality of SAXResults: setLexicalHandler");
  // String xslURI = QetestUtils.filenameToURL(dtdFileInfo.inputName);
  // String xmlURI = QetestUtils.filenameToURL(dtdFileInfo.xmlName);
  //
  // TransformerFactory factory = null;
  // SAXTransformerFactory saxFactory = null;
  // Templates streamTemplates;
  // try {
  // factory = TransformerFactory.newInstance();
  // factory.setErrorListener(new DefaultErrorHandler());
  // saxFactory = (SAXTransformerFactory) factory;
  // // Process a simple stylesheet for use later
  // reporter.logTraceMsg("factory.newTemplates(new StreamSource(" + xslURI +
  // "))");
  // streamTemplates = factory.newTemplates(new StreamSource(xslURI));
  // } catch (Throwable t) {
  // reporter.checkFail("Problem creating factory; can't continue testcase");
  // reporter.logThrowable(reporter.ERRORMSG, t,
  // "Problem creating factory; can't continue testcase");
  // return true;
  // }
  // try {
  // // Validate a StreamSource to a logging SAXResult,
  // // where we validate the actual events passed
  // // through the SAXResult's ContentHandler and
  // // LexicalHandler
  //
  // // Have an actual handler that does the physical output
  // reporter.logInfoMsg("TransformerHandler.setResult(StreamResult)");
  // TransformerHandler tHandler = saxFactory.newTransformerHandler();
  // FileOutputStream fos = new FileOutputStream(outNames.nextName());
  // Result realResult = new StreamResult(fos);
  // tHandler.setResult(realResult);
  //
  // SAXResult saxResult = new SAXResult();
  // // Add a contentHandler that logs out info about the
  // // transform, and that passes-through calls back
  // // to the original tHandler
  // reporter.logInfoMsg("loggingSaxResult.setHandler(loggingContentHandler)");
  // LoggingContentHandler lch = new LoggingContentHandler((Logger) reporter);
  // lch.setDefaultHandler(tHandler);
  // saxResult.setHandler(lch);
  //
  // // Add a lexicalHandler that logs out info about the
  // // transform, and that passes-through calls back
  // // to the original tHandler
  // reporter.logInfoMsg("loggingSaxResult.setLexicalHandler(loggingLexicalHandler)");
  // LoggingLexicalHandler llh = new LoggingLexicalHandler((Logger) reporter);
  // llh.setDefaultHandler(tHandler);
  // saxResult.setLexicalHandler(llh);
  //
  // // Just do a normal transform to this result
  // Transformer transformer = streamTemplates.newTransformer();
  // transformer.setErrorListener(new DefaultErrorHandler());
  // reporter.logTraceMsg("transform(new StreamSource(" + xmlURI +
  // "), loggingSaxResult)");
  // transformer.transform(new StreamSource(xmlURI), saxResult);
  // fos.close(); // must close ostreams we own
  // reporter.logStatusMsg("Closed result stream from loggingSaxResult, about to check result");
  // fileChecker.check(reporter, new File(outNames.currentName()), new
  // File(dtdFileInfo.goldName),
  // "transform loggingSaxResult into: " + outNames.currentName());
  // reporter.logWarningMsg("//@todo validate that llh got lexical events: Bugzilla#888");
  // reporter.logWarningMsg("//@todo validate that lch got content events");
  // } catch (Throwable t) {
  // reporter.checkFail("Basic functionality1 of SAXResults threw: " +
  // t.toString());
  // reporter.logThrowable(reporter.ERRORMSG, t,
  // "Basic functionality1 of SAXResults");
  // }
  // try {
  // // Same as above, with identityTransformer
  // reporter.logInfoMsg("TransformerHandler.setResult(StreamResult)");
  // TransformerHandler tHandler = saxFactory.newTransformerHandler();
  // FileOutputStream fos = new FileOutputStream(outNames.nextName());
  // Result realResult = new StreamResult(fos);
  // tHandler.setResult(realResult);
  //
  // SAXResult saxResult = new SAXResult();
  // reporter.logInfoMsg("loggingSaxResult.setHandler(loggingContentHandler)");
  // LoggingContentHandler lch = new LoggingContentHandler((Logger) reporter);
  // lch.setDefaultHandler(tHandler);
  // saxResult.setHandler(lch);
  //
  // reporter.logInfoMsg("loggingSaxResult.setLexicalHandler(loggingLexicalHandler)");
  // LoggingLexicalHandler llh = new LoggingLexicalHandler((Logger) reporter);
  // llh.setDefaultHandler(tHandler);
  // saxResult.setLexicalHandler(llh);
  //
  // // Do an identityTransform to this result
  // Transformer identityTransformer =
  // TransformerFactory.newInstance().newTransformer();
  // identityTransformer.setErrorListener(new DefaultErrorHandler());
  // reporter.logTraceMsg("identityTransform(new StreamSource(" + xmlURI +
  // "), loggingSaxResult)");
  // identityTransformer.transform(new StreamSource(xmlURI), saxResult);
  // fos.close(); // must close ostreams we own
  // reporter.logStatusMsg("Closed result stream from loggingSaxResult, about to check result");
  // fileChecker.check(reporter, new File(outNames.currentName()), new
  // File(dtdFileInfo.xmlName),
  // "identity transform loggingSaxResult into: " + outNames.currentName());
  // reporter.logWarningMsg("//@todo validate that llh got lexical events: Bugzilla#888");
  // reporter.logWarningMsg("//@todo validate that lch got content events");
  // } catch (Throwable t) {
  // reporter.checkFail("Basic functionality2 of SAXResults threw: " +
  // t.toString());
  // reporter.logThrowable(reporter.ERRORMSG, t,
  // "Basic functionality2 of SAXResults");
  // }
  // try {
  // // Validate a DOMSource to a logging SAXResult, as above
  // DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
  // dfactory.setNamespaceAware(true);
  // DocumentBuilder docBuilder = dfactory.newDocumentBuilder();
  // reporter.logTraceMsg("docBuilder.parse(" + xmlURI + ")");
  // Node xmlNode = docBuilder.parse(new InputSource(xmlURI));
  //
  // // Have an actual handler that does the physical output
  // TransformerHandler tHandler = saxFactory.newTransformerHandler();
  // FileOutputStream fos = new FileOutputStream(outNames.nextName());
  // Result realResult = new StreamResult(fos);
  // tHandler.setResult(realResult);
  //
  // SAXResult saxResult = new SAXResult();
  // // Add a contentHandler that logs out info about the
  // // transform, and that passes-through calls back
  // // to the original tHandler
  // LoggingContentHandler lch = new LoggingContentHandler((Logger) reporter);
  // lch.setDefaultHandler(tHandler);
  // saxResult.setHandler(lch);
  //
  // // Add a lexicalHandler that logs out info about the
  // // transform, and that passes-through calls back
  // // to the original tHandler
  // LoggingLexicalHandler llh = new LoggingLexicalHandler((Logger) reporter);
  // llh.setDefaultHandler(tHandler);
  // saxResult.setLexicalHandler(llh);
  //
  // // Just do a normal transform to this result
  // Transformer transformer = streamTemplates.newTransformer();
  // transformer.setErrorListener(new DefaultErrorHandler());
  // reporter.logTraceMsg("transform(new DOMSource(" + xmlURI +
  // "), loggingSaxResult)");
  // transformer.transform(new DOMSource(xmlNode), saxResult);
  // fos.close(); // must close ostreams we own
  // reporter.logStatusMsg("Closed result stream from loggingSaxResult, about to check result");
  // fileChecker.check(reporter, new File(outNames.currentName()), new
  // File(dtdFileInfo.goldName),
  // "transform DOM-loggingSaxResult into: " + outNames.currentName());
  // reporter.logWarningMsg("//@todo validate that llh got lexical events: Bugzilla#888");
  // reporter.logWarningMsg("//@todo validate that lch got content events");
  // } catch (Throwable t) {
  // reporter.checkFail("Basic functionality3 of SAXResults threw: " +
  // t.toString());
  // reporter.logThrowable(reporter.ERRORMSG, t,
  // "Basic functionality3 of SAXResults");
  // }
  // reporter.testCaseClose();
  // return true;
  // }

}
