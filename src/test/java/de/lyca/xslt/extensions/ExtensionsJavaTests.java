package de.lyca.xslt.extensions;

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
public class ExtensionsJavaTests {

  private static final String PACKAGE = '/' + ExtensionsJavaTests.class.getPackage().getName().replace('.', '/')
      + "/java/";

  @Parameters(name = "{0}")
  public static Collection<Object> params() {
    // Extension elements are not supported (XSLTC)
    Arrays.asList("javaElem01", "javaRedir1", "javaRedir2", "javaSample4");
    return Arrays.asList("javaBugzilla3722", "javaSample3");
  }

  private String name;

  public ExtensionsJavaTests(String name) {
    this.name = PACKAGE + name;
  }

  @Test
  public void extensionsJavaTest() throws Exception {
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    Assert.assertEquals(expected, t.getResultString());
  }

}
