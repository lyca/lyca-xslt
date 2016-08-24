package de.lyca.xslt.conf;

import static de.lyca.xslt.ResourceUtils.getSource;
import static de.lyca.xslt.ResourceUtils.readResource;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.transform.Source;

import org.custommonkey.xmlunit.Transform;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ConfSelectTests {

  private static final String PACKAGE = '/' + ConfSelectTests.class.getPackage().getName().replace('.', '/')
      + "/select/";

  @Parameters(name = "{0}")
  public static Collection<Object[]> params() {
    Collection<Object[]> result = new ArrayList<>();
    for (int i = 1; i < 46; i++) {
      result.add(new Object[] { String.format("select%02d", i), UTF_8 });
    }
    for (int i = 47; i < 87; i++) {
      result.add(new Object[] { String.format("select%02d", i), i == 73 ? ISO_8859_1 : UTF_8 });
    }
    return result;
  }

  private String name;
  private Charset charset;

  public ConfSelectTests(String name, Charset charset) {
    this.name = PACKAGE + name;
    this.charset = charset;
  }

  @Test
  public void confSelectTest() throws Exception {
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", charset);
    final Transform t = new Transform(xml, xsl);
    Assert.assertEquals(expected, t.getResultString());
  }

}
