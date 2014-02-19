package de.lyca.xslt.conferr;

import static de.lyca.xslt.ResourceUtils.getSource;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;

import org.custommonkey.xmlunit.Transform;
import org.custommonkey.xmlunit.exceptions.ConfigurationException;
import org.junit.Ignore;
import org.junit.Test;

public class ConfAttribsetErrTests {

  private static final String PACKAGE = '/' + ConfAttribsetErrTests.class.getPackage().getName().replace('.', '/')
          + "/attribseterr/";

  @Test
  @Ignore("Check spec")
  public void attribseterr01() throws Exception {
    final String name = PACKAGE + "attribseterr01";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
    System.out.println(t.getResultString());
  }

  @Test
  @Ignore("Throws StackOverflow but should throw TransformerConfigurationError")
  public void attribseterr02() throws Exception {
    final String name = PACKAGE + "attribseterr02";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
    System.out.println(t.getResultString());
  }

  @Test
  @Ignore("Circular reference should be detected")
  public void attribseterr03() throws Exception {
    final String name = PACKAGE + "attribseterr03";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
    System.out.println(t.getResultString());
  }

  @Test
  public void attribseterr04() throws Exception {
    final String name = PACKAGE + "attribseterr04";
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
  public void attribseterr05() throws Exception {
    final String name = PACKAGE + "attribseterr05";
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
  @Ignore("Check spec: AttributeSet inside template")
  public void attribseterr06() throws Exception {
    final String name = PACKAGE + "attribseterr06";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
    System.out.println(t.getResultString());
  }

  @Test
  public void attribseterr07() throws Exception {
    final String name = PACKAGE + "attribseterr07";
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
  public void attribseterr08() throws Exception {
    final String name = PACKAGE + "attribseterr08";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
    System.out.println(t.getResultString());
  }

  @Test
  public void attribseterr09() throws Exception {
    final String name = PACKAGE + "attribseterr09";
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
