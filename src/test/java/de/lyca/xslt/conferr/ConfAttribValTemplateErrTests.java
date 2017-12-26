package de.lyca.xslt.conferr;

import static de.lyca.xslt.ResourceUtils.getSource;
import static org.junit.Assert.fail;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;

import org.junit.Test;

import de.lyca.xslt.Transform;

public class ConfAttribValTemplateErrTests {

  private static final String PACKAGE = '/'
      + ConfAttribValTemplateErrTests.class.getPackage().getName().replace('.', '/') + "/attribvaltemplateerr/";

  @Test
  public void attribvaltemplateerr01() throws Exception {
    final String name = PACKAGE + "attribvaltemplateerr01";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    try {
      fail(new Transform(xml, xsl).getResultString());
    } catch (final TransformerConfigurationException e) {
    }
  }

  @Test
  public void attribvaltemplateerr02() throws Exception {
    final String name = PACKAGE + "attribvaltemplateerr02";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    try {
      fail(new Transform(xml, xsl).getResultString());
    } catch (final TransformerConfigurationException e) {
    }
  }

}
