package de.lyca.xslt.capi;

import static de.lyca.xslt.ResourceUtils.getSource;
import static de.lyca.xslt.ResourceUtils.readResource;
import static java.nio.charset.StandardCharsets.UTF_8;

import javax.xml.transform.Source;

import org.junit.Assert;
import org.junit.Test;

import de.lyca.xslt.Transform;

public class CapiSmokeTests {

  private static final String PACKAGE = '/' + CapiSmokeTests.class.getPackage().getName().replace('.', '/') + "/smoke/";

  @Test
  public void capi01() throws Exception {
    final String name = PACKAGE + "capi01";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    Assert.assertEquals(expected, t.getResultString());
  }

  @Test
  public void smoke01() throws Exception {
    final String name = PACKAGE + "smoke01";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    Assert.assertEquals(expected, t.getResultString());
  }

}
