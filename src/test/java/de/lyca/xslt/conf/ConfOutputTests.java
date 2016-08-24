package de.lyca.xslt.conf;

import static de.lyca.xslt.ResourceUtils.getSource;
import static de.lyca.xslt.ResourceUtils.readResource;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_16;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.xml.transform.Source;

import org.custommonkey.xmlunit.Transform;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ConfOutputTests {

  private static final Charset ISO_8859_6 = Charset.forName("ISO-8859-6");
  private static final Charset SHIFT_JIS = Charset.forName("SHIFT_JIS");
  private static final String PACKAGE = '/' + ConfOutputTests.class.getPackage().getName().replace('.', '/')
      + "/output/";

  private String name;
  private Charset encoding;

//  22  @Ignore("EBCDIC-CP-IT encoding problem, so who cares ;-)")
//  59  @Ignore("Some whitespace problems?")
//  63  @Ignore("Html tag vs. xml tag issues?")

  @Parameters(name = "{0}")
  public static Collection<Object[]> params() {
    Collection<Object[]> result = new ArrayList<>();
    int[] exclude = { 22, 31, 59, 63, 83, 108 };
    for (int i = 1; i < 115; i++) {
      if (Arrays.binarySearch(exclude, i) >= 0) {
        continue;
      }
      if (i == 26 || i == 86) {
        result.add(new Object[] { String.format("output%02d", i), ISO_8859_1 });
      } else if (i == 73) {
        result.add(new Object[] { String.format("output%02d", i), SHIFT_JIS });
      } else if (i == 78 || i == 79) {
        result.add(new Object[] { String.format("output%02d", i), ISO_8859_6 });
      } else if (i == 80) {
        result.add(new Object[] { String.format("output%02d", i), UTF_16 });
      } else {
        result.add(new Object[] { String.format("output%02d", i), UTF_8 });
      }
    }
    return result;
  }

  public ConfOutputTests(String name, Charset encoding) {
    this.name = PACKAGE + name;
    this.encoding = encoding;
  }

  @Test
  public void confOutputTest() throws Exception {
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", encoding);
    final Transform t = new Transform(xml, xsl);
    Assert.assertEquals(expected, t.getResultString());
  }

}
