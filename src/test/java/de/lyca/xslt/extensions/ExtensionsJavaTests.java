package de.lyca.xslt.extensions;

import static de.lyca.xslt.ResourceUtils.getSource;
import static de.lyca.xslt.ResourceUtils.readResource;
import static java.nio.charset.StandardCharsets.UTF_8;

import javax.xml.transform.Source;

import org.custommonkey.xmlunit.Transform;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class ExtensionsJavaTests {

  private static final String PACKAGE = '/' + ExtensionsJavaTests.class.getPackage().getName().replace('.', '/')
          + "/java/";

  @Test
  public void javaBugzilla3722() throws Exception {
    final String name = PACKAGE + "javaBugzilla3722";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    Assert.assertEquals(expected, t.getResultString());
  }

  @Test
  @Ignore("Extension elements are not supported (XSLTC)")
  public void javaElem01() throws Exception {
    final String name = PACKAGE + "javaElem01";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    Assert.assertEquals(expected, t.getResultString());
  }

  @Test
  @Ignore("Extension elements are not supported (XSLTC)")
  public void javaRedir1() throws Exception {
    final String name = PACKAGE + "javaRedir1";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    Assert.assertEquals(expected, t.getResultString());
  }

  @Test
  @Ignore("Extension elements are not supported (XSLTC)")
  public void javaRedir2() throws Exception {
    final String name = PACKAGE + "javaRedir2";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    Assert.assertEquals(expected, t.getResultString());
  }

  @Test
  public void javaSample3() throws Exception {
    final String name = PACKAGE + "javaSample3";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    Assert.assertEquals(expected, t.getResultString());
  }

  @Test
  @Ignore("Extension elements are not supported (XSLTC)")
  public void javaSample4() throws Exception {
    final String name = PACKAGE + "javaSample4";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    Assert.assertEquals(expected, t.getResultString());
  }

}
