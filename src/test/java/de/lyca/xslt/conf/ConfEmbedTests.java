package de.lyca.xslt.conf;

import static de.lyca.xslt.ResourceUtils.getSource;
import static de.lyca.xslt.ResourceUtils.readResource;
import static java.nio.charset.StandardCharsets.UTF_8;

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
public class ConfEmbedTests {

  private static final String PACKAGE = '/' + ConfEmbedTests.class.getPackage().getName().replace('.', '/') + "/embed/";

  @Parameters(name = "{0}")
  public static Collection<Object[]> params() {
    Collection<Object[]> result = new ArrayList<>();
    for (int i = 1; i < 5; i++) {
      result.add(new Object[] { String.format("embed%02d", i), i == 3 || i == 4 ? ".xsl" : ".xml" });
    }
    // 8 ignored as it has to be called differently...
    for (int i = 7; i < 8; i++) {
      result.add(new Object[] { String.format("embed%02d", i), ".xml" });
    }
    return result;
  }

  private String name;
  private String filetype;

  public ConfEmbedTests(String name, String filetype) {
    this.name = PACKAGE + name;
    this.filetype = filetype;
  }

  @Test
  public void confEmbedTests() throws Exception {
    final Source xsl = getSource(name + filetype);
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    Assert.assertEquals(expected, t.getResultString());
  }

}
