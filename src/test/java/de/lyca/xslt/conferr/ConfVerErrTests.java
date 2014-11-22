package de.lyca.xslt.conferr;

import static de.lyca.xslt.ResourceUtils.getSource;

import javax.xml.transform.Source;

import org.custommonkey.xmlunit.Transform;
import org.junit.Ignore;
import org.junit.Test;

public class ConfVerErrTests {

  private static final String PACKAGE = '/' + ConfVerErrTests.class.getPackage().getName().replace('.', '/')
          + "/vererr/";

  @Test
  @Ignore
  public void vererr01() throws Exception {
    final String name = PACKAGE + "vererr01";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  public void vererr02() throws Exception {
    final String name = PACKAGE + "vererr02";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  @Ignore
  public void vererr03() throws Exception {
    final String name = PACKAGE + "vererr03";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  public void vererr04() throws Exception {
    final String name = PACKAGE + "vererr04";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  @Ignore
  public void vererr05() throws Exception {
    final String name = PACKAGE + "vererr05";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  @Ignore
  public void vererr06() throws Exception {
    final String name = PACKAGE + "vererr06";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  @Ignore
  public void vererr07() throws Exception {
    final String name = PACKAGE + "vererr07";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  public void vererr08() throws Exception {
    final String name = PACKAGE + "vererr08";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  public void vererr09() throws Exception {
    final String name = PACKAGE + "vererr09";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

}
