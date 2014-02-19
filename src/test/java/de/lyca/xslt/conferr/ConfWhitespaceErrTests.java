package de.lyca.xslt.conferr;

import static de.lyca.xslt.ResourceUtils.getSource;

import javax.xml.transform.Source;

import org.custommonkey.xmlunit.Transform;
import org.junit.Ignore;
import org.junit.Test;

public class ConfWhitespaceErrTests {

  private static final String PACKAGE = '/' + ConfWhitespaceErrTests.class.getPackage().getName().replace('.', '/')
          + "/whitespaceerr/";

  @Test
  @Ignore
  public void whitespaceerr01() throws Exception {
    final String name = PACKAGE + "whitespaceerr01";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  @Ignore
  public void whitespaceerr02() throws Exception {
    final String name = PACKAGE + "whitespaceerr02";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  public void whitespaceerr03() throws Exception {
    final String name = PACKAGE + "whitespaceerr03";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  public void whitespaceerr04() throws Exception {
    final String name = PACKAGE + "whitespaceerr04";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

}
