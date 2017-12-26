package de.lyca.xslt.conferr;

import static de.lyca.xslt.ResourceUtils.getSource;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.lyca.xslt.Transform;

@RunWith(Parameterized.class)
public class ConfConditionalErrTests {

  private static final String PACKAGE = '/' + ConfConditionalErrTests.class.getPackage().getName().replace('.', '/')
      + "/conditionalerr/";

  @Parameters(name = "{0}")
  public static Collection<Object> params() {
    Collection<Object> result = new ArrayList<>();
    int[] exclude = {};
    for (int i = 1; i < 23; i++) {
      if (Arrays.binarySearch(exclude, i) >= 0) {
        continue;
      }
      result.add(String.format("conditionalerr%02d", i));
    }
    return result;
  }

  private String name;

  public ConfConditionalErrTests(String name) {
    this.name = PACKAGE + name;
  }

  @Test
  public void conditionalerrTest() throws Exception {
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    try {
      fail(new Transform(xml, xsl).getResultString());
    } catch (final TransformerConfigurationException e) {
    }
  }

  public void conditionalerr05() throws Exception {
    final String name = PACKAGE + "conditionalerr05";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    // Is it really an error, if a xsl:when is placed after a xsl:otherwise?
    new Transform(xml, xsl);
  }

  public void conditionalerr06() throws Exception {
    final String name = PACKAGE + "conditionalerr06";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    // Is it really an error, if a wrong attribute on xsl:when is just ignored?
    new Transform(xml, xsl);
  }

  public void conditionalerr07() throws Exception {
    final String name = PACKAGE + "conditionalerr07";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    // Is it really an error, if a wrong attribute on xsl:otherwise is just
    // ignored?
    new Transform(xml, xsl);
  }

  public void conditionalerr08() throws Exception {
    final String name = PACKAGE + "conditionalerr08";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    // Is it really an error, if a wrong attribute on xsl:choose is just
    // ignored?
    new Transform(xml, xsl);
  }

  public void conditionalerr19() throws Exception {
    final String name = PACKAGE + "conditionalerr19";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    // xsl:choose is just ignored in top level position
    new Transform(xml, xsl);
  }

  public void conditionalerr20() throws Exception {
    final String name = PACKAGE + "conditionalerr20";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    // xsl:if is just ignored in top level position
    new Transform(xml, xsl);
  }

  public void conditionalerr21() throws Exception {
    final String name = PACKAGE + "conditionalerr21";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    // xsl:when is just ignored in top level position
    new Transform(xml, xsl);
  }

  public void conditionalerr22() throws Exception {
    final String name = PACKAGE + "conditionalerr22";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    // xsl:otherwise is just ignored in top level position
    new Transform(xml, xsl);
  }

}
