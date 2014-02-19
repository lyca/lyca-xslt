package de.lyca.xslt.conferr;

import static de.lyca.xslt.ResourceUtils.getSource;

import javax.xml.transform.Source;

import org.custommonkey.xmlunit.Transform;
import org.junit.Ignore;
import org.junit.Test;

public class ConfPositionErrTests {

  private static final String PACKAGE = '/' + ConfPositionErrTests.class.getPackage().getName().replace('.', '/')
          + "/positionerr/";

  @Test
  @Ignore
  public void positionerr01() throws Exception {
    final String name = PACKAGE + "positionerr01";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  @Ignore
  public void positionerr02() throws Exception {
    final String name = PACKAGE + "positionerr02";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  @Ignore
  public void positionerr03() throws Exception {
    final String name = PACKAGE + "positionerr03";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  @Ignore
  public void positionerr04() throws Exception {
    final String name = PACKAGE + "positionerr04";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  @Ignore
  public void positionerr05() throws Exception {
    final String name = PACKAGE + "positionerr05";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

}
