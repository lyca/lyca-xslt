package de.lyca.xslt.conferr;

import static de.lyca.xslt.ResourceUtils.getSource;

import javax.xml.transform.Source;

import org.custommonkey.xmlunit.Transform;
import org.junit.Ignore;
import org.junit.Test;

public class ConfMdocsErrTests {

  private static final String PACKAGE = '/' + ConfMdocsErrTests.class.getPackage().getName().replace('.', '/')
          + "/mdocserr/";

  @Test
  public void mdocserr01() throws Exception {
    final String name = PACKAGE + "mdocserr01";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  public void mdocserr02() throws Exception {
    final String name = PACKAGE + "mdocserr02";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  @Ignore
  public void mdocserr03() throws Exception {
    final String name = PACKAGE + "mdocserr03";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  public void mdocserr04() throws Exception {
    final String name = PACKAGE + "mdocserr04";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

}
