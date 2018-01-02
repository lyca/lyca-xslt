package de.lyca.xslt.jira;

import static de.lyca.xslt.ResourceUtils.getSource;
import static de.lyca.xslt.ResourceUtils.readResource;
import static java.nio.charset.StandardCharsets.UTF_8;

import javax.xml.transform.Source;

import org.junit.Assert;
import org.junit.Test;

import de.lyca.xslt.Transform;

public class JiraTests {

  private static final String PACKAGE = '/' + JiraTests.class.getPackage().getName().replace('.', '/');

  @Test
  public void jira2419() throws Exception {
    final String name = PACKAGE + "/jira2419";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    Assert.assertEquals(expected, t.getResultString());
  }

}
