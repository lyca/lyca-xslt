package de.lyca.xslt.conf;

import static de.lyca.xslt.ResourceUtils.getSource;
import static de.lyca.xslt.ResourceUtils.readResource;
import static java.nio.charset.StandardCharsets.UTF_8;

import javax.xml.transform.Source;

import org.custommonkey.xmlunit.Transform;
import org.junit.Assert;
import org.junit.Test;

public class ConfProcessorinfoTests {

  private static final String PACKAGE = '/' + ConfProcessorinfoTests.class.getPackage().getName().replace('.', '/')
      + "/processorinfo/";

  @Test
  public void processorinfo03() throws Exception {
    final String name = PACKAGE + "processorinfo03";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    Assert.assertEquals(expected, t.getResultString());
  }

}
