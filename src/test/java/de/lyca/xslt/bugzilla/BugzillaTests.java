package de.lyca.xslt.bugzilla;

import static de.lyca.xslt.ResourceUtils.getSource;
import static de.lyca.xslt.ResourceUtils.readResource;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.fail;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.dom.DOMSource;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import de.lyca.xslt.Transform;

public class BugzillaTests {

  private static final String PACKAGE = '/' + BugzillaTests.class.getPackage().getName().replace('.', '/');

  @Test
  @Ignore("Extension not supported in XSLTC")
  public void bugzilla2925() throws Exception {
    final String name = PACKAGE + "/Bugzilla2925";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    Assert.assertEquals(expected, t.getResultString());
  }

  @Test
  public void bugzilla2945() throws Exception {
    final String name = PACKAGE + "/Bugzilla2945";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    Assert.assertEquals(expected, t.getResultString());
  }

  @Test
  public void bugzilla3001() throws Exception {
    final String name = PACKAGE + "/Bugzilla3001";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    Assert.assertEquals(expected, t.getResultString());
  }

  @Test
  public void bugzilla3031() throws Exception {
    final String name = PACKAGE + "/Bugzilla3031";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    Assert.assertEquals(expected, t.getResultString());
  }

  @Test
  public void bugzilla3060() throws Exception {
    final String name = PACKAGE + "/Bugzilla3060";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    Assert.assertEquals(expected, t.getResultString());
  }

  @Test
  @Ignore("Evaluate not supported by XSLTC")
  public void bugzilla3265() throws Exception {
    final String name = PACKAGE + "/Bugzilla3265";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    Assert.assertEquals(expected, t.getResultString());
  }

  @Test
  public void bugzilla3489() throws Exception {
    final String name = PACKAGE + "/Bugzilla3489";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    try {
      fail(new Transform(xml, xsl).getResultString());
    } catch (final TransformerConfigurationException e) {
    }
  }

  @Test
  public void bugzilla3514() throws Exception {
    final String name = PACKAGE + "/Bugzilla3514";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    Assert.assertEquals(expected, t.getResultString());
  }

  @Test
  @Ignore("xml:space='preserve' is not working")
  public void bugzilla4056() throws Exception {
    final String name = PACKAGE + "/Bugzilla4056";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    Assert.assertEquals(expected, t.getResultString());
  }

  @Test
  public void bugzilla4218() throws Exception {
    final String name = PACKAGE + "/Bugzilla4218";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = new DOMSource();
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    Assert.assertEquals(expected, t.getResultString());
  }

  @Test
  public void bugzilla5609() throws Exception {
    final String name = PACKAGE + "/Bugzilla5609";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    Assert.assertEquals(expected, t.getResultString());
  }

  @Test
  @Ignore("Extension function problem")
  public void bugzilla6181() throws Exception {
    final String name = PACKAGE + "/Bugzilla6181";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    Assert.assertEquals(expected, t.getResultString());
  }

  @Test
  public void bugzilla6284() throws Exception {
    final String name = PACKAGE + "/Bugzilla6284";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    Assert.assertEquals(expected, t.getResultString());
  }

  @Test
  public void bugzilla6312() throws Exception {
    final String name = PACKAGE + "/Bugzilla6312";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    Assert.assertEquals(expected, t.getResultString());
  }

  @Test
  public void bugzilla6328() throws Exception {
    final String name = PACKAGE + "/Bugzilla6328";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    Assert.assertEquals(expected, t.getResultString());
  }

  @Test
  @Ignore("boolean '=' compares text content only, not attributes")
  public void bugzilla6337() throws Exception {
    final String name = PACKAGE + "/Bugzilla6337";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = "";// readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    Assert.assertEquals(expected, t.getResultString());
  }

  @Test
  @Ignore("XSLTC limitations")
  public void bugzillaNodeInfo() throws Exception {
    final String name = PACKAGE + "/BugzillaNodeInfo";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    Assert.assertEquals(expected, t.getResultString());
  }

}
