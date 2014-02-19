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
 * $Id: ParameterTest.java 470101 2006-11-01 21:03:00Z minchau $
 */

/*
 *
 * ParameterTest.java
 *
 */
package de.lyca.xslt.api.trax;

import static de.lyca.xslt.ResourceUtils.getSource;

import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.junit.Assert;
import org.junit.Test;

//-------------------------------------------------------------------------

/**
 * Functional test of various usages of parameters in transforms.
 * 
 * @author Original: shane_curcuru@lotus.com
 * @author Refactored: l.michele@lyca.de
 */
public class ParameterTest {

  private static final String PACKAGE = '/' + ExamplesTest.class.getPackage().getName().replace('.', '/') + "/";

  private static final String FIRST = "<outp>ABC,<B>ABC</B>; DEF,<B>DEF</B>; GHI,<B>GHI</B>; </outp>"
          + System.lineSeparator();
  private static final String FIRST_RESULT = "<outp>ABC,<B>ABC</B>; DEF,<B>DEF</B>; GHI,<B>GHI</B>; </outp>";
  private static final String SECOND = "<outs>s1val,s1val; s2val,s2val; s3val,s3val; </outs>" + System.lineSeparator();
  private static final String SECOND_RESULT = "<outs>s1val,s1val; s2val,s2val; s3val,s3val; </outs>";
  private static final String THIRD = "<outt>true-notset,false-blank,false-a,false-1,notset</outt>"
          + System.lineSeparator();
  private static final String FOURTH = "  1" + System.lineSeparator();
  private static final String FIFTH = "  Item-2-found" + System.lineSeparator();
  private static final String SIXTH = "  3" + System.lineSeparator();

  // paramTest.inputName = QetestUtils.filenameToURL(testBasePath +
  // "ParameterTest.xsl");
  // paramTest.xmlName = QetestUtils.filenameToURL(testBasePath +
  // "ParameterTest.xml");
  //
  // paramTest2.inputName = QetestUtils.filenameToURL(testBasePath +
  // "ParameterTest2.xsl");
  // paramTest2.xmlName = QetestUtils.filenameToURL(testBasePath +
  // "ParameterTest2.xml");

  // <?xml version="1.0" encoding="UTF-8"?><outp>ABC,<B>ABC</B>; DEF,<B>DEF</B>;
  // GHI,<B>GHI</B>; </outp>:
  // <outs>s1val,s1val; s2val,s2val; s3val,s3val; </outs>:
  // <outt>true-notset,false-blank,false-a,false-1,notset</outt>
  // 1
  // Item-2-found
  // 3

  /** Array of test data for parameter testing. */
  protected String paramTests[][] = {
          // { paramName to test, paramValue to test expected output string,
          // description of the test }
          { "t1", "'a'", "<outt>false-notset,false-blank,false-a,false-1,'a'</outt>",
                  "(10)Select expr of a 'param' string" },
          { "t1", "a", "<outt>false-notset,false-blank,true-a,false-1,a</outt>", "(10a)Select expr of a param string" },
          { "t1", "'1'", "<outt>false-notset,false-blank,false-a,false-1,'1'</outt>",
                  "(11)Select expr of a 'param' number" },
          { "t1", "1", "<outt>false-notset,false-blank,false-a,true-1,1</outt>", "(11a)Select expr of a param number" },
          { "t1", "''", "<outt>false-notset,false-blank,false-a,false-1,''</outt>",
                  "(12)Select expr of a param 'blank' string" },
          { "t1", "", "<outt>false-notset,true-blank,false-a,false-1,</outt>",
                  "(12a)Select expr of a param blank string" },
          // { "t1", null,
          // "<outt>false-notset,false-blank,false-a,false-1,</outt>",
          // "(12b)Select expr of a null" },
          { "p1", "'foo'", "<outp>'foo','foo'; DEF,<B>DEF</B>; GHI,<B>GHI</B>; </outp>",
                  "(13)Stylesheet with literal 'param' value" },
          { "p1", "foo", "<outp>foo,foo; DEF,<B>DEF</B>; GHI,<B>GHI</B>; </outp>",
                  "(13a)Stylesheet with literal param value" },
          { "p1", "'bar'", "<outp>'bar','bar'; DEF,<B>DEF</B>; GHI,<B>GHI</B>; </outp>",
                  "(14)Stylesheet with replaced/another literal 'param' value" },
          { "p1", "bar", "<outp>bar,bar; DEF,<B>DEF</B>; GHI,<B>GHI</B>; </outp>",
                  "(14a)Stylesheet with replaced/another literal param value" },
          {
                  "p2",
                  "'&lt;item&gt;bar&lt;/item&gt;'",
                  "<outp>ABC,<B>ABC</B>; '&amp;lt;item&amp;gt;bar&amp;lt;/item&amp;gt;','&amp;lt;item&amp;gt;bar&amp;lt;/item&amp;gt;'; GHI,<B>GHI</B>; </outp>",
                  "(15)Stylesheet with 'param' value with nodes" },
          {
                  "p2",
                  "&lt;item&gt;bar&lt;/item&gt;",
                  "<outp>ABC,<B>ABC</B>; &amp;lt;item&amp;gt;bar&amp;lt;/item&amp;gt;,&amp;lt;item&amp;gt;bar&amp;lt;/item&amp;gt;; GHI,<B>GHI</B>; </outp>",
                  "(15a)Stylesheet with param value with nodes" },
          // TODO { "p3", "'foo3'", FIRST_RESULT,
          // "(16)Stylesheet with literal 'param' value in a template, is not passed"
          // },
          // TODO { "p3", "foo3", FIRST_RESULT,
          // "(16a)Stylesheet with literal param value in a template, is not passed"
          // },
          { "s1", "'foos'", "<outs>'foos','foos'; s2val,s2val; s3val,s3val; </outs>",
                  "(17)Stylesheet with literal 'param' select" },
          { "s1", "foos", "<outs>foos,foos; s2val,s2val; s3val,s3val; </outs>",
                  "(17a)Stylesheet with literal param select" },
          { "s1", "'bars'", "<outs>'bars','bars'; s2val,s2val; s3val,s3val; </outs>",
                  "(18)Stylesheet with replaced/another literal 'param' select" },
          { "s1", "bars", "<outs>bars,bars; s2val,s2val; s3val,s3val; </outs>",
                  "(18a)Stylesheet with replaced/another literal param select" },
          { "s2", "'&lt;item/&gt;'",
                  "<outs>s1val,s1val; '&amp;lt;item/&amp;gt;','&amp;lt;item/&amp;gt;'; s3val,s3val; </outs>",
                  "(19)Stylesheet with nodes(?) 'param' select" },
          { "s2", "&lt;item/&gt;",
                  "<outs>s1val,s1val; &amp;lt;item/&amp;gt;,&amp;lt;item/&amp;gt;; s3val,s3val; </outs>",
                  "(19a)Stylesheet with nodes(?) param select" },
  // TODO { "s3", "foos3", SECOND_RESULT,
  // "(20)Stylesheet with literal 'param' select in a template, is not passed" }
  }; // end of paramTests array

  private String buildExpectedResult(String param, String result) {
    final StringBuilder sb = new StringBuilder();
    if (param != null) {
      switch (param.charAt(0)) {
        case 'p':
          sb.append(result).append(System.lineSeparator()).append(SECOND).append(THIRD);
          break;
        case 's':
          sb.append(FIRST).append(result).append(System.lineSeparator()).append(THIRD);
          break;
        case 't':
          sb.append(FIRST).append(SECOND).append(result).append(System.lineSeparator());
          break;
      }
    } else {
      sb.append(FIRST).append(SECOND).append(THIRD);
    }
    sb.append(FOURTH).append(FIFTH).append(SIXTH);
    return sb.toString();
  }

  @Test
  public void testParam1() throws Exception {
    final String name = PACKAGE + "ParameterTest";
    // TransformerFactory transformerFactory = TransformerFactory.newInstance(
    // "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl",
    // null);
    final TransformerFactory transformerFactory = TransformerFactory.newInstance();
    final Templates templates = transformerFactory.newTemplates(getSource(name + ".xsl"));
    for (int i = 0; i < paramTests.length; i++) {
      final Transformer transformer = templates.newTransformer();
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      transformer.setParameter(paramTests[i][0], paramTests[i][1]);
      final StringWriter stringWriter = new StringWriter();
      transformer.transform(getSource(name + ".xml"), new StreamResult(stringWriter));
      Assert.assertEquals(paramTests[i][3], buildExpectedResult(paramTests[i][0], paramTests[i][2]),
              stringWriter.toString());
    }
  }

  /**
   * Setting various string-valued params. Just loops through array of simple
   * test data.
   * 
   * @return false if we should abort the test; true otherwise
   */
  // public boolean testCase1() {
  // reporter.testCaseInit("Setting various simple string-valued params");
  // try {
  // // Just loop through test elements and try each one
  // // Loop separately for each worker method
  // for (int i = 0; i < paramTests.length; i++) {
  // // Try on a completely independent
  // // transformer and sources each time
  // testSetParam(paramTests[i][0], paramTests[i][1], new
  // StreamSource(paramTest.xmlName), new StreamSource(paramTest.inputName),
  // paramTests[i][2], paramTests[i][3]);
  // }
  // } catch (Exception e) {
  // reporter.logThrowable(Logger.ERRORMSG, e, "Testcase threw");
  // reporter.logErrorMsg("Testcase threw: " + e.toString());
  // }
  // reporter.testCaseClose();
  // return true;
  // }

  /**
   * Reuse the same transformer multiple times with params set. This also
   * reproduces Bugzilla1611
   * 
   * @return false if we should abort the test; true otherwise
   */
  // public boolean testCase2() {
  // reporter.testCaseInit("Reuse the same transformer multiple times with params set");
  // TransformerFactory factory = null;
  // Templates templates = null;
  // Transformer transformer = null;
  // try {
  // factory = TransformerFactory.newInstance();
  // templates = factory.newTemplates(new StreamSource(paramTest2.inputName));
  //
  // // Process the file as-is, without any params set
  // transformer = templates.newTransformer();
  // reporter.logInfoMsg("Transforming " + paramTest.xmlName + " with " +
  // paramTest2.inputName);
  // transformer.transform(new StreamSource(paramTest.xmlName), new
  // StreamResult(outNames.nextName()));
  // // Verify the values are correct for no params set
  // checkFileContains(outNames.currentName(),
  // "<globalVarAttr>ParameterTest.xml:</globalVarAttr>",
  // "(2.0)Processing 1,2 w/no params into: " + outNames.currentName());
  //
  // // Do NOT call clearParameters here; reuse the transformer
  // reporter.logInfoMsg("Reused-Transforming " + paramTest2.xmlName + " with "
  // + paramTest2.inputName);
  // transformer.transform(new StreamSource(paramTest2.xmlName), new
  // StreamResult(outNames.nextName()));
  // // Verify the values are correct for no params set
  // checkFileContains(outNames.currentName(),
  // "<globalVarAttr>ParameterTest2.xml:</globalVarAttr>",
  // "(2.0a) Bugzilla1611 Reused Transformer processing 2,2 w/no params into: "
  // + outNames.currentName());
  //
  // // Do NOT call clearParameters here; reuse the transformer again
  // reporter.logInfoMsg("Reused-Transforming-again " + paramTest.xmlName +
  // " with " + paramTest2.inputName);
  // transformer.transform(new StreamSource(paramTest.xmlName), new
  // StreamResult(outNames.nextName()));
  // // Verify the values are correct for no params set
  // checkFileContains(outNames.currentName(),
  // "<globalVarAttr>ParameterTest.xml:</globalVarAttr>",
  // "(2.0b) Bugzilla1611 Reused-Again Transformer processing 1,2 w/no params into: "
  // + outNames.currentName());
  // } catch (Exception e) {
  // reporter.logThrowable(Logger.ERRORMSG, e, "Testcase threw");
  // reporter.logErrorMsg("Testcase threw: " + e.toString());
  // }
  // reporter.testCaseClose();
  // return true;
  // }

  /**
   * Setting various string-valued params and reusing transformers. Creates one
   * transformer first, then loops through array of simple test data re-using
   * transformer.
   * 
   * @return false if we should abort the test; true otherwise
   */
  // public boolean testCase3() {
  // reporter.testCaseInit("Setting various string-valued params and re-using transformer");
  // TransformerFactory factory = null;
  // Templates templates = null;
  // Transformer transformer = null;
  // try {
  // factory = TransformerFactory.newInstance();
  // factory.setErrorListener(new DefaultErrorHandler());
  // templates = factory.newTemplates(new StreamSource(paramTest.inputName));
  // } catch (Exception e) {
  // reporter.checkFail("Problem creating Templates; cannot continue testcase");
  // reporter.logThrowable(Logger.ERRORMSG, e,
  // "Problem creating Templates; cannot continue testcase");
  // return true;
  // }
  //
  // try {
  // // Process the file as-is, without any params set
  // transformer = templates.newTransformer();
  // transformer.setErrorListener(new DefaultErrorHandler());
  // transformer.transform(new StreamSource(paramTest.xmlName), new
  // StreamResult(outNames.nextName()));
  // transformer.clearParameters();
  // // Verify each of the three kinds of params are correct
  // checkFileContains(outNames.currentName(),
  // "<outp>ABC,<B>ABC</B>; DEF,<B>DEF</B>; GHI,<B>GHI</B>; </outp>",
  // "(0) Stylesheet with default param value into: " + outNames.currentName());
  //
  // checkFileContains(outNames.currentName(),
  // "<outs>s1val,s1val; s2val,s2val; s3val,s3val; </outs>",
  // "(1) ... also with default param value in select expr into: " +
  // outNames.currentName());
  // checkFileContains(outNames.currentName(),
  // "<outt>true-notset,false-blank,false-a,false-1,notset</outt>",
  // "(2) ... also with default param value in select expr into: " +
  // outNames.currentName());
  //
  // // Just loop through test elements and try each one
  // for (int i = 0; i < paramTests.length; i++) {
  // // Re-use the transformer from above for each test
  // transformer.clearParameters();
  // testSetParam(paramTests[i][0], paramTests[i][1], transformer, new
  // StreamSource(paramTest.xmlName), new StreamSource(
  // paramTest.inputName), paramTests[i][2], paramTests[i][3]);
  // }
  // } catch (Exception e) {
  // reporter.logThrowable(Logger.ERRORMSG, e, "Testcase threw");
  // reporter.logErrorMsg("Testcase threw: " + e.toString());
  // }
  // reporter.testCaseClose();
  // return true;
  // }

  /**
   * Test setting a single string-valued parameter. Uses the supplied
   * Transformer and calls setParameter() then transform(Source, Source), then
   * uses the worker method checkFileContains() to validate and output results.
   * 
   * @param paramName
   *          simple name of parameter
   * @param paramVal
   *          String value of parameter
   * @param transformer
   *          object to use
   * @param xmlSource
   *          object to use in transform
   * @param xslStylesheet
   *          object to use in transform
   * @param checkString
   *          to look for in output file (logged)
   * @param comment
   *          to log with check() call
   * @return true if pass, false otherwise
   */
  // protected boolean testSetParam(String paramName, String paramVal,
  // Transformer transformer, Source xmlSource, Source xslStylesheet,
  // String checkString, String comment) {
  // try {
  // reporter.logTraceMsg("setParameter(" + paramName + ", " + paramVal + ")");
  // transformer.setParameter(paramName, paramVal);
  // reporter.logTraceMsg("transform(" + xmlSource.getSystemId() + ", " +
  // xslStylesheet.getSystemId() + ", ...)");
  // transformer.transform(xmlSource, new StreamResult(outNames.nextName()));
  // } catch (Throwable t) {
  // reporter.logThrowable(Logger.ERRORMSG, t,
  // "testSetParam unexpectedly threw");
  // reporter.logErrorMsg("//@todo HACK: intermittent NPE; please report to curcuru@apache.org if you get this");
  // reporter.logErrorMsg("//@todo HACK: intermittent NPE; please report to curcuru@apache.org if you get this");
  // reporter.logErrorMsg("//@todo HACK: intermittent NPE; please report to curcuru@apache.org if you get this");
  // // Since we the NPE is intermittent, and we want the rest
  // // of this test in the smoketest, I'll go against my
  // // better nature and ignore this fail
  // return true; // HACK: should be removed when fixed
  // }
  // return checkFileContains(outNames.currentName(), checkString, "Reused:" +
  // comment + " into: " + outNames.currentName());
  // }

  /**
   * Test setting a single string-valued parameter. Creates a Transformer and
   * calls setParameter() then transform(Source, Source), then uses the worker
   * method checkFileContains() to validate and output results.
   * 
   * @param paramName
   *          simple name of parameter
   * @param paramVal
   *          String value of parameter
   * @param xmlSource
   *          object to use in transform
   * @param xslStylesheet
   *          object to use in transform
   * @param checkString
   *          to look for in output file (logged)
   * @param comment
   *          to log with check() call
   * @return true if pass, false otherwise
   */
  // protected boolean testSetParam(String paramName, String paramVal, Source
  // xmlSource, Source xslStylesheet, String checkString,
  // String comment) {
  // try {
  // TransformerFactory factory = TransformerFactory.newInstance();
  // factory.setErrorListener(new DefaultErrorHandler());
  // Transformer transformer = factory.newTransformer(xslStylesheet);
  // transformer.setErrorListener(new DefaultErrorHandler());
  //
  // reporter.logTraceMsg("setParameter(" + paramName + ", " + paramVal + ")");
  // transformer.setParameter(paramName, paramVal);
  // reporter.logTraceMsg("transform(" + xmlSource.getSystemId() + ", " +
  // xslStylesheet.getSystemId() + ", ...)");
  // transformer.transform(xmlSource, new StreamResult(outNames.nextName()));
  // } catch (Throwable t) {
  // reporter.logThrowable(Logger.ERRORMSG, t,
  // "testSetParam unexpectedly threw");
  // }
  // return checkFileContains(outNames.currentName(), checkString, "New:" +
  // comment + " into: " + outNames.currentName());
  // }

}
