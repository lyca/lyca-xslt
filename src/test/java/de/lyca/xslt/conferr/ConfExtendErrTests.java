package de.lyca.xslt.conferr;

import static de.lyca.xslt.ResourceUtils.getSource;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;

import org.custommonkey.xmlunit.Transform;
import org.custommonkey.xmlunit.exceptions.ConfigurationException;
import org.junit.Test;

public class ConfExtendErrTests {

  private static final String PACKAGE = '/' + ConfExtendErrTests.class.getPackage().getName().replace('.', '/')
          + "/extenderr/";

  @Test
  public void extenderr01() throws Exception {
    final String name = PACKAGE + "extenderr01";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  public void extenderr02() throws Exception {
    final String name = PACKAGE + "extenderr02";
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
  public void extenderr03() throws Exception {
    final String name = PACKAGE + "extenderr03";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  public void extenderr04() throws Exception {
    final String name = PACKAGE + "extenderr04";
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
  public void extenderr05() throws Exception {
    final String name = PACKAGE + "extenderr05";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

}
