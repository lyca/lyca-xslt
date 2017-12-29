package de.lyca.xslt.exslt;

import static de.lyca.xslt.ResourceUtils.getSource;
import static de.lyca.xslt.ResourceUtils.readResource;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.Arrays;
import java.util.Collection;

import javax.xml.transform.Source;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.lyca.xslt.Transform;

@RunWith(Parameterized.class)
public class ExsltCommonTests {

  private static final String PACKAGE = '/' + ExsltCommonTests.class.getPackage().getName().replace('.', '/')
      + "/common/";

  @Parameters(name = "{0}")
  public static Collection<Object> params() {
    return Arrays.asList("common1", "common2", "common3");
  }

  private String name;

  public ExsltCommonTests(String name) {
    this.name = PACKAGE + name;
  }

  @Test
  public void exsltCommonTest() throws Exception {
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    Assert.assertEquals(expected, t.getResultString());
  }

}
