package de.lyca.xslt.conf;

import static de.lyca.xslt.ResourceUtils.getSource;
import static de.lyca.xslt.ResourceUtils.readResource;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.transform.Source;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.lyca.xslt.Transform;

@RunWith(Parameterized.class)
public class ConfImpinclTests {

  private static final String PACKAGE = '/' + ConfImpinclTests.class.getPackage().getName().replace('.', '/')
      + "/impincl/";

  @Parameters(name = "{0}")
  public static Collection<Object> params() {
    Collection<Object> result = new ArrayList<>();
    for (int i = 1; i < 30; i++) {
      result.add(String.format("impincl%02d", i));
    }
    return result;
  }

  private String name;

  public ConfImpinclTests(String name) {
    this.name = PACKAGE + name;
  }

  @Test
  public void confImpinclTest() throws Exception {
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    Assert.assertEquals(expected, t.getResultString());
  }

}
