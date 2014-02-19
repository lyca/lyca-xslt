package de.lyca.xslt.conferr;

import static de.lyca.xslt.ResourceUtils.getSource;

import javax.xml.transform.Source;

import org.custommonkey.xmlunit.Transform;
import org.junit.Ignore;
import org.junit.Test;

public class ConfNamespaceErrTests {

  private static final String PACKAGE = '/' + ConfNamespaceErrTests.class.getPackage().getName().replace('.', '/')
          + "/namespaceerr/";

  @Test
  public void namespaceerr01() throws Exception {
    final String name = PACKAGE + "namespaceerr01";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  public void namespaceerr02() throws Exception {
    final String name = PACKAGE + "namespaceerr02";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  public void namespaceerr03() throws Exception {
    final String name = PACKAGE + "namespaceerr03";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  public void namespaceerr04() throws Exception {
    final String name = PACKAGE + "namespaceerr04";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  public void namespaceerr05() throws Exception {
    final String name = PACKAGE + "namespaceerr05";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  @Ignore
  public void namespaceerr06() throws Exception {
    final String name = PACKAGE + "namespaceerr06";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  @Ignore
  public void namespaceerr07() throws Exception {
    final String name = PACKAGE + "namespaceerr07";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  @Ignore
  public void namespaceerr08() throws Exception {
    final String name = PACKAGE + "namespaceerr08";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  @Ignore
  public void namespaceerr09() throws Exception {
    final String name = PACKAGE + "namespaceerr09";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  @Ignore
  public void namespaceerr10() throws Exception {
    final String name = PACKAGE + "namespaceerr10";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

}
