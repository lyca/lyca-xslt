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
public class ConfMathErrTests {

  private static final String PACKAGE = '/' + ConfMathErrTests.class.getPackage().getName().replace('.', '/')
      + "/matherr/";

  @Parameters(name = "{0}")
  public static Collection<Object> params() {
    Collection<Object> result = new ArrayList<>();
    int[] exclude = {};
    for (int i = 1; i < 15; i++) {
      if (Arrays.binarySearch(exclude, i) >= 0) {
        continue;
      }
      result.add(String.format("matherr%02d", i));
    }
    return result;
  }

  private String name;

  public ConfMathErrTests(String name) {
    this.name = PACKAGE + name;
  }

  @Test
  public void matherrTest() throws Exception {
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    try {
      fail(new Transform(xml, xsl).getResultString());
    } catch (final TransformerConfigurationException e) {
    }
  }

}
