package de.lyca.xslt.api;

import static de.lyca.xslt.ResourceUtils.getSource;
import static de.lyca.xslt.ResourceUtils.readResource;
import static java.nio.charset.StandardCharsets.UTF_8;

import javax.xml.transform.Source;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Transform;
import org.junit.Assert;
import org.junit.Test;

public class ApiTests {

  private static final String PACKAGE = '/' + ApiTests.class.getPackage().getName().replace('.', '/') + '/';

  @Test
  public void minitest() throws Exception {
    final String name = PACKAGE + "Minitest";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    Assert.assertEquals(expected, t.getResultString());
  }

  @Test
  public void minitestParam() throws Exception {
    final String name = PACKAGE + "MinitestParam";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    t.setParameter("param1s", 1234);
    t.setParameter("param1n", "new-param1n-value");
    Assert.assertEquals(expected, t.getResultString());
  }

  @Test
  public void minitestPerf() throws Exception {
    final String name = PACKAGE + "MinitestPerf";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    final Diff diff = new Diff(expected, t.getResultString());
    Assert.assertTrue("XSL transformation failure.", diff.identical());
  }

  @Test
  public void threadOutput() throws Exception {
    final String name = PACKAGE + "ThreadOutput";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    Assert.assertEquals(expected, t.getResultString());
  }

}
