package de.lyca.xslt.conferr;

import static de.lyca.xslt.ResourceUtils.getSource;

import javax.xml.transform.Source;

import org.custommonkey.xmlunit.Transform;
import org.junit.Ignore;
import org.junit.Test;

public class ConfImpinclErrTests {

  private static final String PACKAGE = '/' + ConfImpinclErrTests.class.getPackage().getName().replace('.', '/')
          + "/impinclerr/";

  @Test
  @Ignore("StackOverflowError")
  public void impinclerr01() throws Exception {
    final String name = PACKAGE + "impinclerr01";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  public void impinclerr02() throws Exception {
    final String name = PACKAGE + "impinclerr02";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  public void impinclerr03() throws Exception {
    final String name = PACKAGE + "impinclerr03";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  @Ignore("StackOverflowError")
  public void impinclerr04() throws Exception {
    final String name = PACKAGE + "impinclerr04";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  @Ignore("StackOverflowError")
  public void impinclerr05() throws Exception {
    final String name = PACKAGE + "impinclerr05";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  @Ignore("StackOverflowError")
  public void impinclerr06() throws Exception {
    final String name = PACKAGE + "impinclerr06";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  @Ignore("NullPointerException")
  public void impinclerr07() throws Exception {
    final String name = PACKAGE + "impinclerr07";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  public void impinclerr08() throws Exception {
    final String name = PACKAGE + "impinclerr08";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  public void impinclerr09() throws Exception {
    final String name = PACKAGE + "impinclerr09";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  @Ignore("Include file not found")
  public void impinclerr10() throws Exception {
    final String name = PACKAGE + "impinclerr10";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

}
