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
public class ConfSortErrTests {

  private static final String PACKAGE = '/' + ConfSortErrTests.class.getPackage().getName().replace('.', '/')
      + "/sorterr/";

  @Parameters(name = "{0}")
  public static Collection<Object> params() {
    Collection<Object> result = new ArrayList<>();
    // not existent: 10, hard to handle: 8, 11
    int[] exclude = { 8, 10, 11 };
    for (int i = 1; i < 12; i++) {
      if (Arrays.binarySearch(exclude, i) >= 0) {
        continue;
      }
      result.add(String.format("sorterr%02d", i));
    }
    return result;
  }

  private String name;

  public ConfSortErrTests(String name) {
    this.name = PACKAGE + name;
  }

  @Test
  public void sorterrTest() throws Exception {
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    try {
      fail(new Transform(xml, xsl).getResultString());
    } catch (final TransformerConfigurationException e) {
    }
  }

}
