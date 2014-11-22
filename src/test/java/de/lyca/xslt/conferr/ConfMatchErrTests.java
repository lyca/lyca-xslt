package de.lyca.xslt.conferr;

import static de.lyca.xslt.ResourceUtils.getSource;

import javax.xml.transform.Source;

import org.custommonkey.xmlunit.Transform;
import org.junit.Ignore;
import org.junit.Test;

public class ConfMatchErrTests {

  private static final String PACKAGE = '/' + ConfMatchErrTests.class.getPackage().getName().replace('.', '/')
          + "/matcherr/";

  @Test
  public void matcherr01() throws Exception {
    final String name = PACKAGE + "matcherr01";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  public void matcherr02() throws Exception {
    final String name = PACKAGE + "matcherr02";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  public void matcherr03() throws Exception {
    final String name = PACKAGE + "matcherr03";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  public void matcherr04() throws Exception {
    final String name = PACKAGE + "matcherr04";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  public void matcherr05() throws Exception {
    final String name = PACKAGE + "matcherr05";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  @Ignore("document() in match")
  public void matcherr06() throws Exception {
    final String name = PACKAGE + "matcherr06";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  public void matcherr07() throws Exception {
    final String name = PACKAGE + "matcherr07";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  public void matcherr08() throws Exception {
    final String name = PACKAGE + "matcherr08";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  public void matcherr09() throws Exception {
    final String name = PACKAGE + "matcherr09";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  @Ignore("revisit")
  public void matcherr10() throws Exception {
    final String name = PACKAGE + "matcherr10";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  @Ignore("revisit")
  public void matcherr11() throws Exception {
    final String name = PACKAGE + "matcherr11";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  @Ignore("revisit")
  public void matcherr12() throws Exception {
    final String name = PACKAGE + "matcherr12";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  @Ignore("revisit")
  public void matcherr13() throws Exception {
    final String name = PACKAGE + "matcherr13";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

}
