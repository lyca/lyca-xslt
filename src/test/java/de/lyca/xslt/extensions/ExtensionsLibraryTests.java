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
public class ExtensionsLibraryTests {

  private static final String PACKAGE = '/' + ExtensionsLibraryTests.class.getPackage().getName().replace('.', '/')
      + "/library/";

  @Parameters(name = "{0}")
  public static Collection<Object> params() {
    // evaluate noot supported in the moment; some issues with Axis.FOLLOWING in
    // complex Nodeset scenarios
    Arrays.asList("libraryEvaluate01", "libraryNodeset05", "libraryNodeset06");
    return Arrays.asList("libraryDifference01", "libraryDistinct01", "libraryHasSameNodes01", "libraryIntersection01",
        "libraryMath01", "libraryMath02", "libraryNodeset01", "libraryNodeset02", "libraryNodeset03",
        "libraryNodeset04", "libraryNodeset07", "libraryNodeset08", "librarySet01", "librarySet02",
        "libraryTokenize01");
  }

  private String name;

  public ExtensionsLibraryTests(String name) {
    this.name = PACKAGE + name;
  }

  @Test
  public void extensionsLibraryTest() throws Exception {
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    Assert.assertEquals(expected, t.getResultString());
  }

}
