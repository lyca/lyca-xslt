package de.lyca.xpath.conf;

import static de.lyca.xpath.ResourceUtils.getInputSource;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathVariableResolver;

import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import de.lyca.xpath.NamespaceContextBuilder;
import de.lyca.xpath.XPathVariableResolverBuilder;

public class XPathStringTests {

  private static final String PACKAGE = '/'
          + XPathStringTests.class.getPackage().getName().replace('.', '/').replaceFirst("xpath", "xslt") + "/string/";
  private static XPathFactory xpathfactory;
  private static XPath xpath;

  @BeforeClass
  public static void init() throws Exception {
    xpathfactory = XPathFactory.newInstance();
    xpath = xpathfactory.newXPath();
  }

  @Test
  public void string01() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string01.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("14", xpath.evaluate("string-length('This is a test')", doc));
  }

  @Test
  public void string02() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string02.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("4", xpath.evaluate("string-length(doc)", doc));
  }

  @Test
  public void string04() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string04.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("27", xpath.evaluate("string-length()", doc));
    Assert.assertEquals("12", xpath.evaluate("string-length(doc/a)", doc));
  }

  @Test
  public void string05() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string05.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("Test", xpath.evaluate("string(doc)", doc));
  }

  @Test
  public void string06() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string06.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("true", xpath.evaluate("starts-with('ENCYCLOPEDIA', 'ENCY')", doc));
  }

  @Test
  public void string07() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string07.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("true", xpath.evaluate("contains('ENCYCLOPEDIA', 'CYCL')", doc));
  }

  @Test
  public void string08() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string08.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("1999", xpath.evaluate("substring-before('1999/04/01', '/')", doc));
  }

  @Test
  public void string09() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string09.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("04/01", xpath.evaluate("substring-after('1999/04/01', '/')", doc));
  }

  @Test
  public void string10() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string10.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("ab cd ef", xpath.evaluate("normalize-space('\t\n\r\nab\ncd\t\n\r\nef\t\n\r ')", doc));
    Assert.assertEquals("This is a normalized text node from the source document.",
            xpath.evaluate("normalize-space(doc/a)", doc));
  }

  @Test
  public void string11() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string11.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("BAr", xpath.evaluate("translate('bar','abc','ABC')", doc));
  }

  @Test
  public void string12() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string12.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("xyz", xpath.evaluate("concat('x','yz')", doc));
  }

  @Test
  @Ignore("No format-number in XPath")
  public void string13() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string13.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("1", xpath.evaluate("format-number(1, '#,##0')", doc));
  }

  @Test
  public void string14() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string14.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final XPath local = xpathfactory.newXPath();
    final XPathVariableResolver resolver = new XPathVariableResolverBuilder().var("which",
            local.evaluate("doc/av//*", doc, XPathConstants.NODESET)).build();
    local.setXPathVariableResolver(resolver);
    Assert.assertEquals("\n      b\n      c\n      d\n      e\n    ", local.evaluate("string($which)", doc));
  }

  @Test
  public void string15() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string15.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("1999", xpath.evaluate("substring('1999/04/01', 1, 4)", doc));
  }

  @Test
  public void string16() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string16.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("234", xpath.evaluate("substring('12345', 1.5, 2.6)", doc));
  }

  @Test
  public void string17() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string17.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("12", xpath.evaluate("substring('12345', 0, 3)", doc));
  }

  @Test
  public void string18() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string18.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("", xpath.evaluate("substring('12345', 0 div 0, 3)", doc));
  }

  @Test
  public void string19() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string19.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("", xpath.evaluate("substring('12345', 1, 0 div 0)", doc));
  }

  @Test
  public void string20() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string20.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("12345", xpath.evaluate("substring('12345', -42, 1 div 0)", doc));
  }

  @Test
  public void string21() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string21.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("", xpath.evaluate("substring('12345', -1 div 0, 1 div 0)", doc));
  }

  @Test
  public void string22() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string22.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("", xpath.evaluate("substring(foo, 12, 3)", doc));
  }

  @Test
  public void string30() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string30.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final XPath local = xpathfactory.newXPath();
    local.setNamespaceContext(new NamespaceContextBuilder().add("baz1", "http://xsl.lotus.com/ns1")
            .add("baz2", "http://xsl.lotus.com/ns2").build());
    Assert.assertEquals("", local.evaluate("namespace-uri(baz2:doc/baz1:a/@baz2:attrib1)", doc));
  }

  @Test
  public void string31() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string31.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final XPath local = xpathfactory.newXPath();
    local.setNamespaceContext(new NamespaceContextBuilder().add("baz1", "http://xsl.lotus.com/ns1")
            .add("baz2", "http://xsl.lotus.com/ns2").build());
    Assert.assertEquals("http://xsl.lotus.com/ns1", local.evaluate("namespace-uri(baz2:doc/baz2:b/@baz1:attrib2)", doc));
  }

  @Test
  public void string32() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string32.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final XPath local = xpathfactory.newXPath();
    local.setNamespaceContext(new NamespaceContextBuilder().add("baz1", "http://xsl.lotus.com/ns1")
            .add("baz2", "http://xsl.lotus.com/ns2").build());
    Assert.assertEquals("ns1:a", local.evaluate("name(baz2:doc/*)", doc));
  }

  @Test
  public void string33() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string33.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final XPath local = xpathfactory.newXPath();
    local.setNamespaceContext(new NamespaceContextBuilder().add("baz1", "http://xsl.lotus.com/ns1")
            .add("baz2", "http://xsl.lotus.com/ns2").build());
    Assert.assertEquals("ns1:a", local.evaluate("name(baz2:doc/baz1:a)", doc));
  }

  @Test
  public void string34() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string34.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final XPath local = xpathfactory.newXPath();
    local.setNamespaceContext(new NamespaceContextBuilder().add("baz1", "http://xsl.lotus.com/ns1")
            .add("baz2", "http://xsl.lotus.com/ns2").build());
    Assert.assertEquals("b", local.evaluate("name(baz2:doc/baz2:b)", doc));
  }

  @Test
  public void string35() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string35.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final XPath local = xpathfactory.newXPath();
    local.setNamespaceContext(new NamespaceContextBuilder().add("baz1", "http://xsl.lotus.com/ns1")
            .add("baz2", "http://xsl.lotus.com/ns2").build());
    Assert.assertEquals("", local.evaluate("name(baz2:doc/baz1:a/@baz2:attrib1)", doc));
  }

  @Test
  public void string36() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string36.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final XPath local = xpathfactory.newXPath();
    local.setNamespaceContext(new NamespaceContextBuilder().add("baz1", "http://xsl.lotus.com/ns1")
            .add("baz2", "http://xsl.lotus.com/ns2").build());
    Assert.assertEquals("ns1:attrib2", local.evaluate("name(baz2:doc/baz2:b/@baz1:attrib2)", doc));
  }

  @Test
  public void string37() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string37.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("", xpath.evaluate("string(foo)", doc));
  }

  @Test
  public void string38() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string38.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("0", xpath.evaluate("string(0)", doc));
  }

  @Test
  public void string39() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string39.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("2", xpath.evaluate("string(2)", doc));
  }

  @Test
  public void string40() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string40.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("test", xpath.evaluate("string('test')", doc));
  }

  @Test
  public void string41() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string41.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("", xpath.evaluate("string('')", doc));
  }

  @Test
  public void string42() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string42.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final XPath local = xpathfactory.newXPath();
    final XPathVariableResolver resolver = new XPathVariableResolverBuilder().var("ResultTreeFragTest",
            local.evaluate("doc", doc, XPathConstants.NODESET)).build();
    local.setXPathVariableResolver(resolver);
    Assert.assertEquals("Test", local.evaluate("string($ResultTreeFragTest)", doc));
  }

  @Test
  public void string43() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string43.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final XPath local = xpathfactory.newXPath();
    final XPathVariableResolver resolver = new XPathVariableResolverBuilder().var("emptyResultTreeFragTest",
            local.evaluate("foo", doc, XPathConstants.NODESET)).build();
    local.setXPathVariableResolver(resolver);
    Assert.assertEquals("", local.evaluate("string($emptyResultTreeFragTest)", doc));
  }

  @Test
  public void string44() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string44.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("true", xpath.evaluate("starts-with('ENCYCLOPEDIA', 'EN')", doc));
  }

  @Test
  public void string45() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string45.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("false", xpath.evaluate("starts-with('ENCYCLOPEDIA', 'en')", doc));
  }

  @Test
  public void string46() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string46.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("false", xpath.evaluate("starts-with('ab', 'abc')", doc));
  }

  @Test
  public void string47() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string47.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("false", xpath.evaluate("starts-with('abc', 'bc')", doc));
  }

  @Test
  public void string48() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string48.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("true", xpath.evaluate("starts-with('abc','')", doc));
  }

  @Test
  public void string49() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string49.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("true", xpath.evaluate("starts-with('','')", doc));
  }

  @Test
  public void string50() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string50.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("true", xpath.evaluate("starts-with('true()', 'tr')", doc));
  }

  @Test
  public void string51() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string51.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("true", xpath.evaluate("starts-with(doc, 'ENCY')", doc));
  }

  @Test
  public void string52() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string52.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("false", xpath.evaluate("starts-with(doc, 'test')", doc));
  }

  @Test
  public void string53() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string53.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("true", xpath.evaluate("starts-with(doc/@attr, 'slam')", doc));
  }

  @Test
  public void string54() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string54.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("false", xpath.evaluate("starts-with(doc/@attr, 'wich')", doc));
  }

  @Test
  public void string55() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string55.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("false", xpath.evaluate("contains('ENCYCLOPEDIA', 'TEST')", doc));
  }

  @Test
  public void string56() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string56.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final XPath local = xpathfactory.newXPath();
    final XPathVariableResolver resolver = new XPathVariableResolverBuilder().var("find", "CY")
            .var("node", local.evaluate("doc", doc, XPathConstants.NODESET)).build();
    local.setXPathVariableResolver(resolver);
    Assert.assertEquals("true", local.evaluate("contains($node,$find)", doc));
  }

  @Test
  public void string57() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string57.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("true", xpath.evaluate("contains(concat(.,'BC'),concat('A','B','C'))", doc));
  }

  @Test
  public void string58() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string58.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("false", xpath.evaluate("contains('ab', 'abc')", doc));
  }

  @Test
  public void string59() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string59.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("true", xpath.evaluate("contains('abc', 'bc')", doc));
  }

  @Test
  public void string60() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string60.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("false", xpath.evaluate("contains('abc', 'bcd')", doc));
  }

  @Test
  public void string61() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string61.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("true", xpath.evaluate("contains('abc','')", doc));
  }

  @Test
  public void string62() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string62.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("true", xpath.evaluate("contains('','')", doc));
  }

  @Test
  public void string63() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string63.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("true", xpath.evaluate("contains('true()', 'e')", doc));
  }

  @Test
  public void string64() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string64.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("true", xpath.evaluate("contains(doc, 'CYCL')", doc));
  }

  @Test
  public void string65() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string65.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("false", xpath.evaluate("contains(doc, 'TEST')", doc));
  }

  @Test
  public void string66() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string66.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("true", xpath.evaluate("contains(doc/@attr, 'amwi')", doc));
  }

  @Test
  public void string67() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string67.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("false", xpath.evaluate("contains(doc/@attr, 'TEST')", doc));
  }

  @Test
  public void string68() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string68.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("", xpath.evaluate("substring-before('ENCYCLOPEDIA', '/')", doc));
  }

  @Test
  public void string69() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string69.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("EN", xpath.evaluate("substring-before('ENCYCLOPEDIA', 'C')", doc));
  }

  @Test
  public void string70() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string70.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("", xpath.evaluate("substring-before('ENCYCLOPEDIA', 'c')", doc));
  }

  @Test
  public void string71() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string71.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("1999", xpath.evaluate("substring-before(doc, '/')", doc));
  }

  @Test
  public void string72() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string72.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("", xpath.evaluate("substring-before(foo, '/')", doc));
  }

  @Test
  public void string73() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string73.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("", xpath.evaluate("substring-before(doc/@attr, 'z')", doc));
  }

  @Test
  public void string74() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string74.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("", xpath.evaluate("substring-before(doc/@attr, 'D')", doc));
  }

  @Test
  public void string75() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string75.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("abc", xpath.evaluate("substring-before(doc/@attr, 'd')", doc));
  }

  @Test
  public void string76() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string76.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("", xpath.evaluate("substring-after('ENCYCLOPEDIA', '/')", doc));
  }

  @Test
  public void string77() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string77.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("YCLOPEDIA", xpath.evaluate("substring-after('ENCYCLOPEDIA', 'C')", doc));
  }

  @Test
  public void string78() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string78.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("", xpath.evaluate("substring-after('abcdefghijk','l')", doc));
  }

  @Test
  public void string79() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string79.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("999/04/01", xpath.evaluate("substring-after('1999/04/01', '1')", doc));
  }

  @Test
  public void string80() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string80.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("04/01", xpath.evaluate("substring-after(doc, '/')", doc));
  }

  @Test
  public void string81() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string81.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("", xpath.evaluate("substring-after(foo, '/')", doc));
  }

  @Test
  public void string82() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string82.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("", xpath.evaluate("substring-after(doc/@attr, 'z')", doc));
  }

  @Test
  public void string83() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string83.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("", xpath.evaluate("substring-after(doc/@attr, 'D')", doc));
  }

  @Test
  public void string84() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string84.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("efg", xpath.evaluate("substring-after(doc/@attr, 'd')", doc));
  }

  @Test
  public void string85() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string85.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final XPath local = xpathfactory.newXPath();
    final XPathVariableResolver resolver = new XPathVariableResolverBuilder().var("thisvalue",
            "This       is       a       test").build();
    local.setXPathVariableResolver(resolver);
    Assert.assertEquals("This is a normalized string.", local.evaluate("normalize-space(doc/a)", doc));
    Assert.assertEquals("This is a test", local.evaluate("normalize-space($thisvalue)", doc));
  }

  @Test
  public void string86() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string86.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("This is a normalized string.", xpath.evaluate("normalize-space()", doc));
  }

  @Test
  public void string87() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string87.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("BAR", xpath.evaluate("translate('BAR','abc','ABC')", doc));
  }

  @Test
  public void string88() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string88.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("bar", xpath.evaluate("translate('bar','RAB','xyz')", doc));
  }

  @Test
  public void string89() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string89.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("BAT", xpath.evaluate("translate('BAR','Rab','TxX')", doc));
    Assert.assertEquals("B`B", xpath.evaluate("translate(\"B'B\",\"'\",'`')", doc));
  }

  @Test
  public void string90() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string90.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("BAr", xpath.evaluate("translate(doc/a,'abc','ABC')", doc));
  }

  @Test
  public void string91() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string91.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("baR", xpath.evaluate("translate(doc/b,'ABC','abc')", doc));
  }

  @Test
  public void string92() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string92.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("heIIT", xpath.evaluate("translate(doc/a/@attrib,'lo','IT')", doc));
  }

  @Test
  public void string93() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string93.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("HEiit", xpath.evaluate("translate(doc/b/@attrib,'LO','it')", doc));
  }

  @Test
  public void string94() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string94.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("HELLO", xpath.evaluate("translate(doc/b/@attrib,'lo','it')", doc));
  }

  @Test
  public void string95() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string95.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("AAA", xpath.evaluate("translate('zzaaazzz','abcz','ABC')", doc));
  }

  @Test
  public void string96() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string96.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("xxAAAxxxx", xpath.evaluate("translate('ddaaadddd','abcd','ABCxy')", doc));
  }

  @Test
  public void string97() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string97.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("abcdef", xpath.evaluate("concat('a','b','c','d','ef')", doc));
  }

  @Test
  public void string98() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string98.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("ab", xpath.evaluate("concat(doc/a, doc/b)", doc));
  }

  @Test
  public void string99() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string99.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("abcdef", xpath.evaluate("concat(doc/a, doc/b, doc/c, doc/d, doc/e)", doc));
  }

  @Test
  public void string100() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string100.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("cd34", xpath.evaluate("concat('cd', '34')", doc));
  }

  @Test
  public void string101() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string101.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("cd34", xpath.evaluate("concat('cd', 34)", doc));
  }

  @Test
  public void string102() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string102.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("bc23", xpath.evaluate("concat('bc', string(23))", doc));
  }

  @Test
  public void string103() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string103.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("a34", xpath.evaluate("concat(doc/a, 34)", doc));
  }

  @Test
  public void string104() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string104.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("falsely", xpath.evaluate("concat(false(),'ly')", doc));
  }

  @Test
  @Ignore
  public void string105() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string105.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("", xpath.evaluate("concat(doc/*, doc/*[@attr='whatsup'])", doc));
  }

  @Test
  @Ignore("No format-number in XPath")
  public void string106() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string106.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("1000", xpath.evaluate("format-number(1000, '###0')", doc));
  }

  @Test
  public void string107() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string107.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final XPath local = xpathfactory.newXPath();
    final XPathVariableResolver resolver = new XPathVariableResolverBuilder().var("ResultTreeFragTest",
            local.evaluate("doc/av", doc, XPathConstants.NODESET)).build();
    local.setXPathVariableResolver(resolver);
    Assert.assertEquals(
            "\n    \n      b\n      c\n      d\n      e\n    \n    \n      w\n      x\n      y\n      z\n    \n  ",
            local.evaluate("string($ResultTreeFragTest)", doc));
  }

  @Test
  @Ignore("No format-number in XPath")
  public void string108() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string108.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("01", xpath.evaluate("format-number(1, '00')", doc));
  }

  @Test
  @Ignore("No format-number in XPath")
  public void string109() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string109.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("01.0", xpath.evaluate("format-number(1, '00.0')", doc));
  }

  @Test
  @Ignore("No format-number in XPath")
  public void string110() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string110.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("25%", xpath.evaluate("format-number(0.25, '00%')", doc));
  }

  @Test
  public void string111() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string111.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("PED", xpath.evaluate("substring('ENCYCLOPEDIA', 8, 3)", doc));
  }

  @Test
  public void string112() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string112.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("PEDIA", xpath.evaluate("substring('ENCYCLOPEDIA', 8)", doc));
  }

  @Test
  public void string113() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string113.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("", xpath.evaluate("substring('abcdefghijk',0 div 0, 5)", doc));
  }

  @Test
  public void string114() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string114.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("defghi", xpath.evaluate("substring('abcdefghijk',4, 6)", doc));
  }

  @Test
  public void string115() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string115.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("", xpath.evaluate("substring('1999/04/01', 1, 0)", doc));
  }

  @Test
  public void string116() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string116.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("1999", xpath.evaluate("substring(doc, 1, 4)", doc));
  }

  @Test
  public void string117() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string117.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("", xpath.evaluate("substring(foo, 2, 2)", doc));
  }

  @Test
  public void string118() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string118.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("abc", xpath.evaluate("substring(doc/@attr, 1, 3)", doc));
  }

  @Test
  public void string119() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string119.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("defg", xpath.evaluate("substring(doc/@attr, 4)", doc));
  }

  @Test
  public void string120() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string120.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("cdef", xpath.evaluate("substring(doc/@attr, 2.5, 3.6)", doc));
  }

  @Test
  public void string121() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string121.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final XPath local = xpathfactory.newXPath();
    final XPathVariableResolver resolver = new XPathVariableResolverBuilder().var("s",
            local.evaluate("doc/a", doc, XPathConstants.NODESET)).build();
    local.setXPathVariableResolver(resolver);
    Assert.assertEquals("bar_fly", local.evaluate("translate(normalize-space($s), ' ', '_')", doc));
  }

  @Test
  public void string122() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string122.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("\nb\nc\nd\ne", xpath.evaluate("string(doc/av//*)", doc));
  }

  @Test
  public void string123() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string123.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("TestingString", xpath.evaluate("string()", doc));
  }

  @Test
  @Ignore
  public void string124() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string124.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("", xpath.evaluate("contains(doc/text(), 'SYMBOL 180 \f \"')", doc));
  }

  @Test
  public void string125() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string125.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("false", xpath.evaluate("contains(doc/main,doc/sub)", doc));
  }

  @Test
  public void string126() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string126.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("true", xpath.evaluate("contains(doc/main,doc/sub)", doc));
  }

  @Test
  public void string127() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string127.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("false", xpath.evaluate("contains(doc/main,doc/sub)", doc));
  }

  @Test
  public void string128() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string128.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("false", xpath.evaluate("contains(doc/main,doc/sub)", doc));
  }

  @Test
  public void string129() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string129.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("true", xpath.evaluate("contains(doc,'&')", doc));
  }

  @Test
  public void string130() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string130.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("true", xpath.evaluate("contains(doc, '\u00B0')", doc));
  }

  @Test
  public void string131() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string131.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("17", xpath.evaluate("string()", doc));
  }

  @Test
  @Ignore
  public void string132() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string132.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("", xpath.evaluate("", doc));
  }

  @Test
  @Ignore
  public void string133() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string133.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("", xpath.evaluate("", doc));
  }

  @Test
  @Ignore
  public void string134() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string134.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("", xpath.evaluate("", doc));
  }

  @Test
  @Ignore
  public void string135() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string135.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("", xpath.evaluate("", doc));
  }

  @Test
  public void string136() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string136.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("abrtor", xpath.evaluate("translate(doc/a,'abe','bao')", doc));
  }

  @Test
  public void string137() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string137.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("b*rb*r*t*", xpath.evaluate("translate(doc/a,'aeiouy','******')", doc));
  }

  @Test
  public void string138() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string138.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final XPath local = xpathfactory.newXPath();
    final XPathVariableResolver resolver = new XPathVariableResolverBuilder().var("pt1", "aqu'").var("pt2", "\"eos")
            .build();
    local.setXPathVariableResolver(resolver);
    Assert.assertEquals("QUA-lit+y", local.evaluate("translate(doc/a,concat($pt1,$pt2),'AQU-+EOS')", doc));
  }

  @Test
  public void string139() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string139.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final XPath local = xpathfactory.newXPath();
    final XPathVariableResolver resolver = new XPathVariableResolverBuilder().var("pt1", "aqu'").var("pt2", "\"eos")
            .build();
    local.setXPathVariableResolver(resolver);
    Assert.assertEquals("quan\"ti'ty", local.evaluate("translate(doc/a,'AQU-+EOS',concat($pt1,$pt2))", doc));
  }

  @Test
  @Ignore
  public void string140() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string140.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("", xpath.evaluate("", doc));
  }

  @Test
  public void string141() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string141.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals(
            "t1t2t3t4t5t6t7t8t9t10t11t12t13t14t15t16t17t18t19t20t21t22t23t24t25t26t27t28t29t30t31t32t33t34t35t36t37t38t39t40t41t42t43t44t45"
                    + "t46t47t48t49t50t51t52t53t54t55t56t57t58t59t60t61t62t63t64t65t66t67t68t69t70t71t72t73t74t75t76t77t78t79t80t81t82t83t84t85t86"
                    + "t87t88t89t90t91t92t93t94t95t96t97t98t99t100t101t102t103t104t105t106t107t108t109t110t111t112t113t114t115t116t117t118t119t120"
                    + "t121t122t123t124t125t126t127t128t129t130t131t132t133t134t135t136t137t138t139t140t141t142t143t144t145t146t147t148t149t150t151"
                    + "t152t153t154t155t156t157t158t159t160t161t162t163t164t165t166t167t168t169t170t171t172t173t174t175t176t177t178t179t180t181t182"
                    + "t183t184t185t186t187t188t189t190t191t192t193t194t195t196t197t198t199t200t201t202t203t204t205t206t207t208t209t210t211t212t213"
                    + "t214t215t216t217t218t219t220t221t222t223t224t225t226t227t228t229t230t231t232t233t234t235t236t237t238t239t240t241t242t243t244"
                    + "t245t246t247t248t249t250t251t252t253t254t255t256t257t258t259t260t261t262t263t264t265t266t267t268t269t270t271t272t273t274t275"
                    + "t276t277t278t279t280t281t282t283t284t285t286t287t288t289t290t291t292t293t294t295t296t297t298t299t300t301t302t303t304t305t306"
                    + "t307t308t309t310t311t312t313t314t315t316t317t318t319t320t321t322t323t324t325t326t327t328t329t330t331t332t333t334t335t336t337"
                    + "t338t339t340t341t342t343t344t345t346t347t348t349t350t351t352t353t354t355t356t357t358t359t360t361t362t363t364t365t366t367t368"
                    + "t369t370t371t372t373t374t375t376t377t378t379t380t381t382t383t384t385t386t387t388t389t390t391t392t393t394t395t396t397t398t399"
                    + "t400t401t402t403t404t405t406t407t408t409t410t411t412t413t414t415t416t417t418t419t420t421t422t423t424t425t426t427t428t429t430"
                    + "t431t432t433t434t435t436t437t438t439t440t441t442t443t444t445t446t447t448t449t450t451t452t453t454t455t456t457t458t459t460t461"
                    + "t462t463t464t465t466t467t468t469t470t471t472t473t474t475t476t477t478t479t480t481t482t483t484t485t486t487t488t489t490t491t492"
                    + "t493t494t495t496t497t498t499t500t501t502t503t504t505t506t507t508t509t510t511t512t513t514t515t516t517t518t519t520t521t522t523"
                    + "t524t525t526t527t528t529t530t531t532t533t534t535t536t537t538t539t540t541t542t543t544t545t546t547t548t549t550t551t552t553t554"
                    + "t555t556t557t558t559t560t561t562t563t564t565t566t567t568t569t570t571t572t573t574t575t576t577t578t579t580t581t582t583t584t585"
                    + "t586t587t588t589t590t591t592t593t594t595t596t597t598t599t600t601t602t603t604t605t606t607t608t609t610t611t612t613t614t615t616"
                    + "t617t618t619t620t621t622t623t624t625t626t627t628t629t630t631t632t633t634t635t636t637t638t639t640t641t642t643t644t645t646t647"
                    + "t648t649t650t651t652t653t654t655t656t657t658t659t660t661t662t663t664t665t666t667t668t669t670t671t672t673t674t675t676t677t678"
                    + "t679t680t681t682t683t684t685t686t687t688t689t690t691t692t693t694t695t696t697t698t699t700t701t702t703t704t705t706t707t708t709"
                    + "t710t711t712t713t714t715t716t717t718t719t720t721t722t723t724t725t726t727t728t729t730t731t732t733t734t735t736t737t738t739t740"
                    + "t741t742t743t744t745t746t747t748t749t750t751t752t753t754t755t756t757t758t759t760t761t762t763t764t765t766t767t768t769t770t771"
                    + "t772t773t774t775t776t777t778t779t780t781t782t783t784t785t786t787t788t789t790t791t792t793t794t795t796t797t798t799t800t801t802"
                    + "t803t804t805t806t807t808t809t810t811t812t813t814t815t816t817t818t819t820t821t822t823t824t825t826t827t828t829t830t831t832t833"
                    + "t834t835t836t837t838t839t840t841t842t843t844t845t846t847t848t849t850t851t852t853t854t855t856t857t858t859t860t861t862t863t864"
                    + "t865t866t867t868t869t870t871t872t873t874t875t876t877t878t879t880t881t882t883t884t885t886t887t888t889t890t891t892t893t894t895"
                    + "t896t897t898t899t900t901t902t903t904t905t906t907t908t909t910t911t912t913t914t915t916t917t918t919t920t921t922t923t924t925t926"
                    + "t927t928t929t930t931t932t933t934t935t936t937t938t939t940t941t942t943t944t945t946t947t948t949t950t951t952t953t954t955t956t957"
                    + "t958t959t960t961t962t963t964t965t966t967t968t969t970t971t972t973t974t975t976t977t978t979t980t981t982t983t984t985t986t987t988"
                    + "t989t990t991t992t993t994t995t996t997t998t999t1000",
            xpath.evaluate(
                    "concat('t1','t2','t3','t4','t5','t6','t7','t8','t9','t10',"
                            + "'t11','t12','t13','t14','t15','t16','t17','t18','t19','t20','t21','t22','t23','t24','t25','t26','t27','t28','t29','t30',"
                            + "'t31','t32','t33','t34','t35','t36','t37','t38','t39','t40','t41','t42','t43','t44','t45','t46','t47','t48','t49','t50',"
                            + "'t51','t52','t53','t54','t55','t56','t57','t58','t59','t60','t61','t62','t63','t64','t65','t66','t67','t68','t69','t70',"
                            + "'t71','t72','t73','t74','t75','t76','t77','t78','t79','t80','t81','t82','t83','t84','t85','t86','t87','t88','t89','t90',"
                            + "'t91','t92','t93','t94','t95','t96','t97','t98','t99','t100',"
                            + "'t101','t102','t103','t104','t105','t106','t107','t108','t109','t110',"
                            + "'t111','t112','t113','t114','t115','t116','t117','t118','t119','t120',"
                            + "'t121','t122','t123','t124','t125','t126','t127','t128','t129','t130',"
                            + "'t131','t132','t133','t134','t135','t136','t137','t138','t139','t140',"
                            + "'t141','t142','t143','t144','t145','t146','t147','t148','t149','t150',"
                            + "'t151','t152','t153','t154','t155','t156','t157','t158','t159','t160',"
                            + "'t161','t162','t163','t164','t165','t166','t167','t168','t169','t170',"
                            + "'t171','t172','t173','t174','t175','t176','t177','t178','t179','t180',"
                            + "'t181','t182','t183','t184','t185','t186','t187','t188','t189','t190',"
                            + "'t191','t192','t193','t194','t195','t196','t197','t198','t199','t200',"
                            + "'t201','t202','t203','t204','t205','t206','t207','t208','t209','t210',"
                            + "'t211','t212','t213','t214','t215','t216','t217','t218','t219','t220',"
                            + "'t221','t222','t223','t224','t225','t226','t227','t228','t229','t230',"
                            + "'t231','t232','t233','t234','t235','t236','t237','t238','t239','t240',"
                            + "'t241','t242','t243','t244','t245','t246','t247','t248','t249','t250',"
                            + "'t251','t252','t253','t254','t255','t256','t257','t258','t259','t260',"
                            + "'t261','t262','t263','t264','t265','t266','t267','t268','t269','t270',"
                            + "'t271','t272','t273','t274','t275','t276','t277','t278','t279','t280',"
                            + "'t281','t282','t283','t284','t285','t286','t287','t288','t289','t290',"
                            + "'t291','t292','t293','t294','t295','t296','t297','t298','t299','t300',"
                            + "'t301','t302','t303','t304','t305','t306','t307','t308','t309','t310',"
                            + "'t311','t312','t313','t314','t315','t316','t317','t318','t319','t320',"
                            + "'t321','t322','t323','t324','t325','t326','t327','t328','t329','t330',"
                            + "'t331','t332','t333','t334','t335','t336','t337','t338','t339','t340',"
                            + "'t341','t342','t343','t344','t345','t346','t347','t348','t349','t350',"
                            + "'t351','t352','t353','t354','t355','t356','t357','t358','t359','t360',"
                            + "'t361','t362','t363','t364','t365','t366','t367','t368','t369','t370',"
                            + "'t371','t372','t373','t374','t375','t376','t377','t378','t379','t380',"
                            + "'t381','t382','t383','t384','t385','t386','t387','t388','t389','t390',"
                            + "'t391','t392','t393','t394','t395','t396','t397','t398','t399','t400',"
                            + "'t401','t402','t403','t404','t405','t406','t407','t408','t409','t410',"
                            + "'t411','t412','t413','t414','t415','t416','t417','t418','t419','t420',"
                            + "'t421','t422','t423','t424','t425','t426','t427','t428','t429','t430',"
                            + "'t431','t432','t433','t434','t435','t436','t437','t438','t439','t440',"
                            + "'t441','t442','t443','t444','t445','t446','t447','t448','t449','t450',"
                            + "'t451','t452','t453','t454','t455','t456','t457','t458','t459','t460',"
                            + "'t461','t462','t463','t464','t465','t466','t467','t468','t469','t470',"
                            + "'t471','t472','t473','t474','t475','t476','t477','t478','t479','t480',"
                            + "'t481','t482','t483','t484','t485','t486','t487','t488','t489','t490',"
                            + "'t491','t492','t493','t494','t495','t496','t497','t498','t499','t500',"
                            + "'t501','t502','t503','t504','t505','t506','t507','t508','t509','t510',"
                            + "'t511','t512','t513','t514','t515','t516','t517','t518','t519','t520',"
                            + "'t521','t522','t523','t524','t525','t526','t527','t528','t529','t530',"
                            + "'t531','t532','t533','t534','t535','t536','t537','t538','t539','t540',"
                            + "'t541','t542','t543','t544','t545','t546','t547','t548','t549','t550',"
                            + "'t551','t552','t553','t554','t555','t556','t557','t558','t559','t560',"
                            + "'t561','t562','t563','t564','t565','t566','t567','t568','t569','t570',"
                            + "'t571','t572','t573','t574','t575','t576','t577','t578','t579','t580',"
                            + "'t581','t582','t583','t584','t585','t586','t587','t588','t589','t590',"
                            + "'t591','t592','t593','t594','t595','t596','t597','t598','t599','t600',"
                            + "'t601','t602','t603','t604','t605','t606','t607','t608','t609','t610',"
                            + "'t611','t612','t613','t614','t615','t616','t617','t618','t619','t620',"
                            + "'t621','t622','t623','t624','t625','t626','t627','t628','t629','t630',"
                            + "'t631','t632','t633','t634','t635','t636','t637','t638','t639','t640',"
                            + "'t641','t642','t643','t644','t645','t646','t647','t648','t649','t650',"
                            + "'t651','t652','t653','t654','t655','t656','t657','t658','t659','t660',"
                            + "'t661','t662','t663','t664','t665','t666','t667','t668','t669','t670',"
                            + "'t671','t672','t673','t674','t675','t676','t677','t678','t679','t680',"
                            + "'t681','t682','t683','t684','t685','t686','t687','t688','t689','t690',"
                            + "'t691','t692','t693','t694','t695','t696','t697','t698','t699','t700',"
                            + "'t701','t702','t703','t704','t705','t706','t707','t708','t709','t710',"
                            + "'t711','t712','t713','t714','t715','t716','t717','t718','t719','t720',"
                            + "'t721','t722','t723','t724','t725','t726','t727','t728','t729','t730',"
                            + "'t731','t732','t733','t734','t735','t736','t737','t738','t739','t740',"
                            + "'t741','t742','t743','t744','t745','t746','t747','t748','t749','t750',"
                            + "'t751','t752','t753','t754','t755','t756','t757','t758','t759','t760',"
                            + "'t761','t762','t763','t764','t765','t766','t767','t768','t769','t770',"
                            + "'t771','t772','t773','t774','t775','t776','t777','t778','t779','t780',"
                            + "'t781','t782','t783','t784','t785','t786','t787','t788','t789','t790',"
                            + "'t791','t792','t793','t794','t795','t796','t797','t798','t799','t800',"
                            + "'t801','t802','t803','t804','t805','t806','t807','t808','t809','t810',"
                            + "'t811','t812','t813','t814','t815','t816','t817','t818','t819','t820',"
                            + "'t821','t822','t823','t824','t825','t826','t827','t828','t829','t830',"
                            + "'t831','t832','t833','t834','t835','t836','t837','t838','t839','t840',"
                            + "'t841','t842','t843','t844','t845','t846','t847','t848','t849','t850',"
                            + "'t851','t852','t853','t854','t855','t856','t857','t858','t859','t860',"
                            + "'t861','t862','t863','t864','t865','t866','t867','t868','t869','t870',"
                            + "'t871','t872','t873','t874','t875','t876','t877','t878','t879','t880',"
                            + "'t881','t882','t883','t884','t885','t886','t887','t888','t889','t890',"
                            + "'t891','t892','t893','t894','t895','t896','t897','t898','t899','t900',"
                            + "'t901','t902','t903','t904','t905','t906','t907','t908','t909','t910',"
                            + "'t911','t912','t913','t914','t915','t916','t917','t918','t919','t920',"
                            + "'t921','t922','t923','t924','t925','t926','t927','t928','t929','t930',"
                            + "'t931','t932','t933','t934','t935','t936','t937','t938','t939','t940',"
                            + "'t941','t942','t943','t944','t945','t946','t947','t948','t949','t950',"
                            + "'t951','t952','t953','t954','t955','t956','t957','t958','t959','t960',"
                            + "'t961','t962','t963','t964','t965','t966','t967','t968','t969','t970',"
                            + "'t971','t972','t973','t974','t975','t976','t977','t978','t979','t980',"
                            + "'t981','t982','t983','t984','t985','t986','t987','t988','t989','t990',"
                            + "'t991','t992','t993','t994','t995','t996','t997','t998','t999','t1000'" + ")", doc));
  }

  @Test
  public void string142() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string142.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("0", xpath.evaluate("string-length(substring-before('abcde',''))", doc));
  }

  @Test
  public void string143() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "string143.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals("ABCDE", xpath.evaluate("substring-after('ABCDE','')", doc));
  }

}
