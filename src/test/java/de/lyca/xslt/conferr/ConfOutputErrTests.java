package de.lyca.xslt.conferr;

import static de.lyca.xslt.ResourceUtils.getSource;

import javax.xml.transform.Source;

import org.custommonkey.xmlunit.Transform;
import org.junit.Ignore;
import org.junit.Test;

public class ConfOutputErrTests {

  private static final String PACKAGE = '/' + ConfOutputErrTests.class.getPackage().getName().replace('.', '/')
          + "/outputerr/";

  @Test
  @Ignore
  public void outputerr01() throws Exception {
    final String name = PACKAGE + "outputerr01";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  @Ignore
  public void outputerr02() throws Exception {
    final String name = PACKAGE + "outputerr02";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  @Ignore
  public void outputerr03() throws Exception {
    final String name = PACKAGE + "outputerr03";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  @Ignore
  public void outputerr04() throws Exception {
    final String name = PACKAGE + "outputerr04";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  public void outputerr05() throws Exception {
    final String name = PACKAGE + "outputerr05";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  public void outputerr06() throws Exception {
    final String name = PACKAGE + "outputerr06";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  public void outputerr07() throws Exception {
    final String name = PACKAGE + "outputerr07";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  public void outputerr08() throws Exception {
    final String name = PACKAGE + "outputerr08";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  public void outputerr09() throws Exception {
    final String name = PACKAGE + "outputerr09";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  @Ignore
  public void outputerr10() throws Exception {
    final String name = PACKAGE + "outputerr10";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  public void outputerr11() throws Exception {
    final String name = PACKAGE + "outputerr11";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  public void outputerr12() throws Exception {
    final String name = PACKAGE + "outputerr12";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  public void outputerr13() throws Exception {
    final String name = PACKAGE + "outputerr13";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  public void outputerr14() throws Exception {
    final String name = PACKAGE + "outputerr14";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

}
