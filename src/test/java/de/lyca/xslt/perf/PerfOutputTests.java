package de.lyca.xslt.perf;

import static de.lyca.xslt.ResourceUtils.getSource;
import static de.lyca.xslt.ResourceUtils.readResource;
import static java.nio.charset.StandardCharsets.UTF_8;

import javax.xml.transform.Source;

import org.custommonkey.xmlunit.Transform;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class PerfOutputTests {

  private static final String PACKAGE = '/' + PerfOutputTests.class.getPackage().getName().replace('.', '/')
          + "/output/";

  @Test
  @Ignore("'&amp;' is not decoded to '&' in href. Check spec for expectation.")
  public void outputHhref() throws Exception {
    final String name = PACKAGE + "outputHhref";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    Assert.assertEquals(expected, t.getResultString());
  }

}
