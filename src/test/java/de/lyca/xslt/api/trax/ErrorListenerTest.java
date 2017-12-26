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
 * $Id: ErrorListenerTest.java 470101 2006-11-01 21:03:00Z minchau $
 */

/*
 *
 * ErrorListenerTest.java
 *
 */
package de.lyca.xslt.api.trax;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Node;

import de.lyca.xslt.RecordingErrorListener;
import de.lyca.xslt.ResourceUtils;

//-------------------------------------------------------------------------

/**
 * Verify that ErrorListeners are called properly from Transformers. Also
 * verifies basic Transformer behavior after a stylesheet with errors has been
 * built. Note: parts of this test may rely on specific Xalan functionality, in
 * that with the specific errors I've chosen, Xalan can actually continue to
 * process the stylesheet, even though it had an error. XSLTC mode may either
 * throw slighly different kinds of errors, or may not be able to continue after
 * the error (we should investigate changing this test to just verify common
 * things, and then check the rest into the xalanj2 directory).
 * 
 * @author shane_curcuru@lotus.com
 */
@Ignore("Revisit error handling")
public class ErrorListenerTest {

  private static final String PACKAGE = '/' + ErrorListenerTest.class.getPackage().getName().replace('.', '/') + '/';
  private static final String ERR_PACKAGE = PACKAGE.replace("trax", "err");

  /** Expected type of error during stylesheet build. */
  protected int templatesExpectedType = 0;

  /** Expected String of error during stylesheet build. */
  protected String templatesExpectedValue = null;

  /** Expected type of error during transform. */
  protected int transformExpectedType = 0;

  /** Expected String of error during transform. */
  protected String transformExpectedValue = null;

  /** Subdirectory under test\tests\api for our xsl/xml files. */
  public static final String ERR_SUBDIR = "err";

  /** Name of expected parent test\tests\api directory. */
  public static final String API_PARENTDIR = "api";

  /**
   * Initialize this test
   * 
   * @param p
   *          Properties to initialize from (if needed)
   * @return false if we should abort the test; true otherwise
   */
  // @Override
  // public boolean doTestFileInit(Properties p)
  // {
  // // Used for all tests; just dump files in trax subdir
  // File outSubDir = new File(outputDir + File.separator + ERR_SUBDIR);
  // if (!outSubDir.mkdirs())
  // reporter.logWarningMsg("Could not create output dir: " + outSubDir);
  // // Initialize an output name manager to that dir with .out extension
  // outNames = new OutputNameManager(outputDir + File.separator + ERR_SUBDIR
  // + File.separator + testName, ".out");
  //
  // String testBasePath = inputDir
  // + File.separator
  // + ERR_SUBDIR
  // + File.separator;
  // String goldBasePath = goldDir
  // + File.separator
  // + ERR_SUBDIR
  // + File.separator;
  //
  // goodFileInfo.inputName = inputDir + File.separator
  // + "trax" + File.separator + "identity.xsl";
  // goodFileInfo.xmlName = inputDir + File.separator
  // + "trax" + File.separator + "identity.xml";
  // goodFileInfo.goldName = goldDir + File.separator
  // + "trax" + File.separator + "identity.out";
  //
  // testFileInfo.inputName = testBasePath + "ErrorListenerTest.xsl";
  // testFileInfo.xmlName = testBasePath + "ErrorListenerTest.xml";
  // testFileInfo.goldName = goldBasePath + "ErrorListenerTest.out";
  // templatesExpectedType = LoggingErrorListener.TYPE_FATALERROR;
  // templatesExpectedValue =
  // "decimal-format names must be unique. Name \"myminus\" has been duplicated";
  // transformExpectedType = LoggingErrorListener.TYPE_WARNING;
  // transformExpectedValue = "ExpectedMessage from:list1";
  //
  // return true;
  // }

  /**
   * Build a stylesheet/do a transform with a known-bad stylesheet. Verify that
   * the ErrorListener is called properly. Primarily using StreamSources.
   * 
   * @return false if we should abort the test; true otherwise
   */
  @Test
  public void testCase1() throws Exception {
    final String name = ERR_PACKAGE + "ErrorListenerTest";
    final TransformerFactory factory = TransformerFactory.newInstance();
    final RecordingErrorListener errorListener = new RecordingErrorListener();
    factory.setErrorListener(errorListener);
    factory.newTemplates(ResourceUtils.getSource(name + ".xsl"));
    Assert.assertEquals(1, errorListener.getWarnings().size());
    Assert.assertEquals(0, errorListener.getErrors().size());
    Assert.assertEquals(0, errorListener.getFatalErrors().size());
    Assert.assertTrue(errorListener.getWarnings().get(0).getMessage().contains("myminus"));
  }

  /**
   * Build a bad stylesheet/do a transform with SAX. Verify that the
   * ErrorListener is called properly. Primarily using SAXSources.
   * 
   * @return false if we should abort the test; true otherwise
   */
  @Test
  public void testCase2() throws Exception {
    final String name = ERR_PACKAGE + "ErrorListenerTest";
    final RecordingErrorListener errorListener = new RecordingErrorListener();
    final SAXTransformerFactory saxFactory = (SAXTransformerFactory) TransformerFactory.newInstance();
    saxFactory.setErrorListener(errorListener);
    saxFactory.newTransformerHandler(new SAXSource(ResourceUtils.getInputSource(name + ".xsl")));
    Assert.assertEquals(1, errorListener.getWarnings().size());
    Assert.assertEquals(0, errorListener.getErrors().size());
    Assert.assertEquals(0, errorListener.getFatalErrors().size());
    Assert.assertTrue(errorListener.getWarnings().get(0).getMessage().contains("myminus"));
  }

  /**
   * Build a bad stylesheet/do a transform with DOMs. Verify that the
   * ErrorListener is called properly. Primarily using DOMSources.
   * 
   * @return false if we should abort the test; true otherwise
   */
  @Test
  public void testCase3() throws Exception {
    final String name = ERR_PACKAGE + "ErrorListenerTest";
    final TransformerFactory factory = TransformerFactory.newInstance();
    final RecordingErrorListener errorListener = new RecordingErrorListener();
    factory.setErrorListener(errorListener);

    final DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
    dfactory.setNamespaceAware(true);
    final DocumentBuilder docBuilder = dfactory.newDocumentBuilder();
    final Node xslNode = docBuilder.parse(ResourceUtils.getInputSource(name + ".xsl"));
    final Node xmlNode = docBuilder.parse(ResourceUtils.getInputSource(name + ".xml"));

    final Templates templates = factory.newTemplates(new DOMSource(xslNode));

    Assert.assertEquals(1, errorListener.getWarnings().size());
    Assert.assertEquals(0, errorListener.getErrors().size());
    Assert.assertEquals(0, errorListener.getFatalErrors().size());
    Assert.assertTrue(errorListener.getWarnings().get(0).getMessage().contains("myminus"));

    errorListener.reset();

    final Transformer transformer = templates.newTransformer();
    transformer.setErrorListener(errorListener);

    final StringWriter writer = new StringWriter();
    transformer.transform(new DOMSource(xmlNode), new StreamResult(writer));

    Assert.assertEquals(ResourceUtils.readResource(name + ".out", StandardCharsets.UTF_8), writer.toString());
    Assert.assertEquals(2, errorListener.getWarnings().size());
    Assert.assertEquals("ExpectedMessage from:list1", errorListener.getWarnings().get(0).getMessage());
    Assert.assertEquals("ExpectedMessage from:list2", errorListener.getWarnings().get(1).getMessage());
    Assert.assertEquals(0, errorListener.getErrors().size());
    Assert.assertEquals(0, errorListener.getFatalErrors().size());
  }

  /**
   * Miscellaneous other ErrorListener tests. Includes Bugzilla1266. Primarily
   * using StreamSources.
   * 
   * @return false if we should abort the test; true otherwise
   */
  @Test
  public void testCase4() throws Exception {
    final String name = PACKAGE + "identity";
    final TransformerFactory factory = TransformerFactory.newInstance();
    factory.newTemplates(ResourceUtils.getSource(name + ".xsl"));

    final Templates templates = factory.newTemplates(ResourceUtils.getSource(name + ".xsl"));
    final Transformer transformer = templates.newTransformer();
    final RecordingErrorListener errorListener = new RecordingErrorListener();
    transformer.setErrorListener(errorListener);

    // TODO at least a warning should be issued
    transformer.setOutputProperty(OutputKeys.ENCODING, "illegal-encoding-value");

    Assert.assertEquals(0, errorListener.getWarnings().size());
    Assert.assertEquals(0, errorListener.getErrors().size());
    Assert.assertEquals(0, errorListener.getFatalErrors().size());

    final StringWriter writer = new StringWriter();
    transformer.transform(ResourceUtils.getSource(name + ".xml"), new StreamResult(writer));

    Assert.assertEquals(2, errorListener.getWarnings().size());
    Assert.assertEquals(0, errorListener.getErrors().size());
    Assert.assertEquals(0, errorListener.getFatalErrors().size());

    // reporter.logTraceMsg("about to transform(...)");
    // transformer.transform(new
    // StreamSource(QetestUtils.filenameToURL(goodFileInfo.xmlName)), new
    // StreamResult(outNames.nextName()));
    // reporter.logTraceMsg("after transform(...)");
    // reporter.logStatusMsg("loggingErrorListener after transform:" +
    // loggingErrorListener.getQuickCounters());
    //
    // // Validate that one warning (about illegal-encoding-value) should have
    // been reported
    // int[] errCtr = loggingErrorListener.getCounters();
    // reporter.logErrorMsg("Validation of warning throw Moved to Bugzilla1266.java Oct-01 -sc");
    // /*
    // * **** Moved to Bugzilla1266.java Oct-01 -sc
    // reporter.check((errCtr[LoggingErrorListener.TYPE_WARNING] > 0), true,
    // * "At least one Warning listned to for illegal-encoding-value");*** Moved
    // to Bugzilla1266.java Oct-01 -sc ****
    // */
    //
    // // Validate the actual output file as well: in this case,
    // // the stylesheet should still work
    // fileChecker.check(reporter, new File(outNames.currentName()), new
    // File(goodFileInfo.goldName),
    // "transform of good xsl w/bad output props into: " +
    // outNames.currentName());

  }

}
