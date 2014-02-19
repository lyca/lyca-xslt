package de.lyca.xpath.conf;

import static de.lyca.xpath.ResourceUtils.getInputSource;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathVariableResolver;

import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import de.lyca.xpath.XPathVariableResolverBuilder;

public class XPathBooleanTests {

  private static final String PACKAGE = '/'
          + XPathBooleanTests.class.getPackage().getName().replace('.', '/').replaceFirst("xpath", "xslt") + "/bool/";
  private static XPathFactory xpathfactory;
  private static XPath xpath;

  @BeforeClass
  public static void init() throws Exception {
    xpathfactory = XPathFactory.newInstance();
    xpath = xpathfactory.newXPath();
  }

  @Test
  public void boolean01() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean01.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertTrue((boolean) xpath.evaluate("true()", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean02() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean02.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertTrue((boolean) xpath.evaluate("true() and true()", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean03() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean03.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertTrue((boolean) xpath.evaluate("true() or true()", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean04() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean04.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertFalse((boolean) xpath.evaluate("not(true())", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean05() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean05.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertFalse((boolean) xpath.evaluate("boolean('')", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean06() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean06.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertFalse((boolean) xpath.evaluate("1>2", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean07() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean07.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertFalse((boolean) xpath.evaluate("1>=2", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean08() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean08.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertTrue((boolean) xpath.evaluate("/doc/p[1][lang('en')]", doc, XPathConstants.BOOLEAN));
    Assert.assertTrue((boolean) xpath.evaluate("/doc/p[2][lang('en')]", doc, XPathConstants.BOOLEAN));
    Assert.assertTrue((boolean) xpath.evaluate("/doc/p[3][lang('en')]", doc, XPathConstants.BOOLEAN));
    Assert.assertTrue((boolean) xpath.evaluate("/doc/p[4][lang('en')]", doc, XPathConstants.BOOLEAN));
    Assert.assertFalse((boolean) xpath.evaluate("/doc/p[5][lang('en')]", doc, XPathConstants.BOOLEAN));
    Assert.assertFalse((boolean) xpath.evaluate("/doc/p[6][lang('en')]", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean09() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean09.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertFalse((boolean) xpath.evaluate("false()", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean10() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean10.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertTrue((boolean) xpath.evaluate("1=1", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean11() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean11.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertFalse((boolean) xpath.evaluate("1=2", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean12() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean12.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertTrue((boolean) xpath.evaluate("1 = 1.00", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean13() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean13.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertTrue((boolean) xpath.evaluate("0 = -0", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean14() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean14.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertTrue((boolean) xpath.evaluate("1 = '001'", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean15() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean15.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertTrue((boolean) xpath.evaluate("true()='0'", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean16() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean16.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertTrue((boolean) xpath.evaluate("false()=''", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean17() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean17.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertTrue((boolean) xpath.evaluate("true()=2", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean18() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean18.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertTrue((boolean) xpath.evaluate("false()=0", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean19() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean19.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertFalse((boolean) xpath.evaluate("false() and false()", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean20() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean20.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertTrue((boolean) xpath.evaluate("'foo' and 'fop'", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean21() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean21.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertFalse((boolean) xpath.evaluate("true() and false()", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean22() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean22.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertFalse((boolean) xpath.evaluate("false() and true()", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean23() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean23.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertTrue((boolean) xpath.evaluate("'1' and '0'", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean24() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean24.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertTrue((boolean) xpath.evaluate("true() or false()", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean25() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean25.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertTrue((boolean) xpath.evaluate("false() or true()", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean26() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean26.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertFalse((boolean) xpath.evaluate("false() or false()", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean27() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean27.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertFalse((boolean) xpath.evaluate("0 or ''", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean28() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean28.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertTrue((boolean) xpath.evaluate("not(false())", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean29() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean29.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertFalse((boolean) xpath.evaluate("not(false() = false())", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean30() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean30.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertTrue((boolean) xpath.evaluate("not(true() = false())", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean31() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean31.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertTrue((boolean) xpath.evaluate("not('')", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean32() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean32.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertFalse((boolean) xpath.evaluate("not('0')", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean33() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean33.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertTrue((boolean) xpath.evaluate("boolean('0')", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean34() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean34.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertFalse((boolean) xpath.evaluate("boolean(0)", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean35() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean35.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertFalse((boolean) xpath.evaluate("boolean(-0)", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean36() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean36.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertTrue((boolean) xpath.evaluate("boolean(1)", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean37() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean37.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertTrue((boolean) xpath.evaluate("boolean(1 div 0)", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean38() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean38.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("NaN", xpath.evaluate("0 div 0", doc));
  }

  @Test
  public void boolean39() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean39.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertFalse((boolean) xpath.evaluate("boolean(0 div 0)", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean40() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean40.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertTrue((boolean) xpath.evaluate("boolean(doc)", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean41() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean41.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertFalse((boolean) xpath.evaluate("boolean(foo)", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean42() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean42.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final XPath local = xpathfactory.newXPath();
    final XPathVariableResolver resolver = new XPathVariableResolverBuilder().var("ResultTreeFragTest",
            local.evaluate("doc", doc, XPathConstants.NODESET)).build();
    local.setXPathVariableResolver(resolver);
    Assert.assertTrue((boolean) local.evaluate("boolean($ResultTreeFragTest)", doc, XPathConstants.BOOLEAN));
    Assert.assertTrue((boolean) xpath.evaluate("doc[boolean(.)]", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean43() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean43.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final XPath local = xpathfactory.newXPath();
    final XPathVariableResolver resolver = new XPathVariableResolverBuilder().var("emptyResultTreeFragTest",
            local.evaluate("foo", doc, XPathConstants.NODESET)).build();
    local.setXPathVariableResolver(resolver);
    Assert.assertFalse((boolean) local.evaluate("boolean($emptyResultTreeFragTest)", doc, XPathConstants.BOOLEAN));
    Assert.assertFalse((boolean) xpath.evaluate("doc/text()[boolean(child::*)]", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean44() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean44.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertFalse((boolean) xpath.evaluate("1>1", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean45() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean45.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertTrue((boolean) xpath.evaluate("2>1", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean46() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean46.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertTrue((boolean) xpath.evaluate("1<2", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean47() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean47.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertFalse((boolean) xpath.evaluate("1<1", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean48() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean48.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertFalse((boolean) xpath.evaluate("2<1", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean49() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean49.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertTrue((boolean) xpath.evaluate("'2'>'1'", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean50() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean50.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertFalse((boolean) xpath.evaluate("0 > -0", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean51() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean51.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertTrue((boolean) xpath.evaluate("2>=2", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean52() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean52.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertTrue((boolean) xpath.evaluate("2>=1", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean53() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean53.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertTrue((boolean) xpath.evaluate("1<=2", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean54() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean54.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertTrue((boolean) xpath.evaluate("1<=1", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean55() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean55.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertFalse((boolean) xpath.evaluate("2<=1", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean56() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean56.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertFalse((boolean) xpath.evaluate("false() and 1 div 0", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean57() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean57.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertTrue((boolean) xpath.evaluate("true() or 1 div 0", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean58() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean58.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final XPath local = xpathfactory.newXPath();
    final XPathVariableResolver resolver = new XPathVariableResolverBuilder()
            .var("x", local.evaluate("doc/av//*", doc, XPathConstants.NODESET))
            .var("y", local.evaluate("doc/av//j", doc, XPathConstants.NODESET)).build();
    local.setXPathVariableResolver(resolver);
    Assert.assertTrue((boolean) local.evaluate("$x = 'foo'", doc, XPathConstants.BOOLEAN));
    Assert.assertFalse((boolean) local.evaluate("not($x != 'foo')", doc, XPathConstants.BOOLEAN));
    Assert.assertTrue((boolean) local.evaluate("$y = 'foo'", doc, XPathConstants.BOOLEAN));
    Assert.assertTrue((boolean) local.evaluate("not($y!= 'foo')", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean59() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean59.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final XPath local = xpathfactory.newXPath();
    final XPathVariableResolver resolver = new XPathVariableResolverBuilder().var("x",
            local.evaluate("doc/avj//k", doc, XPathConstants.NODESET)).build();
    local.setXPathVariableResolver(resolver);
    Assert.assertFalse((boolean) local.evaluate("$x = 'foo'", doc, XPathConstants.BOOLEAN));
    Assert.assertTrue((boolean) local.evaluate("not($x = 'foo')", doc, XPathConstants.BOOLEAN));
    Assert.assertFalse((boolean) local.evaluate("$x != 'foo'", doc, XPathConstants.BOOLEAN));
    Assert.assertTrue((boolean) local.evaluate("not($x != 'foo')", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean60() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean60.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertFalse((boolean) xpath.evaluate("1!=1", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean61() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean61.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertTrue((boolean) xpath.evaluate("1!=2", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean62() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean62.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertFalse((boolean) xpath.evaluate("1 != 1.00", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean63() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean63.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertTrue((boolean) xpath.evaluate("false()!=true()", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean64() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean64.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertTrue((boolean) xpath.evaluate("true()!=false()", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean65() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean65.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertFalse((boolean) xpath.evaluate("false()!=false()", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean66() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean66.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertFalse((boolean) xpath.evaluate("'ace' != 'ace'", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean67() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean67.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertTrue((boolean) xpath.evaluate("'ace' != 'abc'", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean68() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean68.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertTrue((boolean) xpath.evaluate("'H' != '  H'", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean69() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean69.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertTrue((boolean) xpath.evaluate("'H' != 'H  '", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean70() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean70.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertTrue((boolean) xpath.evaluate("/doc/j[@l='12'] = /doc/j[@w='33']", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean71() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean71.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertFalse((boolean) xpath.evaluate("/doc/j[@l='12'] = /doc/j[@l='17']", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean72() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean72.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertTrue((boolean) xpath.evaluate("/doc/j[@l='12'] = /doc/j[@w='45']", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean73() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean73.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertTrue((boolean) xpath.evaluate("/doc/j[@l='12'] != /doc/j[@w='33']", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean74() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean74.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertTrue((boolean) xpath.evaluate("/doc/j[@l='12'] != /doc/j[@l='17']", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean75() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean75.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertTrue((boolean) xpath.evaluate("/doc/j[@l='12'] != /doc/j[@w='45']", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean76() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean76.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertFalse((boolean) xpath.evaluate("/doc/j[@l='16'] != /doc/j[@w='78']", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean77() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean77.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertTrue((boolean) xpath.evaluate("1.9999999 < 2.0", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean78() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean78.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertFalse((boolean) xpath.evaluate("2.0000001 < 2.0", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean79() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean79.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertTrue((boolean) xpath.evaluate("1.9999999 < 2", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean80() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean80.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertFalse((boolean) xpath.evaluate("2 < 2.0", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean81() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean81.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertTrue((boolean) xpath.evaluate("'001' = 1", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean82() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean82.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertTrue((boolean) xpath.evaluate("0=false()", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean83() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean83.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertTrue((boolean) xpath.evaluate("'0'=true()", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean84() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean84.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final XPath local = xpathfactory.newXPath();
    final XPathVariableResolver resolver = new XPathVariableResolverBuilder().var("x",
            local.evaluate("doc/avj/good/*", doc, XPathConstants.NODESET)).build();
    local.setXPathVariableResolver(resolver);
    Assert.assertTrue((boolean) local.evaluate("$x = 34", doc, XPathConstants.BOOLEAN));
    Assert.assertFalse((boolean) local.evaluate("not($x = 34)", doc, XPathConstants.BOOLEAN));
    Assert.assertTrue((boolean) local.evaluate("$x != 34", doc, XPathConstants.BOOLEAN));
    Assert.assertFalse((boolean) local.evaluate("not($x != 34)", doc, XPathConstants.BOOLEAN));
    Assert.assertTrue((boolean) local.evaluate("34 = $x", doc, XPathConstants.BOOLEAN));
    Assert.assertFalse((boolean) local.evaluate("not(34 = $x)", doc, XPathConstants.BOOLEAN));
    Assert.assertTrue((boolean) local.evaluate("34 != $x", doc, XPathConstants.BOOLEAN));
    Assert.assertFalse((boolean) local.evaluate("not(34 != $x)", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean85() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean85.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final XPath local = xpathfactory.newXPath();
    final XPathVariableResolver resolver = new XPathVariableResolverBuilder().var("x",
            local.evaluate("doc/avj/bool/*", doc, XPathConstants.NODESET)).build();
    local.setXPathVariableResolver(resolver);
    Assert.assertTrue((boolean) local.evaluate("$x = true()", doc, XPathConstants.BOOLEAN));
    Assert.assertFalse((boolean) local.evaluate("not($x = true())", doc, XPathConstants.BOOLEAN));
    Assert.assertFalse((boolean) local.evaluate("$x != true()", doc, XPathConstants.BOOLEAN));
    Assert.assertTrue((boolean) local.evaluate("not($x != true())", doc, XPathConstants.BOOLEAN));
    Assert.assertTrue((boolean) local.evaluate("true() = $x", doc, XPathConstants.BOOLEAN));
    Assert.assertFalse((boolean) local.evaluate("not(true() = $x)", doc, XPathConstants.BOOLEAN));
    Assert.assertFalse((boolean) local.evaluate("true() != $x", doc, XPathConstants.BOOLEAN));
    Assert.assertTrue((boolean) local.evaluate("not(true() != $x)", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean86() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean86.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final XPath local = xpathfactory.newXPath();
    final XPathVariableResolver resolver = new XPathVariableResolverBuilder().var("x",
            local.evaluate("doc/avj/none", doc, XPathConstants.NODESET)).build();
    local.setXPathVariableResolver(resolver);
    Assert.assertFalse((boolean) local.evaluate("$x = true()", doc, XPathConstants.BOOLEAN));
    Assert.assertTrue((boolean) local.evaluate("not($x = true())", doc, XPathConstants.BOOLEAN));
    Assert.assertTrue((boolean) local.evaluate("$x != true()", doc, XPathConstants.BOOLEAN));
    Assert.assertFalse((boolean) local.evaluate("not($x != true())", doc, XPathConstants.BOOLEAN));
    Assert.assertFalse((boolean) local.evaluate("true() = $x", doc, XPathConstants.BOOLEAN));
    Assert.assertTrue((boolean) local.evaluate("not(true() = $x)", doc, XPathConstants.BOOLEAN));
    Assert.assertTrue((boolean) local.evaluate("true() != $x", doc, XPathConstants.BOOLEAN));
    Assert.assertFalse((boolean) local.evaluate("not(true() != $x)", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean87() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean87.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final XPath local = xpathfactory.newXPath();
    final XPathVariableResolver resolver = new XPathVariableResolverBuilder()
            .var("normalRTF", local.evaluate("doc/avj", doc, XPathConstants.NODESET))
            .var("emptyRTF", local.evaluate("doc/foo", doc, XPathConstants.NODESET)).build();
    local.setXPathVariableResolver(resolver);
    Assert.assertTrue((boolean) local.evaluate("$normalRTF = true()", doc, XPathConstants.BOOLEAN));
    Assert.assertFalse((boolean) local.evaluate("$normalRTF != true()", doc, XPathConstants.BOOLEAN));
    Assert.assertTrue((boolean) local.evaluate("true() = $normalRTF ", doc, XPathConstants.BOOLEAN));
    Assert.assertFalse((boolean) local.evaluate("true() != $normalRTF", doc, XPathConstants.BOOLEAN));
    Assert.assertFalse((boolean) local.evaluate("$emptyRTF = true()", doc, XPathConstants.BOOLEAN));
    Assert.assertTrue((boolean) local.evaluate("$emptyRTF != true()", doc, XPathConstants.BOOLEAN));
    Assert.assertFalse((boolean) local.evaluate("true() = $emptyRTF", doc, XPathConstants.BOOLEAN));
    Assert.assertTrue((boolean) local.evaluate("true() != $emptyRTF", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean88() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean88.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final XPath local = xpathfactory.newXPath();
    final XPathVariableResolver resolver = new XPathVariableResolverBuilder().var("stringRTF",
            local.evaluate("doc/str", doc, XPathConstants.NODESET)).build();
    local.setXPathVariableResolver(resolver);
    Assert.assertTrue((boolean) local.evaluate("$stringRTF = 'found'", doc, XPathConstants.BOOLEAN));
    Assert.assertFalse((boolean) local.evaluate("$stringRTF != 'found'", doc, XPathConstants.BOOLEAN));
    Assert.assertTrue((boolean) local.evaluate("'found' = $stringRTF", doc, XPathConstants.BOOLEAN));
    Assert.assertFalse((boolean) local.evaluate("'found' != $stringRTF", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean89() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean89.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final XPath local = xpathfactory.newXPath();
    final XPathVariableResolver resolver = new XPathVariableResolverBuilder().var("numericRTF",
            local.evaluate("doc/num", doc, XPathConstants.NODESET)).build();
    local.setXPathVariableResolver(resolver);
    Assert.assertTrue((boolean) local.evaluate("$numericRTF = 17", doc, XPathConstants.BOOLEAN));
    Assert.assertFalse((boolean) local.evaluate("$numericRTF != 17", doc, XPathConstants.BOOLEAN));
    Assert.assertTrue((boolean) local.evaluate("17 = $numericRTF", doc, XPathConstants.BOOLEAN));
    Assert.assertFalse((boolean) local.evaluate("17 != $numericRTF", doc, XPathConstants.BOOLEAN));
  }

  @Test
  public void boolean90() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "boolean90.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertTrue((boolean) xpath.evaluate("boolean('1')", doc, XPathConstants.BOOLEAN));
  }

}
