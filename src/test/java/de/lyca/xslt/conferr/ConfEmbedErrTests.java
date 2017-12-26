package de.lyca.xslt.conferr;

import static de.lyca.xslt.ResourceUtils.getSource;
import static org.junit.Assert.fail;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;

import org.junit.Test;

import de.lyca.xslt.Transform;

public class ConfEmbedErrTests {

  private static final String PACKAGE = '/' + ConfEmbedErrTests.class.getPackage().getName().replace('.', '/')
      + "/embederr/";

  @Test
  public void embederr01() throws Exception {
    final String name = PACKAGE + "embederr01";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    try {
      fail(new Transform(xml, xsl).getResultString());
    } catch (final TransformerConfigurationException e) {
    }
  }

  @Test
  public void embederr02() throws Exception {
    final String name = PACKAGE + "embederr02";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    try {
      fail(new Transform(xml, xsl).getResultString());
    } catch (final TransformerConfigurationException e) {
    }
  }

}
