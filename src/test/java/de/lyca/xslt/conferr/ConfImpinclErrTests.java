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
public class ConfImpinclErrTests {

  private static final String PACKAGE = '/' + ConfImpinclErrTests.class.getPackage().getName().replace('.', '/')
      + "/impinclerr/";

  @Parameters(name = "{0}")
  public static Collection<Object> params() {
    Collection<Object> result = new ArrayList<>();
    int[] exclude = {};
    for (int i = 1; i < 11; i++) {
      if (Arrays.binarySearch(exclude, i) >= 0) {
        continue;
      }
      result.add(String.format("impinclerr%02d", i));
    }
    return result;
  }

  private String name;

  public ConfImpinclErrTests(String name) {
    this.name = PACKAGE + name;
  }

  @Test
  public void impinclerrTest() throws Exception {
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    try {
      fail(new Transform(xml, xsl).getResultString());
    } catch (final TransformerConfigurationException e) {
    }
  }

  // @Test
  // @Ignore("StackOverflowError")
  // public void impinclerr01() throws Exception {
  // final String name = PACKAGE + "impinclerr01";
  // final Source xsl = getSource(name + ".xsl");
  // final Source xml = getSource(name + ".xml");
  // final Transform t = new Transform(xml, xsl);
  // }
  //
  // @Test
  // public void impinclerr02() throws Exception {
  // final String name = PACKAGE + "impinclerr02";
  // final Source xsl = getSource(name + ".xsl");
  // final Source xml = getSource(name + ".xml");
  // final Transform t = new Transform(xml, xsl);
  // }
  //
  // @Test
  // public void impinclerr03() throws Exception {
  // final String name = PACKAGE + "impinclerr03";
  // final Source xsl = getSource(name + ".xsl");
  // final Source xml = getSource(name + ".xml");
  // final Transform t = new Transform(xml, xsl);
  // }
  //
  // @Test
  // @Ignore("StackOverflowError")
  // public void impinclerr04() throws Exception {
  // final String name = PACKAGE + "impinclerr04";
  // final Source xsl = getSource(name + ".xsl");
  // final Source xml = getSource(name + ".xml");
  // final Transform t = new Transform(xml, xsl);
  // }
  //
  // @Test
  // @Ignore("StackOverflowError")
  // public void impinclerr05() throws Exception {
  // final String name = PACKAGE + "impinclerr05";
  // final Source xsl = getSource(name + ".xsl");
  // final Source xml = getSource(name + ".xml");
  // final Transform t = new Transform(xml, xsl);
  // }
  //
  // @Test
  // @Ignore("StackOverflowError")
  // public void impinclerr06() throws Exception {
  // final String name = PACKAGE + "impinclerr06";
  // final Source xsl = getSource(name + ".xsl");
  // final Source xml = getSource(name + ".xml");
  // final Transform t = new Transform(xml, xsl);
  // }
  //
  // @Test
  // @Ignore("NullPointerException")
  // public void impinclerr07() throws Exception {
  // final String name = PACKAGE + "impinclerr07";
  // final Source xsl = getSource(name + ".xsl");
  // final Source xml = getSource(name + ".xml");
  // final Transform t = new Transform(xml, xsl);
  // }
  //
  // @Test
  // public void impinclerr08() throws Exception {
  // final String name = PACKAGE + "impinclerr08";
  // final Source xsl = getSource(name + ".xsl");
  // final Source xml = getSource(name + ".xml");
  // final Transform t = new Transform(xml, xsl);
  // }
  //
  // @Test
  // public void impinclerr09() throws Exception {
  // final String name = PACKAGE + "impinclerr09";
  // final Source xsl = getSource(name + ".xsl");
  // final Source xml = getSource(name + ".xml");
  // final Transform t = new Transform(xml, xsl);
  // }
  //
  // @Test
  // @Ignore("Include file not found")
  // public void impinclerr10() throws Exception {
  // final String name = PACKAGE + "impinclerr10";
  // final Source xsl = getSource(name + ".xsl");
  // final Source xml = getSource(name + ".xml");
  // final Transform t = new Transform(xml, xsl);
  // }

}
