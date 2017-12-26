package de.lyca.xslt.conferr;

import static de.lyca.xslt.ResourceUtils.getSource;
import static org.junit.Assert.fail;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;

import org.junit.Test;

import de.lyca.xslt.Transform;

public class ConfExpressionErrTests {

  private static final String PACKAGE = '/' + ConfExpressionErrTests.class.getPackage().getName().replace('.', '/')
      + "/expressionerr/";

  @Test
  public void expressionerr01() throws Exception {
    final String name = PACKAGE + "expressionerr01";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    try {
      fail(new Transform(xml, xsl).getResultString());
    } catch (final TransformerConfigurationException e) {
    }
  }

  @Test
  public void expressionerr02() throws Exception {
    final String name = PACKAGE + "expressionerr02";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    try {
      fail(new Transform(xml, xsl).getResultString());
    } catch (final TransformerConfigurationException e) {
    }
  }

}
