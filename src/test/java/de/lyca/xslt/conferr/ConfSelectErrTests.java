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
public class ConfSelectErrTests {

  private static final String PACKAGE = '/' + ConfSelectErrTests.class.getPackage().getName().replace('.', '/')
      + "/selecterr/";

  @Parameters(name = "{0}")
  public static Collection<Object> params() {
    Collection<Object> result = new ArrayList<>();
    // When a permitted operation is performed on a result tree fragment, it is
    // performed exactly as it would be on the equivalent node-set. The question
    // is, if for-each is permitted on a RTF.
    int[] exclude = { 13 };
    for (int i = 1; i < 25; i++) {
      if (Arrays.binarySearch(exclude, i) >= 0) {
        continue;
      }
      result.add(String.format("selecterr%02d", i));
    }
    return result;
  }

  private String name;

  public ConfSelectErrTests(String name) {
    this.name = PACKAGE + name;
  }

  @Test
  public void selecterrTest() throws Exception {
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    try {
      fail(new Transform(xml, xsl).getResultString());
    } catch (final TransformerConfigurationException e) {
    }
  }

}
