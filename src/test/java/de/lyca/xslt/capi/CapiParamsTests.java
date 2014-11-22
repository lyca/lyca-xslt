package de.lyca.xslt.capi;

import static de.lyca.xslt.ResourceUtils.getSource;
import static de.lyca.xslt.ResourceUtils.readResource;
import static java.nio.charset.StandardCharsets.UTF_8;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.custommonkey.xmlunit.Transform;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class CapiParamsTests {

  private static final String PACKAGE = '/' + CapiParamsTests.class.getPackage().getName().replace('.', '/')
          + "/params/";

  @Test
  public void params01() throws Exception {
    final String name = PACKAGE + "params01";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    t.setParameter("input", "testing 1 2 3");
    Assert.assertEquals(expected, t.getResultString());
  }

  @Test
  public void params02() throws Exception {
    final String name = PACKAGE + "params02";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    t.setParameter("in1", "A");
    t.setParameter("in2", "B");
    t.setParameter("in3", "C");
    t.setParameter("in4", "D");
    t.setParameter("in5", "E");
    Assert.assertEquals(expected, t.getResultString());
  }

  @Test
  public void params03() throws Exception {
    final String name = PACKAGE + "params03";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    t.setParameter("{http://www.lotus.com}in1", "DATA");
    Assert.assertEquals(expected, t.getResultString());
  }

  @Test
  public void params04() throws Exception {
    final String name = PACKAGE + "params04";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    t.setParameter("input", "testing");
    Assert.assertEquals(expected, t.getResultString());
  }

  @Test
  public void params05() throws Exception {
    final String name = PACKAGE + "params05";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    t.setParameter("input", "testing 1 2 3");
    Assert.assertEquals(expected, t.getResultString());
  }

  @Test
  public void params06() throws Exception {
    final String name = PACKAGE + "params06";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    t.setParameter("input", "testing 1 2 3");
    Assert.assertEquals(expected, t.getResultString());
  }

  @Test
  @Ignore("Embedded stylesheet not working")
  public void params07() throws Exception {
    final String name = PACKAGE + "params07";
    final Source xslEmbeddedInXml = getSource(name + ".xsl");
    final Source xml = new DOMSource();
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xslEmbeddedInXml);
    Assert.assertEquals(expected, t.getResultString());
  }

  @Test
  @Ignore("Embedding stylesheet not working")
  public void params08() throws Exception {
    final String name = PACKAGE + "params08";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = new DOMSource();
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    Assert.assertEquals(expected, t.getResultString());
  }

}
