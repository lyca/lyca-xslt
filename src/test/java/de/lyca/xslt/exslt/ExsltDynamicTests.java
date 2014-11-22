package de.lyca.xslt.exslt;

import static de.lyca.xslt.ResourceUtils.getSource;
import static de.lyca.xslt.ResourceUtils.readResource;
import static java.nio.charset.StandardCharsets.UTF_8;

import javax.xml.transform.Source;

import org.custommonkey.xmlunit.Transform;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class ExsltDynamicTests {

  private static final String PACKAGE = '/' + ExsltDynamicTests.class.getPackage().getName().replace('.', '/')
          + "/dynamic/";

  @Test
  @Ignore("Dynamic is not supported by XSLTC")
  public void dynamic1() throws Exception {
    final String name = PACKAGE + "dynamic1";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    Assert.assertEquals(expected, t.getResultString());
  }

}
