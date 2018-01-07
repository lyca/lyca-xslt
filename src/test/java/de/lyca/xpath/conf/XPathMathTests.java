package de.lyca.xpath.conf;

import static de.lyca.xpath.ResourceUtils.getInputSource;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathVariableResolver;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import de.lyca.xpath.DocumentUtils;
import de.lyca.xpath.XPathVariableResolverBuilder;

public class XPathMathTests {

  private static final String PACKAGE = '/'
          + XPathMathTests.class.getPackage().getName().replace('.', '/').replaceFirst("xpath", "xslt") + "/math/";
  private static XPathFactory xpathfactory;
  private static XPath xpath;

  @BeforeClass
  public static void init() throws Exception {
    xpathfactory = XPathFactory.newInstance();
    xpath = xpathfactory.newXPath();
  }

  @Test
  public void math01() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math01.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(1.0, xpath.evaluate("number(doc/n1)", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math02() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math02.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(0d, xpath.evaluate("floor(0.0)", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math03() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math03.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(0d, xpath.evaluate("ceiling(0.0)", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math04() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math04.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(0d, xpath.evaluate("round(0.0)", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math05() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math05.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(10d, xpath.evaluate("sum(doc/n)", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math06() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math06.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(6d, xpath.evaluate("2*3", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math07() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math07.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(9d, xpath.evaluate("3+6", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math08() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math08.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(2d, xpath.evaluate("3-1", doc, XPathConstants.NUMBER));
    final XPath local = xpathfactory.newXPath();
    final XPathVariableResolver resolver = new XPathVariableResolverBuilder().var("h", 60).var("i", 20).build();
    local.setXPathVariableResolver(resolver);
    Assert.assertEquals(40d, local.evaluate("$h - $i", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math09() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math09.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(3d, xpath.evaluate("6 div 2", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math10() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math10.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(1d, xpath.evaluate("5 mod 2", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math11() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math11.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(Double.NaN, xpath.evaluate("number(foo)", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math12() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math12.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(2d, xpath.evaluate("number(2)", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math13() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math13.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(3d, xpath.evaluate("number('3')", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math14() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math14.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(Double.NaN, xpath.evaluate("number('')", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math15() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math15.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(Double.NaN, xpath.evaluate("number('abc')", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math16() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math16.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(1d, xpath.evaluate("number(string(1.0))=1", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math17() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math17.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(Double.NaN, xpath.evaluate("number(n4)", doc, XPathConstants.NUMBER));
    final XPath local = xpathfactory.newXPath();
    final XPathVariableResolver resolver = new XPathVariableResolverBuilder().var("resultTreeFragTest",
            local.evaluate("doc/n4", doc, XPathConstants.NODESET)).build();
    local.setXPathVariableResolver(resolver);
    Assert.assertEquals(4d, local.evaluate("number($resultTreeFragTest)", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math18() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math18.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    final XPath local = xpathfactory.newXPath();
    final XPathVariableResolver resolver = new XPathVariableResolverBuilder().var("emptyResultTreeFragTest",
            local.evaluate("foo", doc, XPathConstants.NODESET)).build();
    local.setXPathVariableResolver(resolver);
    Assert.assertEquals(Double.NaN, local.evaluate("number($emptyResultTreeFragTest)", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math19() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math19.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(1d, xpath.evaluate("number(true())=1", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math20() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math20.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(1d, xpath.evaluate("number(false())=0", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math21() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math21.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(0d, xpath.evaluate("number('xxx')=number('xxx')", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math22() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math22.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(0d, xpath.evaluate("number('xxx')=0", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math23() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math23.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(0d, xpath.evaluate("floor(doc/n0)", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math24() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math24.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(1d, xpath.evaluate("floor(1.9)", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math25() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math25.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(1d, xpath.evaluate("floor(doc/n1)", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math26() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math26.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(2d, xpath.evaluate("floor(2.999999)", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math27() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math27.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(2d, xpath.evaluate("floor(doc/n2)", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math28() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math28.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(-2d, xpath.evaluate("floor(-1.5)", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math29() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math29.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(1d, xpath.evaluate("floor(1)=1", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math30() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math30.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(1d, xpath.evaluate("floor(1.9)=1", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math31() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math31.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(1d, xpath.evaluate("floor(-1.5)=-2", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math32() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math32.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(0d, xpath.evaluate("ceiling(doc/n0)", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math33() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math33.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(2d, xpath.evaluate("ceiling(1.54)", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math34() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math34.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(2d, xpath.evaluate("ceiling(doc/n1)", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math35() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math35.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(3d, xpath.evaluate("ceiling(2.999999)", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math36() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math36.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(3d, xpath.evaluate("ceiling(doc/n2)", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math37() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math37.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(1d, xpath.evaluate("ceiling(1)=1", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math38() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math38.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(1d, xpath.evaluate("ceiling(1.1)=2", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math39() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math39.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(1d, xpath.evaluate("ceiling(-1.5)=-1", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math40() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math40.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(0d, xpath.evaluate("round(doc/n0)", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math41() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math41.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(1d, xpath.evaluate("round(1.24)", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math42() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math42.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(1d, xpath.evaluate("round(doc/n1)", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math43() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math43.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(3d, xpath.evaluate("round(2.999999)", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math44() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math44.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(3d, xpath.evaluate("round(doc/n2)", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math45() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math45.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(1d, xpath.evaluate("round(1.1)=1", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math46() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math46.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(1d, xpath.evaluate("round(-1.1)=-1", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math47() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math47.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(1d, xpath.evaluate("round(1.9)=2", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math48() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math48.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(1d, xpath.evaluate("round(-1.9)=-2", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math49() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math49.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(1d, xpath.evaluate("round(1.5)=2", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math50() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math50.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(3d, xpath.evaluate("round(2.5)", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math51() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math51.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(1d, xpath.evaluate("round(-2.5)=-2", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math52() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math52.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(-1d, xpath.evaluate("round(-1.5)", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math53() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math53.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(6d, xpath.evaluate("sum(doc/n/@attrib)", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math54() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math54.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(0d, xpath.evaluate("sum(doc/x)", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math55() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math55.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(6d, xpath.evaluate("doc/n1*doc/n2", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math56() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math56.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(8d, xpath.evaluate("(doc/n1/@attrib)*(doc/n2/@attrib)", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math57() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math57.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(9d, xpath.evaluate("doc/n1+doc/n2", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math58() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math58.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(10d, xpath.evaluate("(doc/n1/@attrib)+(doc/n2/@attrib)", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math59() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math59.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(10d, xpath.evaluate("doc/n1/@attrib + doc/n2/@attrib", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math60() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math60.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(-1d, xpath.evaluate("1-2", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math61() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math61.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(4d, xpath.evaluate("doc/n-2 - doc/n-1", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math62() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math62.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(4d, xpath.evaluate("7+-3", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math63() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math63.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(4d, xpath.evaluate("doc/n-2+-doc/n-1", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math64() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math64.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(10d, xpath.evaluate("7 - -3", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math65() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math65.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(10d, xpath.evaluate("doc/n-2 - -doc/n-1", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math66() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math66.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(-4d, xpath.evaluate("-7 --3", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math67() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math67.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(-4d, xpath.evaluate("-doc/n-2 --doc/n-1", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math68() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math68.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(8d, xpath.evaluate("-doc/n-2/@attrib --doc/n-1/@attrib", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math69() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math69.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(8d, xpath.evaluate("-(doc/n-2/@attrib) - -(doc/n-1/@attrib)", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math70() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math70.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(-3d, xpath.evaluate("6 div -2", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math71() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math71.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(2d, xpath.evaluate("doc/n1 div doc/n2", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math72() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math72.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(3d, xpath.evaluate("doc/div div doc/mod", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math73() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math73.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(2d, xpath.evaluate("doc/n1/@attrib div doc/n2/@attrib", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math74() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math74.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(10d, xpath.evaluate("doc/div/@attrib div doc/mod/@attrib", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math75() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math75.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(1d, xpath.evaluate("1 div -0 = 2 div -0", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math76() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math76.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(0d, xpath.evaluate("1 div -0 = 1 div 0", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math77() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math77.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(0d, xpath.evaluate("0 div 0 >= 0", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math78() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math78.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(0d, xpath.evaluate("0 div 0 < 0", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math79() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math79.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(1d, xpath.evaluate("doc/n1 mod doc/n2", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math80() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math80.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(-1d, xpath.evaluate("doc/div mod doc/mod", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math81() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math81.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(2d, xpath.evaluate("doc/n1/@attrib mod doc/n2/@attrib", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math82() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math82.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(-1d, xpath.evaluate("doc/div/@attrib mod doc/mod/@attrib", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math83() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math83.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(1d, xpath.evaluate("(5 mod 2 = 1) and (5 mod -2 = 1) and (-5 mod 2 = -1) and (-5 mod -2 = -1)",
            doc, XPathConstants.NUMBER));
  }

  @Test
  public void math84() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math84.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(15d, xpath.evaluate("sum(doc/av//h)", doc, XPathConstants.NUMBER));
    final XPath local = xpathfactory.newXPath();
    final XPathVariableResolver resolver = new XPathVariableResolverBuilder().var("rtf",
            local.evaluate("doc/av//h", doc, XPathConstants.NODESET)).build();
    local.setXPathVariableResolver(resolver);
    Assert.assertEquals(15d, local.evaluate("sum($rtf)", doc, XPathConstants.NUMBER));

  }

  @Test
  public void math85() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math85.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(4d, xpath.evaluate(
            "((((((doc/n3+5)*(3)+(((doc/n2)+2)*(doc/n1 - 6)))-(doc/n4 - doc/n2))+(-(4-6)))))", doc,
            XPathConstants.NUMBER));
  }

  @Test
  public void math86() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math86.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(24d, xpath.evaluate("doc/n1*doc/n2*doc/n3*doc/n4", doc, XPathConstants.NUMBER));
    Assert.assertEquals(63d, xpath.evaluate("doc/n1*doc/n2*doc/n3*doc/n4*doc/n5*doc/n6*doc/n7*doc/n8*doc/n9*doc/n10",
            doc, XPathConstants.NUMBER));
  }

  @Test
  public void math87() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math87.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(60d, xpath.evaluate("doc/n0 div doc/n1 div doc/n2 div doc/n3", doc, XPathConstants.NUMBER));
    Assert.assertEquals(6d,
            xpath.evaluate("doc/n0 div doc/n1 div doc/n2 div doc/n3 div doc/n4", doc, XPathConstants.NUMBER));
    Assert.assertEquals(2d,
            xpath.evaluate("doc/n0 div doc/n1 div doc/n2 div doc/n3 div doc/n4 div doc/n5", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math88() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math88.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(2d, xpath.evaluate(
            "(doc/n1*doc/n2*doc/n3*doc/n4*doc/n5*doc/n6)div doc/n7 div doc/n8 div doc/n9 div doc/n10", doc,
            XPathConstants.NUMBER));
  }

  @Test
  public void math89() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math89.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(Double.NaN, xpath.evaluate("(2 + number('xxx'))", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math90() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math90.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(Double.NaN, xpath.evaluate("2 * -number('xxx')", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math91() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math91.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(Double.NaN, xpath.evaluate("2 - number('xxx')", doc, XPathConstants.NUMBER));
    Assert.assertEquals(Double.NaN, xpath.evaluate("number('xxx') - 10", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math92() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math92.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(Double.NaN, xpath.evaluate("2 div number('xxx')", doc, XPathConstants.NUMBER));
    Assert.assertEquals(Double.NaN, xpath.evaluate("number('xxx') div 3", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math93() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math93.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(Double.NaN, xpath.evaluate("2 mod number('xxx')", doc, XPathConstants.NUMBER));
    Assert.assertEquals(Double.NaN, xpath.evaluate("number('xxx') mod 3", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math94() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math94.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(Double.NaN, xpath.evaluate("floor(number('xxx'))", doc, XPathConstants.NUMBER));
    Assert.assertEquals(Double.NaN, xpath.evaluate("ceiling(number('xxx'))", doc, XPathConstants.NUMBER));
    Assert.assertEquals(Double.NaN, xpath.evaluate("round(number('xxx'))", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math95() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math95.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(Double.NaN, xpath.evaluate("sum(doc/e)", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math96() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math96.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(-17d, xpath.evaluate("sum(doc/e)", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math97() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math97.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(200d, xpath.evaluate("10+5+25+20+15+50+35+40", doc, XPathConstants.NUMBER));
    Assert.assertEquals(17d, xpath.evaluate("2+doc/n5+7+doc/n3", doc, XPathConstants.NUMBER));
    final XPath local = xpathfactory.newXPath();
    final XPathVariableResolver resolver = new XPathVariableResolverBuilder().var("anum", 10).build();
    local.setXPathVariableResolver(resolver);
    Assert.assertEquals(27d, local.evaluate("doc/n2+3+$anum+7+doc/n5", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math98() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math98.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(40d, xpath.evaluate("100-9-7-4-17-18-5", doc, XPathConstants.NUMBER));
    Assert.assertEquals(77d, xpath.evaluate("100-doc/n6 -4-doc/n1 -1-11", doc, XPathConstants.NUMBER));
    final XPath local = xpathfactory.newXPath();
    final XPathVariableResolver resolver = new XPathVariableResolverBuilder().var("anum", 10).build();
    local.setXPathVariableResolver(resolver);
    Assert.assertEquals(60d, local.evaluate("100-$anum -5-15-$anum", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math99() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math99.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(17d, xpath.evaluate("3*2+5*4-4*2-1", doc, XPathConstants.NUMBER));
    Assert.assertEquals(24d, xpath.evaluate("doc/n6*5-8*doc/n2+5*2", doc, XPathConstants.NUMBER));
    final XPath local = xpathfactory.newXPath();
    final XPathVariableResolver resolver = new XPathVariableResolverBuilder().var("anum", 10).build();
    local.setXPathVariableResolver(resolver);
    Assert.assertEquals(39d, local.evaluate("$anum*5-4*doc/n2+doc/n6*doc/n1 -doc/n3*3", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math100() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math100.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(5d, xpath.evaluate("(24 div 3 +2) div (40 div 8 -3)", doc, XPathConstants.NUMBER));
    Assert.assertEquals(44d, xpath.evaluate("80 div doc/n2 + 12 div 2 - doc/n4 div doc/n2", doc, XPathConstants.NUMBER));
    final XPath local = xpathfactory.newXPath();
    final XPathVariableResolver resolver = new XPathVariableResolverBuilder().var("anum", 10).build();
    local.setXPathVariableResolver(resolver);
    Assert.assertEquals(9d,
            local.evaluate("70 div $anum - 18 div doc/n6 + $anum div doc/n2", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math101() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math101.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(15d, xpath.evaluate("48 mod 17 - 2 mod 9 + 13 mod 5", doc, XPathConstants.NUMBER));
    Assert.assertEquals(2d,
            xpath.evaluate("56 mod round(doc/n5*2+1.444) - doc/n6 mod 4 + 7 mod doc/n4", doc, XPathConstants.NUMBER));
    final XPath local = xpathfactory.newXPath();
    final XPathVariableResolver resolver = new XPathVariableResolverBuilder().var("anum", 10).build();
    local.setXPathVariableResolver(resolver);
    Assert.assertEquals(2d, local.evaluate("(77 mod $anum + doc/n5 mod 8) mod $anum", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math102() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math102.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(17d, xpath.evaluate("number(doc)", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math103() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math103.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(-3d, xpath.evaluate("-(doc/n1|doc/n2)", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math104() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math104.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertFalse((boolean) xpath.evaluate("contains(number(doc/n[1]),'NaN')", doc, XPathConstants.BOOLEAN));
    Assert.assertFalse((boolean) xpath.evaluate("contains(number(doc/n[2]),'NaN')", doc, XPathConstants.BOOLEAN));
    Assert.assertFalse((boolean) xpath.evaluate("contains(number(doc/n[3]),'NaN')", doc, XPathConstants.BOOLEAN));
    Assert.assertFalse((boolean) xpath.evaluate("contains(number(doc/n[4]),'NaN')", doc, XPathConstants.BOOLEAN));
    Assert.assertFalse((boolean) xpath.evaluate("contains(number(doc/n[5]),'NaN')", doc, XPathConstants.BOOLEAN));
    Assert.assertTrue((boolean) xpath.evaluate("contains(number(doc/n[6]),'NaN')", doc, XPathConstants.BOOLEAN));
    Assert.assertTrue((boolean) xpath.evaluate("contains(number(doc/n[7]),'NaN')", doc, XPathConstants.BOOLEAN));
    Assert.assertTrue((boolean) xpath.evaluate("contains(number(doc/n[8]),'NaN')", doc, XPathConstants.BOOLEAN));
    Assert.assertTrue((boolean) xpath.evaluate("contains(number(doc/n[9]),'NaN')", doc, XPathConstants.BOOLEAN));
    Assert.assertFalse((boolean) xpath.evaluate("contains(number(doc/n[10]),'NaN')", doc, XPathConstants.BOOLEAN));
    Assert.assertFalse((boolean) xpath.evaluate("contains(number(doc/n[11]),'NaN')", doc, XPathConstants.BOOLEAN));
    Assert.assertFalse((boolean) xpath.evaluate("contains(number(doc/n[12]),'NaN')", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void math105() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math105.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(9876543210d, xpath.evaluate("9876543210", doc, XPathConstants.NUMBER));
  }

  @Test
  public void math110() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math110.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals(1.75, xpath.evaluate("number(1.75)", doc, XPathConstants.NUMBER));
    Assert.assertEquals(1.75, xpath.evaluate("number(7 div 4)", doc, XPathConstants.NUMBER));
    Assert.assertEquals(1d, xpath.evaluate("(number(1.75) = (7 div 4))", doc, XPathConstants.NUMBER));
    Assert.assertEquals(1.75, xpath.evaluate("number(0.109375 * 16)", doc, XPathConstants.NUMBER));
    Assert.assertEquals(1d, xpath.evaluate("(number(1.75) = (0.109375 * 16))", doc, XPathConstants.NUMBER));
    Assert.assertEquals(0.0004, xpath.evaluate("number(doc/k)", doc, XPathConstants.NUMBER));
    Assert.assertEquals(0.0004, xpath.evaluate("number(4 div 10000)", doc, XPathConstants.NUMBER));
    Assert.assertEquals(1d, xpath.evaluate("(number(doc/k) = (4 div 10000))", doc, XPathConstants.NUMBER));
    Assert.assertEquals(0.0004, xpath.evaluate("number(0.0001 * 4)", doc, XPathConstants.NUMBER));
    Assert.assertEquals(1d, xpath.evaluate("(number(doc/k) = (0.0001 * 4))", doc, XPathConstants.NUMBER));

  }

  @Test
  public void math111() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "math111.xml");
    final Document doc = DocumentUtils.fromSource(xml);
    Assert.assertEquals("0", xpath.evaluate("string(number(doc/number[1]))", doc, XPathConstants.STRING));
    Assert.assertEquals("0", xpath.evaluate("string(-1 * number(doc/number[1]))", doc, XPathConstants.STRING));
    Assert.assertEquals("0.4", xpath.evaluate("string(number(doc/number[2]))", doc, XPathConstants.STRING));
    Assert.assertEquals("-0.4", xpath.evaluate("string(-1 * number(doc/number[2]))", doc, XPathConstants.STRING));
    Assert.assertEquals("4", xpath.evaluate("string(number(doc/number[3]))", doc, XPathConstants.STRING));
    Assert.assertEquals("-4", xpath.evaluate("string(-1 * number(doc/number[3]))", doc, XPathConstants.STRING));
    Assert.assertEquals("0.04", xpath.evaluate("string(number(doc/number[4]))", doc, XPathConstants.STRING));
    Assert.assertEquals("-0.04", xpath.evaluate("string(-1 * number(doc/number[4]))", doc, XPathConstants.STRING));
    Assert.assertEquals("0.004", xpath.evaluate("string(number(doc/number[5]))", doc, XPathConstants.STRING));
    Assert.assertEquals("-0.004", xpath.evaluate("string(-1 * number(doc/number[5]))", doc, XPathConstants.STRING));
    Assert.assertEquals("0.0004", xpath.evaluate("string(number(doc/number[6]))", doc, XPathConstants.STRING));
    Assert.assertEquals("-0.0004", xpath.evaluate("string(-1 * number(doc/number[6]))", doc, XPathConstants.STRING));
    Assert.assertEquals("0.0000000000001", xpath.evaluate("string(number(doc/number[7]))", doc, XPathConstants.STRING));
    Assert.assertEquals("-0.0000000000001",
            xpath.evaluate("string(-1 * number(doc/number[7]))", doc, XPathConstants.STRING));
    Assert.assertEquals("0.0000000000000000000000000001",
            xpath.evaluate("string(number(doc/number[8]))", doc, XPathConstants.STRING));
    Assert.assertEquals("-0.0000000000000000000000000001",
            xpath.evaluate("string(-1 * number(doc/number[8]))", doc, XPathConstants.STRING));
    Assert.assertEquals("0.0000000000001000000000000001",
            xpath.evaluate("string(number(doc/number[9]))", doc, XPathConstants.STRING));
    Assert.assertEquals("-0.0000000000001000000000000001",
            xpath.evaluate("string(-1 * number(doc/number[9]))", doc, XPathConstants.STRING));
    Assert.assertEquals("0.0012", xpath.evaluate("string(number(doc/number[10]))", doc, XPathConstants.STRING));
    Assert.assertEquals("-0.0012", xpath.evaluate("string(-1 * number(doc/number[10]))", doc, XPathConstants.STRING));
    Assert.assertEquals("0.012", xpath.evaluate("string(number(doc/number[11]))", doc, XPathConstants.STRING));
    Assert.assertEquals("-0.012", xpath.evaluate("string(-1 * number(doc/number[11]))", doc, XPathConstants.STRING));
  }

}
