package de.lyca.xslt.conf;

import static de.lyca.xslt.ResourceUtils.getSource;
import static de.lyca.xslt.ResourceUtils.readResource;
import static java.nio.charset.StandardCharsets.UTF_8;

import javax.xml.transform.Source;

import org.custommonkey.xmlunit.Transform;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class ConfSortLangTests {

  private static final String PACKAGE = '/' + ConfSortLangTests.class.getPackage().getName().replace('.', '/')
      + "/sort/";

  @Test
  public void sortAlphabetEnglish() throws Exception {
    final String name = PACKAGE + "sort-alphabet-english";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    t.setParameter("lang", "en");
    Assert.assertEquals(expected, t.getResultString());
  }

  @Test
  @Ignore("Collation is not used / lang attribute is ignored")
  public void sortAlphabetPolish() throws Exception {
    final String name = PACKAGE + "sort-alphabet-polish";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    t.setParameter("lang", "pl");
    Assert.assertEquals(expected, t.getResultString());
  }

  @Test
  @Ignore("Collation is not used / lang attribute is ignored")
  public void sortAlphabetRussian() throws Exception {
    final String name = PACKAGE + "sort-alphabet-russian";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    t.setParameter("lang", "ru");
    Assert.assertEquals(expected, t.getResultString());
  }

}
