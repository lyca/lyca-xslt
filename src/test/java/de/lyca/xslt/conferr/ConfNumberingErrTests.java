package de.lyca.xslt.conferr;

import static de.lyca.xslt.ResourceUtils.getSource;

import javax.xml.transform.Source;

import org.custommonkey.xmlunit.Transform;
import org.junit.Ignore;
import org.junit.Test;

public class ConfNumberingErrTests {

  private static final String PACKAGE = '/' + ConfNumberingErrTests.class.getPackage().getName().replace('.', '/')
          + "/numberingerr/";

  @Test
  public void numberingerr01() throws Exception {
    final String name = PACKAGE + "numberingerr01";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  public void numberingerr02() throws Exception {
    final String name = PACKAGE + "numberingerr02";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  public void numberingerr03() throws Exception {
    final String name = PACKAGE + "numberingerr03";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  @Ignore
  public void numberingerr04() throws Exception {
    final String name = PACKAGE + "numberingerr04";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  @Ignore
  public void numberingerr05() throws Exception {
    final String name = PACKAGE + "numberingerr05";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  public void numberingerr06() throws Exception {
    final String name = PACKAGE + "numberingerr06";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  public void numberingerr07() throws Exception {
    final String name = PACKAGE + "numberingerr07";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

}
