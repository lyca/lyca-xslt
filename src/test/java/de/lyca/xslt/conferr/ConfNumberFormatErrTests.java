package de.lyca.xslt.conferr;

import static de.lyca.xslt.ResourceUtils.getSource;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.lyca.xslt.Transform;

@RunWith(Parameterized.class)
public class ConfNumberFormatErrTests {

  private static final String PACKAGE = '/' + ConfNumberFormatErrTests.class.getPackage().getName().replace('.', '/')
      + "/numberformaterr/";

  @Parameters(name = "{0}")
  public static Collection<Object> params() {
    Collection<Object> result = new ArrayList<>();
    // excluded tests contain 'valid' format (at least the jdk thinks so)
    int[] exclude = { 18, 19, 24, 25, 29, 30 };
    for (int i = 1; i < 31; i++) {
      if (Arrays.binarySearch(exclude, i) >= 0) {
        continue;
      }
      result.add(String.format("numberformaterr%02d", i));
    }
    return result;
  }

  private String name;

  public ConfNumberFormatErrTests(String name) {
    this.name = PACKAGE + name;
  }

  @Test
  public void numberformaterrTest() throws Exception {
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    try {
      fail(new Transform(xml, xsl).getResultString());
    } catch (final TransformerException e) {
    }
  }

}
