package de.lyca.xslt.accept;

import static de.lyca.xslt.ResourceUtils.getSource;
import static de.lyca.xslt.ResourceUtils.readResource;
import static java.nio.charset.StandardCharsets.UTF_8;

import javax.xml.transform.Source;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import de.lyca.xslt.Transform;

public class AcceptEntrefTests {

  private static final String PACKAGE = '/' + AcceptEntrefTests.class.getPackage().getName().replace('.', '/')
          + "/entref/";

  @Test
  @Ignore
  public void entref01() throws Exception {
    final String name = PACKAGE + "entref01";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    Assert.assertEquals(expected, t.getResultString());
  }

  @Test
  public void entref02() throws Exception {
    final String name = PACKAGE + "entref02";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    Assert.assertEquals(expected, t.getResultString());
  }

  @Test
  public void entref03() throws Exception {
    final String name = PACKAGE + "entref03";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    Assert.assertEquals(expected, t.getResultString());
  }

}
