package de.lyca.xslt.conferr;

import static de.lyca.xslt.ResourceUtils.getSource;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;

import org.custommonkey.xmlunit.Transform;
import org.custommonkey.xmlunit.exceptions.ConfigurationException;
import org.junit.Test;

public class ConfBoolErrTests {

  private static final String PACKAGE = '/' + ConfBoolErrTests.class.getPackage().getName().replace('.', '/')
          + "/boolerr/";

  @Test
  public void booleanerr01() throws Exception {
    final String name = PACKAGE + "booleanerr01";
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
  public void booleanerr02() throws Exception {
    final String name = PACKAGE + "booleanerr02";
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
  public void booleanerr03() throws Exception {
    final String name = PACKAGE + "booleanerr03";
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
  public void booleanerr04() throws Exception {
    final String name = PACKAGE + "booleanerr04";
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
  public void booleanerr05() throws Exception {
    final String name = PACKAGE + "booleanerr05";
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
  public void booleanerr06() throws Exception {
    final String name = PACKAGE + "booleanerr06";
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
  public void booleanerr07() throws Exception {
    final String name = PACKAGE + "booleanerr07";
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
  public void booleanerr08() throws Exception {
    final String name = PACKAGE + "booleanerr08";
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
  public void booleanerr09() throws Exception {
    final String name = PACKAGE + "booleanerr09";
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
  public void booleanerr10() throws Exception {
    final String name = PACKAGE + "booleanerr10";
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
  public void booleanerr11() throws Exception {
    final String name = PACKAGE + "booleanerr11";
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
  public void booleanerr12() throws Exception {
    final String name = PACKAGE + "booleanerr12";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
    System.out.println(t.getResultString());
  }

  @Test
  public void booleanerr13() throws Exception {
    final String name = PACKAGE + "booleanerr13";
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
  public void booleanerr14() throws Exception {
    final String name = PACKAGE + "booleanerr14";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
    System.out.println(t.getResultString());
  }

}
