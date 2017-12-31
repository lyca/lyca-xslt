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
public class ConfAttribsetTests {

  private static final String PACKAGE = '/' + ConfAttribsetTests.class.getPackage().getName().replace('.', '/')
      + "/attribset/";

  @Parameters(name = "{0}")
  public static Collection<Object> params() {
    Collection<Object> result = new ArrayList<>();
    // 15, 23: xmlns cannot be used as attribute name
    // 24: overriding prefix not supported in the moment
    int[] exclude = { 15, 23, 24 };
    for (int i = 1; i < 50; i++) {
      if (Arrays.binarySearch(exclude, i) >= 0) {
        continue;
      }
      result.add(String.format("attribset%02d", i));
    }
    return result;
  }

  private String name;

  public ConfAttribsetTests(String name) {
    this.name = PACKAGE + name;
  }

  @Test
  public void confAttribsetTest() throws Exception {
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    Assert.assertEquals(expected, t.getResultString());
  }

}
