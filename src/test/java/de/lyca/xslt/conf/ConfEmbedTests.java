package de.lyca.xslt.conf;

import static de.lyca.xslt.ResourceUtils.getSource;
import static de.lyca.xslt.ResourceUtils.readResource;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.ArrayList;
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
public class ConfEmbedTests {

  private static final String PACKAGE = '/' + ConfEmbedTests.class.getPackage().getName().replace('.', '/') + "/embed/";

  @Parameters(name = "{0}")
  public static Collection<Object[]> params() {
    Collection<Object[]> result = new ArrayList<>();
    // 5, 6: non existent
    // 8 ignored as it has to be called differently...
    int[] exclude = { 5, 6, 8 };
    for (int i = 1; i < 9; i++) {
      if (Arrays.binarySearch(exclude, i) >= 0) {
        continue;
      }
      result.add(new Object[] { String.format("embed%02d", i), i == 3 || i == 4 ? ".xsl" : ".xml" });
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
