package de.lyca.xslt.conferr;

import static de.lyca.xslt.ResourceUtils.getSource;

import javax.xml.transform.Source;

import org.custommonkey.xmlunit.Transform;
import org.junit.Ignore;
import org.junit.Test;

public class ConfLreErrTests {

  private static final String PACKAGE = '/' + ConfLreErrTests.class.getPackage().getName().replace('.', '/')
          + "/lreerr/";

  @Test
  public void lreerr01() throws Exception {
    final String name = PACKAGE + "lreerr01";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  @Ignore("NullPointerException")
  public void lreerr02() throws Exception {
    final String name = PACKAGE + "lreerr02";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  public void lreerr03() throws Exception {
    final String name = PACKAGE + "lreerr03";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  @Ignore("Leerer Attributname")
  public void lreerr04() throws Exception {
    final String name = PACKAGE + "lreerr04";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  public void lreerr05() throws Exception {
    final String name = PACKAGE + "lreerr05";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  public void lreerr06() throws Exception {
    final String name = PACKAGE + "lreerr06";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  public void lreerr07() throws Exception {
    final String name = PACKAGE + "lreerr07";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  public void lreerr08() throws Exception {
    final String name = PACKAGE + "lreerr08";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

}
