package de.lyca.xslt.perf;

import static de.lyca.xslt.ResourceUtils.getSource;
import static de.lyca.xslt.ResourceUtils.readResource;
import static java.nio.charset.StandardCharsets.UTF_8;

import javax.xml.transform.Source;

import org.custommonkey.xmlunit.Transform;
import org.junit.Assert;
import org.junit.Test;

public class PerfSortTests {

  private static final String PACKAGE = '/' + PerfSortTests.class.getPackage().getName().replace('.', '/') + "/sort/";

  @Test
  public void sortBig() throws Exception {
    final String name = PACKAGE + "sort-big";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    Assert.assertEquals(expected, t.getResultString());
  }

  @Test
  public void sortNumbers1() throws Exception {
    final String name = PACKAGE + "sort-numbers1";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(PACKAGE + "../xtestdata/num1krandom.xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    Assert.assertEquals(expected, t.getResultString());
  }

  @Test
  public void sortWords1() throws Exception {
    final String name = PACKAGE + "sort-words1";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(PACKAGE + "../xtestdata/words-repeat.xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    Assert.assertEquals(expected, t.getResultString());
  }

}
