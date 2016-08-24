package de.lyca.xslt.conferr;

import static de.lyca.xslt.ResourceUtils.getSource;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.custommonkey.xmlunit.Transform;
import org.custommonkey.xmlunit.exceptions.ConfigurationException;
import org.junit.Ignore;
import org.junit.Test;

import de.lyca.xalan.xsltc.TransletException;

public class ConfMdocsErrTests {

  private static final String PACKAGE = '/' + ConfMdocsErrTests.class.getPackage().getName().replace('.', '/')
      + "/mdocserr/";

  @Test(expected = TransformerException.class)
  public void mdocserr01() throws Exception {
    final String name = PACKAGE + "mdocserr01";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    new Transform(xml, xsl).getResultString();
  }

  @Test(expected = TransformerConfigurationException.class)
  public void mdocserr02() throws Exception {
    final String name = PACKAGE + "mdocserr02";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    try {
      new Transform(xml, xsl).getResultString();
    } catch (ConfigurationException e) {
      throw (Exception) e.getCause();
    }
  }

  @Test(expected = TransformerConfigurationException.class)
  public void mdocserr03() throws Exception {
    final String name = PACKAGE + "mdocserr03";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    try {
      new Transform(xml, xsl).getResultString();
    } catch (ConfigurationException e) {
      throw (Exception) e.getCause();
    }
  }

  @Test
  public void mdocserr04() throws Exception {
    final String name = PACKAGE + "mdocserr04";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    System.out.println(new Transform(xml, xsl).getResultString());
  }

}
