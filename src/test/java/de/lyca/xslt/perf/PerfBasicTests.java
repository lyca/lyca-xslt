package de.lyca.xslt.perf;

import static de.lyca.xslt.ResourceUtils.getSource;
import static de.lyca.xslt.ResourceUtils.readResource;
import static java.nio.charset.StandardCharsets.UTF_8;

import javax.xml.transform.Source;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import de.lyca.xslt.Transform;

public class PerfBasicTests {

  private static final String PACKAGE = '/' + PerfBasicTests.class.getPackage().getName().replace('.', '/') + "/basic/";

  @Test
  @Ignore("Linebreak Differences (Windows vs. Unix")
  public void basicAllWell() throws Exception {
    final String name = PACKAGE + "basic-all_well";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    Assert.assertEquals(expected, t.getResultString());
  }

  @Test
  @Ignore("Linebreak Differences (Windows vs. Unix")
  public void basicDateTranscode() throws Exception {
    final String name = PACKAGE + "basic-datetranscode";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    Assert.assertEquals(expected, t.getResultString());
  }

}
