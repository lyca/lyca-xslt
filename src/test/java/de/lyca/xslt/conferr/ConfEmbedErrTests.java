package de.lyca.xslt.conferr;

import static de.lyca.xslt.ResourceUtils.getSource;

import javax.xml.transform.Source;

import org.custommonkey.xmlunit.Transform;
import org.junit.Test;

public class ConfEmbedErrTests {

  private static final String PACKAGE = '/' + ConfEmbedErrTests.class.getPackage().getName().replace('.', '/')
          + "/embederr/";

  @Test
  public void embederr01() throws Exception {
    final String name = PACKAGE + "embederr01";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  public void embederr02() throws Exception {
    final String name = PACKAGE + "embederr02";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

}
