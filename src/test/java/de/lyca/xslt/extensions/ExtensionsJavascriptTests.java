package de.lyca.xslt.extensions;

import static de.lyca.xslt.ResourceUtils.getSource;
import static de.lyca.xslt.ResourceUtils.readResource;
import static java.nio.charset.StandardCharsets.UTF_8;

import javax.xml.transform.Source;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import de.lyca.xslt.Transform;

public class ExtensionsJavascriptTests {

  private static final String PACKAGE = '/' + ExtensionsJavascriptTests.class.getPackage().getName().replace('.', '/')
          + "/javascript/";

  @Test
  @Ignore("JavaScript is not supported by XSLTC")
  public void javascriptSample2() throws Exception {
    final String name = PACKAGE + "javascriptSample2";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    Assert.assertEquals(expected, t.getResultString());
  }

  @Test
  @Ignore("JavaScript is not supported by XSLTC")
  public void javascriptSample5() throws Exception {
    final String name = PACKAGE + "javascriptSample5";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    Assert.assertEquals(expected, t.getResultString());
  }

}
