package de.lyca.xslt.conferr;

import static de.lyca.xslt.ResourceUtils.getSource;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;

import org.custommonkey.xmlunit.Transform;
import org.custommonkey.xmlunit.exceptions.ConfigurationException;
import org.junit.Test;

public class ConfAttribValTemplateErrTests {

  private static final String PACKAGE = '/'
          + ConfAttribValTemplateErrTests.class.getPackage().getName().replace('.', '/') + "/attribvaltemplateerr/";

  @Test
  public void attribvaltemplateerr01() throws Exception {
    final String name = PACKAGE + "attribvaltemplateerr01";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    try {
      new Transform(xml, xsl);
    } catch (final ConfigurationException e) {
      final Throwable t = e.getCause();
      if (t instanceof TransformerConfigurationException) {
        System.out.println(t.getMessage());
      } else
        throw e;
    }
  }

  @Test
  public void attribvaltemplateerr02() throws Exception {
    final String name = PACKAGE + "attribvaltemplateerr02";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    try {
      new Transform(xml, xsl);
    } catch (final ConfigurationException e) {
      final Throwable t = e.getCause();
      if (t instanceof TransformerConfigurationException) {
        System.out.println(t.getMessage());
      } else
        throw e;
    }
  }

}
