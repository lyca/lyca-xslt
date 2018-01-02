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
public class ExsltStringsTests {

  private static final String PACKAGE = '/' + ExsltStringsTests.class.getPackage().getName().replace('.', '/')
      + "/strings/";

  @Parameters(name = "{0}")
  public static Collection<Object> params() {
    return Arrays.asList("strings1", "strings2", "strings3", "strings4", "strings5", "strings6", "strings7", "strings8",
        "strings9", "strings10", "strings11");
  }

  private String name;

  public ExsltStringsTests(String name) {
    this.name = PACKAGE + name;
  }

  @Test
  public void exsltStringsTest() throws Exception {
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    Assert.assertEquals(expected, t.getResultString());
  }

}
