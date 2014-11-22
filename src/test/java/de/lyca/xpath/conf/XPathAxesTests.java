package de.lyca.xpath.conf;

import static de.lyca.xpath.ResourceUtils.getInputSource;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import de.lyca.xpath.NamespaceContextBuilder;

public class XPathAxesTests {

  private static final String PACKAGE = '/'
          + XPathAxesTests.class.getPackage().getName().replace('.', '/').replaceFirst("xpath", "xslt") + "/axes/";
  private static XPathFactory xpathfactory;
  private static XPath xpath;

  private static void generateAsserts(NodeList nodeList) {
    System.out.println("Assert.assertEquals(" + nodeList.getLength() + ", nodeList.getLength());");
    for (int i = 0, length = nodeList.getLength(); i < length; i++) {
      final Node node = nodeList.item(i);
      System.out
              .println("Assert.assertEquals(\"" + node.getNodeName() + "\", nodeList.item(" + i + ").getNodeName());");
    }
    System.out.println();
    for (int i = 0, length = nodeList.getLength(); i < length; i++) {
      final Node node = nodeList.item(i);
      System.out.println("Assert.assertEquals(\"" + node.getNodeValue() + "\", nodeList.item(" + i
              + ").getNodeValue());");
    }
    Assert.fail("Replace 'generateAsserts(nodeList)'!");
  }

  @BeforeClass
  public static void init() throws Exception {
    xpathfactory = XPathFactory.newInstance();
    xpath = xpathfactory.newXPath();
  }

  @Test
  public void axes01() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes01.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/ancestor::*", doc, XPathConstants.NODESET);
    Assert.assertEquals(3, nodeList.getLength());
    Assert.assertEquals("far-north", nodeList.item(0).getNodeName());
    Assert.assertEquals("north", nodeList.item(1).getNodeName());
    Assert.assertEquals("near-north", nodeList.item(2).getNodeName());
  }

  @Test
  public void axes02() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes02.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/ancestor-or-self::*", doc, XPathConstants.NODESET);
    Assert.assertEquals(4, nodeList.getLength());
    Assert.assertEquals("far-north", nodeList.item(0).getNodeName());
    Assert.assertEquals("north", nodeList.item(1).getNodeName());
    Assert.assertEquals("near-north", nodeList.item(2).getNodeName());
    Assert.assertEquals("center", nodeList.item(3).getNodeName());
  }

  @Test
  public void axes03() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes03.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/attribute::*", doc, XPathConstants.NODESET);
    Assert.assertEquals(3, nodeList.getLength());
    Assert.assertEquals("center-attr-1", nodeList.item(0).getNodeName());
    Assert.assertEquals("center-attr-2", nodeList.item(1).getNodeName());
    Assert.assertEquals("center-attr-3", nodeList.item(2).getNodeName());
  }

  @Test
  public void axes04() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes04.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/child::*", doc, XPathConstants.NODESET);
    Assert.assertEquals(3, nodeList.getLength());
    Assert.assertEquals("near-south-east", nodeList.item(0).getNodeName());
    Assert.assertEquals("near-south", nodeList.item(1).getNodeName());
    Assert.assertEquals("near-south-west", nodeList.item(2).getNodeName());
  }

  @Test
  public void axes05() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes05.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/descendant::*", doc, XPathConstants.NODESET);
    Assert.assertEquals(5, nodeList.getLength());
    Assert.assertEquals("near-south-east", nodeList.item(0).getNodeName());
    Assert.assertEquals("near-south", nodeList.item(1).getNodeName());
    Assert.assertEquals("south", nodeList.item(2).getNodeName());
    Assert.assertEquals("far-south", nodeList.item(3).getNodeName());
    Assert.assertEquals("near-south-west", nodeList.item(4).getNodeName());
  }

  @Test
  public void axes06() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes06.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/descendant-or-self::*", doc, XPathConstants.NODESET);
    Assert.assertEquals(6, nodeList.getLength());
    Assert.assertEquals("center", nodeList.item(0).getNodeName());
    Assert.assertEquals("near-south-east", nodeList.item(1).getNodeName());
    Assert.assertEquals("near-south", nodeList.item(2).getNodeName());
    Assert.assertEquals("south", nodeList.item(3).getNodeName());
    Assert.assertEquals("far-south", nodeList.item(4).getNodeName());
    Assert.assertEquals("near-south-west", nodeList.item(5).getNodeName());
  }

  @Test
  public void axes07() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes07.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/following::*", doc, XPathConstants.NODESET);
    Assert.assertEquals(5, nodeList.getLength());
    Assert.assertEquals("near-east", nodeList.item(0).getNodeName());
    Assert.assertEquals("east", nodeList.item(1).getNodeName());
    Assert.assertEquals("far-east", nodeList.item(2).getNodeName());
    Assert.assertEquals("way-out-yonder-east", nodeList.item(3).getNodeName());
    Assert.assertEquals("out-yonder-east", nodeList.item(4).getNodeName());
  }

  @Test
  public void axes08() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes08.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/preceding::*", doc, XPathConstants.NODESET);
    Assert.assertEquals(5, nodeList.getLength());
    Assert.assertEquals("way-out-yonder-west", nodeList.item(0).getNodeName());
    Assert.assertEquals("out-yonder-west", nodeList.item(1).getNodeName());
    Assert.assertEquals("far-west", nodeList.item(2).getNodeName());
    Assert.assertEquals("west", nodeList.item(3).getNodeName());
    Assert.assertEquals("near-west", nodeList.item(4).getNodeName());
  }

  @Test
  public void axes09() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes09.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/following-sibling::*", doc, XPathConstants.NODESET);
    Assert.assertEquals(3, nodeList.getLength());
    Assert.assertEquals("near-east", nodeList.item(0).getNodeName());
    Assert.assertEquals("east", nodeList.item(1).getNodeName());
    Assert.assertEquals("far-east", nodeList.item(2).getNodeName());
  }

  @Test
  public void axes10() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes10.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/preceding-sibling::*", doc, XPathConstants.NODESET);
    Assert.assertEquals(3, nodeList.getLength());
    Assert.assertEquals("far-west", nodeList.item(0).getNodeName());
    Assert.assertEquals("west", nodeList.item(1).getNodeName());
    Assert.assertEquals("near-west", nodeList.item(2).getNodeName());
  }

  @Test
  public void axes11() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes11.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/parent::*", doc, XPathConstants.NODESET);
    Assert.assertEquals(1, nodeList.getLength());
    Assert.assertEquals("near-north", nodeList.item(0).getNodeName());
  }

  @Test
  public void axes12() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes12.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/self::*", doc, XPathConstants.NODESET);
    Assert.assertEquals(1, nodeList.getLength());
    Assert.assertEquals("center", nodeList.item(0).getNodeName());
  }

  @Test
  public void axes13() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes13.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final String format = "%s/%s/%s/%s";
    final String test0 = xpath.evaluate("//text()[ancestor::*[@new='true'][not(text())]]/.", doc);
    final String test1 = xpath.evaluate("name(//text()[ancestor::*[@new='true'][not(text())]]/ancestor::*[1])", doc);
    final String test2 = xpath.evaluate("name(//text()[ancestor::*[@new='true'][not(text())]]/ancestor::*[2])", doc);
    final String test3 = xpath.evaluate("name(//text()[ancestor::*[@new='true'][not(text())]]/ancestor::*[3])", doc);

    final String test4 = xpath.evaluate("//text()[ancestor::*[2][@new='true'][text()]]/.", doc);
    final String test5 = xpath.evaluate("name(//text()[ancestor::*[2][@new='true'][text()]]/ancestor::*[1])", doc);
    final String test6 = xpath.evaluate("name(//text()[ancestor::*[2][@new='true'][text()]]/ancestor::*[2])", doc);
    final String test7 = xpath.evaluate("name(//text()[ancestor::*[2][@new='true'][text()]]/ancestor::*[3])", doc);

    final String test8 = xpath.evaluate("//text()[ancestor::*[2][@new='false'][text()]]/.", doc);
    final String test9 = xpath.evaluate("name(//text()[ancestor::*[2][@new='false'][text()]]/ancestor::*[1])", doc);
    final String test10 = xpath.evaluate("name(//text()[ancestor::*[2][@new='false'][text()]]/ancestor::*[2])", doc);
    final String test11 = xpath.evaluate("name(//text()[ancestor::*[2][@new='false'][text()]]/ancestor::*[3])", doc);

    final String test12 = xpath.evaluate("//text()[ancestor::*[2][@new='false'][not(text())]]/.", doc);
    final String test13 = xpath.evaluate("name(//text()[ancestor::*[2][@new='false'][not(text())]]/ancestor::*[1])",
            doc);
    final String test14 = xpath.evaluate("name(//text()[ancestor::*[2][@new='false'][not(text())]]/ancestor::*[2])",
            doc);
    final String test15 = xpath.evaluate("name(//text()[ancestor::*[2][@new='false'][not(text())]]/ancestor::*[3])",
            doc);

    Assert.assertEquals("doc/foo/baz/is new", String.format(format, test3, test2, test1, test0));
    Assert.assertEquals("doc/foo/baz/is new but has text", String.format(format, test7, test6, test5, test4));
    Assert.assertEquals("///", String.format(format, test11, test10, test9, test8));
    Assert.assertEquals("doc/foo/baz/is not new", String.format(format, test15, test14, test13, test12));
  }

  @Test
  public void axes14() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes14.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final String test0 = xpath.evaluate("//baz/ancestor-or-self::*[@att1][1]/@att1", doc);
    final String test1 = xpath.evaluate("(//baz/ancestor-or-self::*)[@att1][1]/@att1", doc);
    Assert.assertEquals("a", test0);
    Assert.assertEquals("c", test1);
  }

  @Test
  public void axes15() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes15.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final String[] nodeNames = new String[] { "root", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m",
            "n", "o", "p" };
    final String[] nodeSelects = new String[] { "", "//a/", "//b/", "//c/", "//d/", "//e/", "//f/", "//g/", "//h/",
            "//i/", "//j/", "//k/", "//l/", "//m/", "//n/", "//o/", "//p/" };
    final int[] ancestors = new int[] { 0, 0, 1, 2, 2, 3, 3, 2, 3, 4, 4, 1, 2, 3, 3, 2, 2 };
    final int[] precedings = new int[] { 0, 0, 0, 0, 1, 1, 2, 4, 4, 4, 5, 9, 9, 9, 10, 12, 13 };
    final int[] selfs = new int[] { 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
    final int[] descendants = new int[] { 16, 15, 8, 0, 2, 0, 0, 3, 2, 0, 0, 5, 2, 0, 0, 0, 0 };
    final int[] followings = new int[] { 0, 0, 6, 13, 10, 11, 10, 6, 6, 7, 6, 0, 2, 3, 2, 1, 0 };
    for (int i = 0; i < nodeSelects.length; i++) {
      final NodeList ancestorList = (NodeList) xpath.evaluate(nodeSelects[i] + "ancestor::*", doc,
              XPathConstants.NODESET);
      final NodeList precedingList = (NodeList) xpath.evaluate(nodeSelects[i] + "preceding::*", doc,
              XPathConstants.NODESET);
      final NodeList selfList = (NodeList) xpath.evaluate(nodeSelects[i] + "self::*", doc, XPathConstants.NODESET);
      final NodeList descendantList = (NodeList) xpath.evaluate(nodeSelects[i] + "descendant::*", doc,
              XPathConstants.NODESET);
      final NodeList followingList = (NodeList) xpath.evaluate(nodeSelects[i] + "following::*", doc,
              XPathConstants.NODESET);
      Assert.assertEquals("Node " + nodeNames[i] + " ancestors:", ancestors[i], ancestorList.getLength());
      Assert.assertEquals("Node " + nodeNames[i] + " precedings:", precedings[i], precedingList.getLength());
      Assert.assertEquals("Node " + nodeNames[i] + " selfs:", selfs[i], selfList.getLength());
      Assert.assertEquals("Node " + nodeNames[i] + " descendants:", descendants[i], descendantList.getLength());
      Assert.assertEquals("Node " + nodeNames[i] + " followings:", followings[i], followingList.getLength());
    }
  }

  @Test
  public void axes16() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes16.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/ancestor::*[3]", doc, XPathConstants.NODESET);
    Assert.assertEquals(1, nodeList.getLength());
    Assert.assertEquals("far-north", nodeList.item(0).getNodeName());
  }

  @Test
  public void axes17() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes17.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/ancestor-or-self::*[1]", doc, XPathConstants.NODESET);
    Assert.assertEquals(1, nodeList.getLength());
    Assert.assertEquals("center", nodeList.item(0).getNodeName());
  }

  @Test
  public void axes18() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes18.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/attribute::*[2]", doc, XPathConstants.NODESET);
    Assert.assertEquals(1, nodeList.getLength());
    Assert.assertEquals("center-attr-2", nodeList.item(0).getNodeName());
  }

  @Test
  public void axes19() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes19.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/@*", doc, XPathConstants.NODESET);
    Assert.assertEquals(3, nodeList.getLength());
    Assert.assertEquals("center-attr-1", nodeList.item(0).getNodeName());
    Assert.assertEquals("center-attr-2", nodeList.item(1).getNodeName());
    Assert.assertEquals("center-attr-3", nodeList.item(2).getNodeName());
  }

  @Test
  public void axes20() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes20.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/@*[2]", doc, XPathConstants.NODESET);
    Assert.assertEquals(0, nodeList.getLength());
  }

  @Test
  public void axes21() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes21.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/child::*[2]", doc, XPathConstants.NODESET);
    Assert.assertEquals(0, nodeList.getLength());
  }

  @Test
  public void axes22() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes22.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/child::near-south-west", doc, XPathConstants.NODESET);
    Assert.assertEquals(0, nodeList.getLength());
  }

  @Test
  public void axes23() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes23.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/descendant::*[3]", doc, XPathConstants.NODESET);
    Assert.assertEquals(1, nodeList.getLength());
    Assert.assertEquals("far-south", nodeList.item(0).getNodeName());
  }

  @Test
  public void axes24() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes24.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/descendant::far-south", doc, XPathConstants.NODESET);
    Assert.assertEquals(1, nodeList.getLength());
    Assert.assertEquals("far-south", nodeList.item(0).getNodeName());
  }

  @Test
  public void axes25() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes25.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/descendant-or-self::*[3]", doc,
            XPathConstants.NODESET);
    Assert.assertEquals(1, nodeList.getLength());
    Assert.assertEquals("south", nodeList.item(0).getNodeName());
  }

  @Test
  public void axes26() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes26.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/descendant-or-self::far-south", doc,
            XPathConstants.NODESET);
    Assert.assertEquals(1, nodeList.getLength());
    Assert.assertEquals("far-south", nodeList.item(0).getNodeName());
  }

  @Test
  public void axes27() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes27.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/descendant-or-self::center", doc,
            XPathConstants.NODESET);
    Assert.assertEquals(1, nodeList.getLength());
    Assert.assertEquals("center", nodeList.item(0).getNodeName());
  }

  @Test
  public void axes28() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes28.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/following::*[4]", doc, XPathConstants.NODESET);
    Assert.assertEquals(0, nodeList.getLength());
  }

  @Test
  public void axes29() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes29.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/following::out-yonder-east", doc,
            XPathConstants.NODESET);
    Assert.assertEquals(0, nodeList.getLength());
  }

  @Test
  public void axes30() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes30.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/preceding::*[4]", doc, XPathConstants.NODESET);
    Assert.assertEquals(0, nodeList.getLength());
  }

  @Test
  public void axes31() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes31.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/preceding::out-yonder-west", doc,
            XPathConstants.NODESET);
    Assert.assertEquals(0, nodeList.getLength());
  }

  @Test
  public void axes32() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes32.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath
            .evaluate("//center/following-sibling::*[2]", doc, XPathConstants.NODESET);
    Assert.assertEquals(1, nodeList.getLength());
    Assert.assertEquals("east", nodeList.item(0).getNodeName());
  }

  @Test
  public void axes33() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes33.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath
            .evaluate("//center/following-sibling::east", doc, XPathConstants.NODESET);
    Assert.assertEquals(1, nodeList.getLength());
    Assert.assertEquals("east", nodeList.item(0).getNodeName());
  }

  @Test
  public void axes34() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes34.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath
            .evaluate("//center/preceding-sibling::*[2]", doc, XPathConstants.NODESET);
    Assert.assertEquals(1, nodeList.getLength());
    Assert.assertEquals("west", nodeList.item(0).getNodeName());
  }

  @Test
  public void axes35() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes35.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath
            .evaluate("//center/preceding-sibling::west", doc, XPathConstants.NODESET);
    Assert.assertEquals(1, nodeList.getLength());
    Assert.assertEquals("west", nodeList.item(0).getNodeName());
  }

  @Test
  public void axes36() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes36.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/parent::near-north", doc, XPathConstants.NODESET);
    Assert.assertEquals(1, nodeList.getLength());
    Assert.assertEquals("near-north", nodeList.item(0).getNodeName());
  }

  @Test
  public void axes37() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes37.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/parent::*[1]", doc, XPathConstants.NODESET);
    Assert.assertEquals(1, nodeList.getLength());
    Assert.assertEquals("near-north", nodeList.item(0).getNodeName());
  }

  @Test
  public void axes38() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes38.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/parent::foo", doc, XPathConstants.NODESET);
    Assert.assertEquals(0, nodeList.getLength());
  }

  @Test
  public void axes39() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes39.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/..", doc, XPathConstants.NODESET);
    Assert.assertEquals(1, nodeList.getLength());
    Assert.assertEquals("near-north", nodeList.item(0).getNodeName());
  }

  @Test
  public void axes40() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes40.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/self::center", doc, XPathConstants.NODESET);
    Assert.assertEquals(1, nodeList.getLength());
    Assert.assertEquals("center", nodeList.item(0).getNodeName());
  }

  @Test
  public void axes41() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes41.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/self::*[1]", doc, XPathConstants.NODESET);
    Assert.assertEquals(1, nodeList.getLength());
    Assert.assertEquals("center", nodeList.item(0).getNodeName());
  }

  @Test
  public void axes42() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes42.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/self::foo", doc, XPathConstants.NODESET);
    Assert.assertEquals(0, nodeList.getLength());
  }

  @Test
  public void axes43() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes43.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/.", doc, XPathConstants.NODESET);
    Assert.assertEquals(1, nodeList.getLength());
    Assert.assertEquals("center", nodeList.item(0).getNodeName());
  }

  @Test
  public void axes44() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes44.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/attribute::center-attr-2", doc,
            XPathConstants.NODESET);
    Assert.assertEquals(1, nodeList.getLength());
    Assert.assertEquals("center-attr-2", nodeList.item(0).getNodeName());
  }

  @Test
  public void axes45() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes45.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/@center-attr-2", doc, XPathConstants.NODESET);
    Assert.assertEquals(1, nodeList.getLength());
    Assert.assertEquals("center-attr-2", nodeList.item(0).getNodeName());
  }

  @Test
  public void axes46() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes46.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/../@width", doc, XPathConstants.NODESET);
    Assert.assertEquals(1, nodeList.getLength());
    Assert.assertEquals("22", nodeList.item(0).getNodeValue());
  }

  @Test
  public void axes47() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes47.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("/docs//doc/..//foo/@att1", doc, XPathConstants.NODESET);
    Assert.assertEquals(4, nodeList.getLength());
    Assert.assertEquals("c", nodeList.item(0).getNodeValue());
    Assert.assertEquals("b", nodeList.item(1).getNodeValue());
    Assert.assertEquals("a", nodeList.item(2).getNodeValue());
    Assert.assertEquals("a", nodeList.item(3).getNodeValue());
  }

  @Test
  public void axes48() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes48.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/child::*/child::*", doc, XPathConstants.NODESET);
    Assert.assertEquals(1, nodeList.getLength());
    Assert.assertEquals("south", nodeList.item(0).getNodeName());
  }

  @Test
  public void axes49() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes49.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/child::*/descendant::*", doc, XPathConstants.NODESET);
    Assert.assertEquals(2, nodeList.getLength());
    Assert.assertEquals("south", nodeList.item(0).getNodeName());
    Assert.assertEquals("far-south", nodeList.item(1).getNodeName());
  }

  @Test
  public void axes50() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes50.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/descendant::*/child::*", doc, XPathConstants.NODESET);
    Assert.assertEquals(2, nodeList.getLength());
    Assert.assertEquals("south", nodeList.item(0).getNodeName());
    Assert.assertEquals("far-south", nodeList.item(1).getNodeName());
  }

  @Test
  public void axes51() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes51.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//near-north/center//child::*", doc, XPathConstants.NODESET);
    Assert.assertEquals(5, nodeList.getLength());
    Assert.assertEquals("near-south-east", nodeList.item(0).getNodeName());
    Assert.assertEquals("near-south", nodeList.item(1).getNodeName());
    Assert.assertEquals("south", nodeList.item(2).getNodeName());
    Assert.assertEquals("far-south", nodeList.item(3).getNodeName());
    Assert.assertEquals("near-south-west", nodeList.item(4).getNodeName());
  }

  @Test
  public void axes52() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes52.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//near-north/center//descendant::*", doc,
            XPathConstants.NODESET);
    Assert.assertEquals(5, nodeList.getLength());
    Assert.assertEquals("near-south-east", nodeList.item(0).getNodeName());
    Assert.assertEquals("near-south", nodeList.item(1).getNodeName());
    Assert.assertEquals("south", nodeList.item(2).getNodeName());
    Assert.assertEquals("far-south", nodeList.item(3).getNodeName());
    Assert.assertEquals("near-south-west", nodeList.item(4).getNodeName());
  }

  @Test
  public void axes53() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes53.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//near-north/center/descendant::*", doc,
            XPathConstants.NODESET);
    Assert.assertEquals(5, nodeList.getLength());
    Assert.assertEquals("near-south-east", nodeList.item(0).getNodeName());
    Assert.assertEquals("near-south", nodeList.item(1).getNodeName());
    Assert.assertEquals("south", nodeList.item(2).getNodeName());
    Assert.assertEquals("far-south", nodeList.item(3).getNodeName());
    Assert.assertEquals("near-south-west", nodeList.item(4).getNodeName());
  }

  @Test
  public void axes54() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes54.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//near-north/center/child::*", doc, XPathConstants.NODESET);
    Assert.assertEquals(3, nodeList.getLength());
    Assert.assertEquals("near-south-east", nodeList.item(0).getNodeName());
    Assert.assertEquals("near-south", nodeList.item(1).getNodeName());
    Assert.assertEquals("near-south-west", nodeList.item(2).getNodeName());
  }

  @Test
  public void axes55() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes55.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//near-north/center//*", doc, XPathConstants.NODESET);
    Assert.assertEquals(5, nodeList.getLength());
    Assert.assertEquals("near-south-east", nodeList.item(0).getNodeName());
    Assert.assertEquals("near-south", nodeList.item(1).getNodeName());
    Assert.assertEquals("south", nodeList.item(2).getNodeName());
    Assert.assertEquals("far-south", nodeList.item(3).getNodeName());
    Assert.assertEquals("near-south-west", nodeList.item(4).getNodeName());
  }

  @Test
  public void axes56() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes56.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//xx/descendant::*", doc, XPathConstants.NODESET);
    Assert.assertEquals(5, nodeList.getLength());
    Assert.assertEquals("xxchild", nodeList.item(0).getNodeName());
    Assert.assertEquals("xxchild", nodeList.item(1).getNodeName());
    Assert.assertEquals("childofxx", nodeList.item(2).getNodeName());
    Assert.assertEquals("xxsub", nodeList.item(3).getNodeName());
    Assert.assertEquals("xxsubsub", nodeList.item(4).getNodeName());
  }

  @Test
  public void axes57() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes57.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//title", doc, XPathConstants.NODESET);
    Assert.assertEquals(16, nodeList.getLength());
    Assert.assertEquals("Test for source tree depth", nodeList.item(0).getTextContent());
    Assert.assertEquals("Level A", nodeList.item(1).getTextContent());
    Assert.assertEquals("Level B", nodeList.item(2).getTextContent());
    Assert.assertEquals("Level C", nodeList.item(3).getTextContent());
    Assert.assertEquals("Level D", nodeList.item(4).getTextContent());
    Assert.assertEquals("Level E", nodeList.item(5).getTextContent());
    Assert.assertEquals("Level F", nodeList.item(6).getTextContent());
    Assert.assertEquals("Level G", nodeList.item(7).getTextContent());
    Assert.assertEquals("Level H", nodeList.item(8).getTextContent());
    Assert.assertEquals("Level I", nodeList.item(9).getTextContent());
    Assert.assertEquals("Level J", nodeList.item(10).getTextContent());
    Assert.assertEquals("Level K", nodeList.item(11).getTextContent());
    Assert.assertEquals("Level L", nodeList.item(12).getTextContent());
    Assert.assertEquals("Level M", nodeList.item(13).getTextContent());
    Assert.assertEquals("Level N", nodeList.item(14).getTextContent());
    Assert.assertEquals("Level O", nodeList.item(15).getTextContent());
  }

  @Test
  public void axes58() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes58.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final XPath namespaced = xpathfactory.newXPath();
    namespaced.setNamespaceContext(new NamespaceContextBuilder().add("foo", "http://www.ped.com").build());
    final NodeList nodeList = (NodeList) namespaced.evaluate("/docs/doc/attribute::*  | /docs/foo:doc/attribute::*",
            doc, XPathConstants.NODESET);
    Assert.assertEquals(7, nodeList.getLength());
    Assert.assertEquals("x ", nodeList.item(0).getNodeValue());
    Assert.assertEquals("y ", nodeList.item(1).getNodeValue());
    Assert.assertEquals("z ", nodeList.item(2).getNodeValue());
    Assert.assertEquals("ay ", nodeList.item(3).getNodeValue());
    Assert.assertEquals("az ", nodeList.item(4).getNodeValue());
    Assert.assertEquals("by ", nodeList.item(5).getNodeValue());
    Assert.assertEquals("bz ", nodeList.item(6).getNodeValue());
  }

  @Test
  public void axes59() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes59.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//docs//namespace::*", doc, XPathConstants.NODESET);
    Assert.assertEquals(6, nodeList.getLength());
    Assert.assertEquals("http://www.w3.org/XML/1998/namespace", nodeList.item(0).getNodeValue());
    Assert.assertEquals("http://buster.com", nodeList.item(1).getNodeValue());
    Assert.assertEquals("http://somebody.elses.extension", nodeList.item(2).getNodeValue());
    Assert.assertEquals("http://administrator.com", nodeList.item(3).getNodeValue());
    Assert.assertEquals("http://xml.apache.org/xslt/java", nodeList.item(4).getNodeValue());
    Assert.assertEquals("http://tester.com", nodeList.item(5).getNodeValue());
    Assert.assertEquals("xmlns:xml", nodeList.item(0).getNodeName());
    Assert.assertEquals("xmlns:bdd", nodeList.item(1).getNodeName());
    Assert.assertEquals("xmlns:ext", nodeList.item(2).getNodeName());
    Assert.assertEquals("xmlns:jad", nodeList.item(3).getNodeName());
    Assert.assertEquals("xmlns:java", nodeList.item(4).getNodeName());
    Assert.assertEquals("xmlns:ped", nodeList.item(5).getNodeName());
  }

  @Test
  public void axes60() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes60.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/attribute::*", doc, XPathConstants.NODESET);
    Assert.assertEquals(3, nodeList.getLength());
    Assert.assertEquals("center-attr-1", nodeList.item(0).getNodeName());
    Assert.assertEquals("center-attr-2", nodeList.item(1).getNodeName());
    Assert.assertEquals("center-attr-3", nodeList.item(2).getNodeName());
  }

  @Test
  public void axes61() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes61.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//near-north/child::*", doc, XPathConstants.NODESET);
    Assert.assertEquals(7, nodeList.getLength());
    Assert.assertEquals("far-west", nodeList.item(0).getNodeName());
    Assert.assertEquals("west", nodeList.item(1).getNodeName());
    Assert.assertEquals("near-west", nodeList.item(2).getNodeName());
    Assert.assertEquals("center", nodeList.item(3).getNodeName());
    Assert.assertEquals("near-east", nodeList.item(4).getNodeName());
    Assert.assertEquals("east", nodeList.item(5).getNodeName());
    Assert.assertEquals("far-east", nodeList.item(6).getNodeName());
  }

  @Test
  public void axes62() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes62.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("/doc/namespace::ped | /doc/namespace::bdd", doc,
            XPathConstants.NODESET);
    Assert.assertEquals(2, nodeList.getLength());
    Assert.assertEquals("xmlns:bdd", nodeList.item(0).getNodeName());
    Assert.assertEquals("xmlns:ped", nodeList.item(1).getNodeName());
    Assert.assertEquals("http://buster.com", nodeList.item(0).getNodeValue());
    Assert.assertEquals("http://tester.com", nodeList.item(1).getNodeValue());
  }

  @Test
  public void axes63() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes63.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/self::*[near-south]", doc, XPathConstants.NODESET);
    Assert.assertEquals(1, nodeList.getLength());
    Assert.assertEquals("center", nodeList.item(0).getNodeName());
  }

  @Test
  public void axes64() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes64.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath
            .evaluate("//center/self::*[@center-attr-2]", doc, XPathConstants.NODESET);
    Assert.assertEquals(1, nodeList.getLength());
    Assert.assertEquals("center", nodeList.item(0).getNodeName());
  }

  @Test
  public void axes65() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes65.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/self::text()", doc, XPathConstants.NODESET);
    Assert.assertEquals(0, nodeList.getLength());
  }

  @Test
  public void axes66() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes66.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/self::comment()", doc, XPathConstants.NODESET);
    Assert.assertEquals(0, nodeList.getLength());
  }

  @Test
  public void axes67() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes67.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/self::processing-instruction()", doc,
            XPathConstants.NODESET);
    Assert.assertEquals(0, nodeList.getLength());
  }

  @Test
  public void axes68() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes68.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("/*//namespace::*", doc, XPathConstants.NODESET);
    Assert.assertEquals(4, nodeList.getLength());
    Assert.assertEquals("xmlns:xml", nodeList.item(0).getNodeName());
    Assert.assertEquals("xmlns:ext", nodeList.item(1).getNodeName());
    Assert.assertEquals("xmlns:foo", nodeList.item(2).getNodeName());
    Assert.assertEquals("xmlns:whiz", nodeList.item(3).getNodeName());
    Assert.assertEquals("http://www.w3.org/XML/1998/namespace", nodeList.item(0).getNodeValue());
    Assert.assertEquals("http://somebody.elses.extension", nodeList.item(1).getNodeValue());
    Assert.assertEquals("http://foo.com", nodeList.item(2).getNodeValue());
    Assert.assertEquals("http://whiz.com/special/page", nodeList.item(3).getNodeValue());
  }

  @Test
  public void axes69() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes69.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/preceding-sibling::*/following-sibling::*", doc,
            XPathConstants.NODESET);
    Assert.assertEquals(6, nodeList.getLength());
    Assert.assertEquals("west", nodeList.item(0).getNodeName());
    Assert.assertEquals("near-west", nodeList.item(1).getNodeName());
    Assert.assertEquals("center", nodeList.item(2).getNodeName());
    Assert.assertEquals("near-east", nodeList.item(3).getNodeName());
    Assert.assertEquals("east", nodeList.item(4).getNodeName());
    Assert.assertEquals("far-east", nodeList.item(5).getNodeName());
  }

  @Test
  public void axes70() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes70.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/preceding-sibling::*/following-sibling::*", doc,
            XPathConstants.NODESET);
    Assert.assertEquals(6, nodeList.getLength());
    Assert.assertEquals("west", nodeList.item(0).getNodeName());
    Assert.assertEquals("near-west", nodeList.item(1).getNodeName());
    Assert.assertEquals("center", nodeList.item(2).getNodeName());
    Assert.assertEquals("near-east", nodeList.item(3).getNodeName());
    Assert.assertEquals("east", nodeList.item(4).getNodeName());
    Assert.assertEquals("far-east", nodeList.item(5).getNodeName());
  }

  @Test
  public void axes71() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes71.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/preceding-sibling::*[2]/following-sibling::*", doc,
            XPathConstants.NODESET);
    Assert.assertEquals(5, nodeList.getLength());
    Assert.assertEquals("near-west", nodeList.item(0).getNodeName());
    Assert.assertEquals("center", nodeList.item(1).getNodeName());
    Assert.assertEquals("near-east", nodeList.item(2).getNodeName());
    Assert.assertEquals("east", nodeList.item(3).getNodeName());
    Assert.assertEquals("far-east", nodeList.item(4).getNodeName());
  }

  @Test
  public void axes72() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes72.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/preceding-sibling::*[2]/following-sibling::*[4]",
            doc, XPathConstants.NODESET);
    Assert.assertEquals(1, nodeList.getLength());
    Assert.assertEquals("east", nodeList.item(0).getNodeName());
  }

  @Test
  public void axes73() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes73.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath
            .evaluate(
                    "//center/preceding-sibling::*[2]/following-sibling::*[4]/preceding-sibling::*[5]/following-sibling::*[4]/following-sibling::*[2]",
                    doc, XPathConstants.NODESET);
    Assert.assertEquals(1, nodeList.getLength());
    Assert.assertEquals("far-east", nodeList.item(0).getNodeName());
  }

  @Test
  public void axes74() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes74.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/following-sibling::*/preceding-sibling::*", doc,
            XPathConstants.NODESET);
    Assert.assertEquals(6, nodeList.getLength());
    Assert.assertEquals("far-west", nodeList.item(0).getNodeName());
    Assert.assertEquals("west", nodeList.item(1).getNodeName());
    Assert.assertEquals("near-west", nodeList.item(2).getNodeName());
    Assert.assertEquals("center", nodeList.item(3).getNodeName());
    Assert.assertEquals("near-east", nodeList.item(4).getNodeName());
    Assert.assertEquals("east", nodeList.item(5).getNodeName());
  }

  @Test
  public void axes75() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes75.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/following-sibling::*/preceding-sibling::*", doc,
            XPathConstants.NODESET);
    Assert.assertEquals(6, nodeList.getLength());
    Assert.assertEquals("far-west", nodeList.item(0).getNodeName());
    Assert.assertEquals("west", nodeList.item(1).getNodeName());
    Assert.assertEquals("near-west", nodeList.item(2).getNodeName());
    Assert.assertEquals("center", nodeList.item(3).getNodeName());
    Assert.assertEquals("near-east", nodeList.item(4).getNodeName());
    Assert.assertEquals("east", nodeList.item(5).getNodeName());
  }

  @Test
  public void axes76() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes76.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/following-sibling::*[2]/preceding-sibling::*", doc,
            XPathConstants.NODESET);
    Assert.assertEquals(5, nodeList.getLength());
    Assert.assertEquals("far-west", nodeList.item(0).getNodeName());
    Assert.assertEquals("west", nodeList.item(1).getNodeName());
    Assert.assertEquals("near-west", nodeList.item(2).getNodeName());
    Assert.assertEquals("center", nodeList.item(3).getNodeName());
    Assert.assertEquals("near-east", nodeList.item(4).getNodeName());
  }

  @Test
  public void axes77() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes77.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/following-sibling::*[2]/preceding-sibling::*[4]",
            doc, XPathConstants.NODESET);
    Assert.assertEquals(1, nodeList.getLength());
    Assert.assertEquals("west", nodeList.item(0).getNodeName());
  }

  @Test
  public void axes78() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes78.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath
            .evaluate(
                    "//center/following-sibling::*[2]/preceding-sibling::*[4]/following-sibling::*[5]/preceding-sibling::*[4]/preceding-sibling::*[2]",
                    doc, XPathConstants.NODESET);
    Assert.assertEquals(1, nodeList.getLength());
    Assert.assertEquals("far-west", nodeList.item(0).getNodeName());
  }

  @Test
  public void axes79() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes79.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath
            .evaluate("//center/following::*[4]/../*[2]", doc, XPathConstants.NODESET);
    Assert.assertEquals(1, nodeList.getLength());
    Assert.assertEquals("north-north-west2", nodeList.item(0).getNodeName());
  }

  @Test
  public void axes80() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes80.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/preceding::*[2]/../following::*", doc,
            XPathConstants.NODESET);
    Assert.assertEquals(2, nodeList.getLength());
    Assert.assertEquals("north-north-east1", nodeList.item(0).getNodeName());
    Assert.assertEquals("north-north-east2", nodeList.item(1).getNodeName());
  }

  @Test
  public void axes81() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes81.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate(
            "//center/preceding::*[2]/../descendant::*[10]/following-sibling::east", doc, XPathConstants.NODESET);
    Assert.assertEquals(1, nodeList.getLength());
    Assert.assertEquals("east", nodeList.item(0).getNodeName());
  }

  @Test
  public void axes82() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes82.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    // '//*[//center]' means 'select all nodes, if document has a center node'
    final NodeList nodeList = (NodeList) xpath.evaluate("//*[//center]", doc, XPathConstants.NODESET);
    Assert.assertEquals(19, nodeList.getLength());
    Assert.assertEquals("far-north", nodeList.item(0).getNodeName());
    Assert.assertEquals("north-north-west1", nodeList.item(1).getNodeName());
    Assert.assertEquals("north-north-west2", nodeList.item(2).getNodeName());
    Assert.assertEquals("north", nodeList.item(3).getNodeName());
    Assert.assertEquals("near-north", nodeList.item(4).getNodeName());
    Assert.assertEquals("far-west", nodeList.item(5).getNodeName());
    Assert.assertEquals("west", nodeList.item(6).getNodeName());
    Assert.assertEquals("near-west", nodeList.item(7).getNodeName());
    Assert.assertEquals("center", nodeList.item(8).getNodeName());
    Assert.assertEquals("near-south-west", nodeList.item(9).getNodeName());
    Assert.assertEquals("near-south", nodeList.item(10).getNodeName());
    Assert.assertEquals("south", nodeList.item(11).getNodeName());
    Assert.assertEquals("far-south", nodeList.item(12).getNodeName());
    Assert.assertEquals("near-south-east", nodeList.item(13).getNodeName());
    Assert.assertEquals("near-east", nodeList.item(14).getNodeName());
    Assert.assertEquals("east", nodeList.item(15).getNodeName());
    Assert.assertEquals("far-east", nodeList.item(16).getNodeName());
    Assert.assertEquals("north-north-east1", nodeList.item(17).getNodeName());
    Assert.assertEquals("north-north-east2", nodeList.item(18).getNodeName());
  }

  @Test
  public void axes83() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes83.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center//ancestor::*", doc, XPathConstants.NODESET);
    Assert.assertEquals(6, nodeList.getLength());
    Assert.assertEquals("far-north", nodeList.item(0).getNodeName());
    Assert.assertEquals("north", nodeList.item(1).getNodeName());
    Assert.assertEquals("near-north", nodeList.item(2).getNodeName());
    Assert.assertEquals("center", nodeList.item(3).getNodeName());
    Assert.assertEquals("near-south", nodeList.item(4).getNodeName());
    Assert.assertEquals("south", nodeList.item(5).getNodeName());
  }

  @Test
  public void axes84() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes84.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    // '//*[//center]' means 'select all nodes, if document has a center node'
    final NodeList nodeList = (NodeList) xpath.evaluate("//*[//center][count(ancestor::*) >= 2]/../parent::*", doc,
            XPathConstants.NODESET);
    Assert.assertEquals(5, nodeList.getLength());
    Assert.assertEquals("far-north", nodeList.item(0).getNodeName());
    Assert.assertEquals("north", nodeList.item(1).getNodeName());
    Assert.assertEquals("near-north", nodeList.item(2).getNodeName());
    Assert.assertEquals("center", nodeList.item(3).getNodeName());
    Assert.assertEquals("near-south", nodeList.item(4).getNodeName());
  }

  @Test
  public void axes85() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes85.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    // '//*[//center]' means 'select all nodes, if document has a center node'
    final NodeList nodeList = (NodeList) xpath.evaluate("//*[//center][count(./*/*) > 0]", doc, XPathConstants.NODESET);
    Assert.assertEquals(5, nodeList.getLength());
    Assert.assertEquals("far-north", nodeList.item(0).getNodeName());
    Assert.assertEquals("north", nodeList.item(1).getNodeName());
    Assert.assertEquals("near-north", nodeList.item(2).getNodeName());
    Assert.assertEquals("center", nodeList.item(3).getNodeName());
    Assert.assertEquals("near-south", nodeList.item(4).getNodeName());
  }

  @Test
  public void axes86() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes86.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate(
            "//center/ancestor::*[count(child::*) > 1]/*[not(//center = .//ancestor-or-self::*)]", doc,
            XPathConstants.NODESET);
    Assert.assertEquals(10, nodeList.getLength());
    Assert.assertEquals("north-north-west1", nodeList.item(0).getNodeName());
    Assert.assertEquals("north-north-west2", nodeList.item(1).getNodeName());
    Assert.assertEquals("far-west", nodeList.item(2).getNodeName());
    Assert.assertEquals("west", nodeList.item(3).getNodeName());
    Assert.assertEquals("near-west", nodeList.item(4).getNodeName());
    Assert.assertEquals("near-east", nodeList.item(5).getNodeName());
    Assert.assertEquals("east", nodeList.item(6).getNodeName());
    Assert.assertEquals("far-east", nodeList.item(7).getNodeName());
    Assert.assertEquals("north-north-east1", nodeList.item(8).getNodeName());
    Assert.assertEquals("north-north-east2", nodeList.item(9).getNodeName());

  }

  @Test
  public void axes87() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes87.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/@*/ancestor::*", doc, XPathConstants.NODESET);
    Assert.assertEquals(4, nodeList.getLength());
    Assert.assertEquals("far-north", nodeList.item(0).getNodeName());
    Assert.assertEquals("north", nodeList.item(1).getNodeName());
    Assert.assertEquals("near-north", nodeList.item(2).getNodeName());
    Assert.assertEquals("center", nodeList.item(3).getNodeName());
  }

  @Test
  public void axes88() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes88.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/@*/following::*", doc, XPathConstants.NODESET);
    Assert.assertEquals(10, nodeList.getLength());
    Assert.assertEquals("near-south-west", nodeList.item(0).getNodeName());
    Assert.assertEquals("near-south", nodeList.item(1).getNodeName());
    Assert.assertEquals("south", nodeList.item(2).getNodeName());
    Assert.assertEquals("far-south", nodeList.item(3).getNodeName());
    Assert.assertEquals("near-south-east", nodeList.item(4).getNodeName());
    Assert.assertEquals("near-east", nodeList.item(5).getNodeName());
    Assert.assertEquals("east", nodeList.item(6).getNodeName());
    Assert.assertEquals("far-east", nodeList.item(7).getNodeName());
    Assert.assertEquals("north-north-east1", nodeList.item(8).getNodeName());
    Assert.assertEquals("north-north-east2", nodeList.item(9).getNodeName());
  }

  @Test
  public void axes89() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes89.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/@*/preceding::*", doc, XPathConstants.NODESET);
    Assert.assertEquals(5, nodeList.getLength());
    Assert.assertEquals("north-north-west1", nodeList.item(0).getNodeName());
    Assert.assertEquals("north-north-west2", nodeList.item(1).getNodeName());
    Assert.assertEquals("far-west", nodeList.item(2).getNodeName());
    Assert.assertEquals("west", nodeList.item(3).getNodeName());
    Assert.assertEquals("near-west", nodeList.item(4).getNodeName());
  }

  @Test
  public void axes90() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes90.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/preceding-sibling::*|//center/following-sibling::*",
            doc, XPathConstants.NODESET);
    Assert.assertEquals(6, nodeList.getLength());
    Assert.assertEquals("far-west", nodeList.item(0).getNodeName());
    Assert.assertEquals("west", nodeList.item(1).getNodeName());
    Assert.assertEquals("near-west", nodeList.item(2).getNodeName());
    Assert.assertEquals("near-east", nodeList.item(3).getNodeName());
    Assert.assertEquals("east", nodeList.item(4).getNodeName());
    Assert.assertEquals("far-east", nodeList.item(5).getNodeName());
  }

  @Test
  public void axes91() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes91.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    // TODO revisit XPath
    final NodeList nodeList = (NodeList) xpath
            .evaluate(
                    "//center/preceding-sibling::*/ancestor::*[last()]/*[last()] | //center/following-sibling::*/ancestor::*[last()]/*[last()]",
                    doc, XPathConstants.NODESET);
    Assert.assertEquals(1, nodeList.getLength());
    Assert.assertEquals("north-north-east2", nodeList.item(0).getNodeName());
  }

  @Test
  public void axes92() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes92.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate(
            "//center//near-south/preceding-sibling::*|//center/following-sibling::east/ancestor-or-self::*[2]", doc,
            XPathConstants.NODESET);
    Assert.assertEquals(2, nodeList.getLength());
    Assert.assertEquals("near-north", nodeList.item(0).getNodeName());
    Assert.assertEquals("near-south-west", nodeList.item(1).getNodeName());
  }

  @Test
  public void axes93() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes93.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/@*", doc, XPathConstants.NODESET);
    Assert.assertEquals(3, nodeList.getLength());
    Assert.assertEquals("center-attr-1", nodeList.item(0).getNodeName());
    Assert.assertEquals("center-attr-2", nodeList.item(1).getNodeName());
    Assert.assertEquals("center-attr-3", nodeList.item(2).getNodeName());
  }

  @Test
  public void axes94() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes94.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/@*/child::*", doc, XPathConstants.NODESET);
    Assert.assertEquals(0, nodeList.getLength());
  }

  @Test
  public void axes95() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes95.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/@*/descendant::node()", doc, XPathConstants.NODESET);
    Assert.assertEquals(0, nodeList.getLength());
  }

  @Test
  public void axes96() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes96.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/@*/parent::*", doc, XPathConstants.NODESET);
    Assert.assertEquals(1, nodeList.getLength());
    Assert.assertEquals("center", nodeList.item(0).getNodeName());
  }

  @Test
  public void axes97() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes97.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/@*/ancestor::*", doc, XPathConstants.NODESET);
    Assert.assertEquals(4, nodeList.getLength());
    Assert.assertEquals("far-north", nodeList.item(0).getNodeName());
    Assert.assertEquals("north", nodeList.item(1).getNodeName());
    Assert.assertEquals("near-north", nodeList.item(2).getNodeName());
    Assert.assertEquals("center", nodeList.item(3).getNodeName());
  }

  @Test
  public void axes98() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes98.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList1 = (NodeList) xpath.evaluate("//center/@*/self::node()", doc, XPathConstants.NODESET);
    final NodeList nodeList2 = (NodeList) xpath.evaluate("//center/@*/.", doc, XPathConstants.NODESET);
    Assert.assertEquals(3, nodeList1.getLength());
    Assert.assertEquals("center-attr-1", nodeList1.item(0).getNodeName());
    Assert.assertEquals("center-attr-2", nodeList1.item(1).getNodeName());
    Assert.assertEquals("center-attr-3", nodeList1.item(2).getNodeName());
    Assert.assertEquals(3, nodeList2.getLength());
    Assert.assertEquals("center-attr-1", nodeList2.item(0).getNodeName());
    Assert.assertEquals("center-attr-2", nodeList2.item(1).getNodeName());
    Assert.assertEquals("center-attr-3", nodeList2.item(2).getNodeName());
  }

  @Test
  public void axes99() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes99.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/@*/descendant-or-self::node()", doc,
            XPathConstants.NODESET);
    Assert.assertEquals(3, nodeList.getLength());
    Assert.assertEquals("center-attr-1", nodeList.item(0).getNodeName());
    Assert.assertEquals("center-attr-2", nodeList.item(1).getNodeName());
    Assert.assertEquals("center-attr-3", nodeList.item(2).getNodeName());
  }

  @Test
  public void axes100() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes100.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/@*/ancestor-or-self::*", doc, XPathConstants.NODESET);
    Assert.assertEquals(4, nodeList.getLength());
    Assert.assertEquals("far-north", nodeList.item(0).getNodeName());
    Assert.assertEquals("north", nodeList.item(1).getNodeName());
    Assert.assertEquals("near-north", nodeList.item(2).getNodeName());
    Assert.assertEquals("center", nodeList.item(3).getNodeName());
  }

  @Test
  public void axes101() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes101.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/@*/preceding-sibling::node()", doc,
            XPathConstants.NODESET);
    Assert.assertEquals(0, nodeList.getLength());
  }

  @Test
  public void axes102() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes102.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath
            .evaluate("//center/@*/following-sibling::*", doc, XPathConstants.NODESET);
    Assert.assertEquals(0, nodeList.getLength());
  }

  @Test
  public void axes103() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes103.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/@*/ancestor::*/near-north/*[4]/@*/preceding::*", doc,
            XPathConstants.NODESET);
    Assert.assertEquals(5, nodeList.getLength());
    Assert.assertEquals("north-north-west1", nodeList.item(0).getNodeName());
    Assert.assertEquals("north-north-west2", nodeList.item(1).getNodeName());
    Assert.assertEquals("far-west", nodeList.item(2).getNodeName());
    Assert.assertEquals("west", nodeList.item(3).getNodeName());
    Assert.assertEquals("near-west", nodeList.item(4).getNodeName());
  }

  @Test
  public void axes104() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes104.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate(
            "//center/@*/ancestor::*/near-north/*[4]/@*/preceding::comment()", doc, XPathConstants.NODESET);
    Assert.assertEquals(3, nodeList.getLength());
    Assert.assertEquals(" Comment-2 ", nodeList.item(0).getNodeValue());
    Assert.assertEquals(" Comment-3 ", nodeList.item(1).getNodeValue());
    Assert.assertEquals(" Comment-4 ", nodeList.item(2).getNodeValue());
  }

  @Test
  public void axes105() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes105.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/@*/ancestor::*/near-north/*[4]/@*/preceding::text()",
            doc, XPathConstants.NODESET);
    Assert.assertEquals(14, nodeList.getLength());
    Assert.assertEquals("#text", nodeList.item(0).getNodeName());
    Assert.assertEquals("#text", nodeList.item(1).getNodeName());
    Assert.assertEquals("#text", nodeList.item(2).getNodeName());
    Assert.assertEquals("#text", nodeList.item(3).getNodeName());
    Assert.assertEquals("#text", nodeList.item(4).getNodeName());
    Assert.assertEquals("#text", nodeList.item(5).getNodeName());
    Assert.assertEquals("#text", nodeList.item(6).getNodeName());
    Assert.assertEquals("#text", nodeList.item(7).getNodeName());
    Assert.assertEquals("#text", nodeList.item(8).getNodeName());
    Assert.assertEquals("#text", nodeList.item(9).getNodeName());
    Assert.assertEquals("#text", nodeList.item(10).getNodeName());
    Assert.assertEquals("#text", nodeList.item(11).getNodeName());
    Assert.assertEquals("#text", nodeList.item(12).getNodeName());
    Assert.assertEquals("#text", nodeList.item(13).getNodeName());
  }

  @Test
  public void axes106() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes106.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate(
            "//center/@*/ancestor::*/near-north/*[4]/@*/preceding::processing-instruction()", doc,
            XPathConstants.NODESET);
    Assert.assertEquals(3, nodeList.getLength());
    Assert.assertEquals("pi-2", nodeList.item(0).getNodeValue());
    Assert.assertEquals("pi-3", nodeList.item(1).getNodeValue());
    Assert.assertEquals("pi-4", nodeList.item(2).getNodeValue());
  }

  @Test
  public void axes107() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes107.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/@*/ancestor::*/near-north/*[4]/@*/preceding::node()",
            doc, XPathConstants.NODESET);
    Assert.assertEquals(25, nodeList.getLength());
    Assert.assertEquals("#text", nodeList.item(0).getNodeName());
    Assert.assertEquals("north-north-west1", nodeList.item(1).getNodeName());
    Assert.assertEquals("#text", nodeList.item(2).getNodeName());
    Assert.assertEquals("north-north-west2", nodeList.item(3).getNodeName());
    Assert.assertEquals("#text", nodeList.item(4).getNodeName());
    Assert.assertEquals("#comment", nodeList.item(5).getNodeName());
    Assert.assertEquals("#text", nodeList.item(6).getNodeName());
    Assert.assertEquals("a-pi", nodeList.item(7).getNodeName());
    Assert.assertEquals("#text", nodeList.item(8).getNodeName());
    Assert.assertEquals("#text", nodeList.item(9).getNodeName());
    Assert.assertEquals("#comment", nodeList.item(10).getNodeName());
    Assert.assertEquals("#text", nodeList.item(11).getNodeName());
    Assert.assertEquals("a-pi", nodeList.item(12).getNodeName());
    Assert.assertEquals("#text", nodeList.item(13).getNodeName());
    Assert.assertEquals("#text", nodeList.item(14).getNodeName());
    Assert.assertEquals("far-west", nodeList.item(15).getNodeName());
    Assert.assertEquals("#text", nodeList.item(16).getNodeName());
    Assert.assertEquals("west", nodeList.item(17).getNodeName());
    Assert.assertEquals("#text", nodeList.item(18).getNodeName());
    Assert.assertEquals("near-west", nodeList.item(19).getNodeName());
    Assert.assertEquals("#text", nodeList.item(20).getNodeName());
    Assert.assertEquals("#comment", nodeList.item(21).getNodeName());
    Assert.assertEquals("#text", nodeList.item(22).getNodeName());
    Assert.assertEquals("a-pi", nodeList.item(23).getNodeName());
    Assert.assertEquals("#text", nodeList.item(24).getNodeName());
  }

  @Test
  public void axes108() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes108.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath
            .evaluate("//center/@*/following::comment()", doc, XPathConstants.NODESET);
    Assert.assertEquals(2, nodeList.getLength());
    Assert.assertEquals("Comment-5", nodeList.item(0).getNodeValue());
    Assert.assertEquals("Comment-6", nodeList.item(1).getNodeValue());
  }

  @Test
  public void axes109() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes109.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//c/@x/following::*", doc, XPathConstants.NODESET);
    Assert.assertEquals(4, nodeList.getLength());
    Assert.assertEquals("d1", nodeList.item(0).getNodeName());
    Assert.assertEquals("d2", nodeList.item(1).getNodeName());
    Assert.assertEquals("c2", nodeList.item(2).getNodeName());
    Assert.assertEquals("b2", nodeList.item(3).getNodeName());
  }

  @Test
  public void axes110() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes110.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/@*/following::processing-instruction()", doc,
            XPathConstants.NODESET);
    Assert.assertEquals(2, nodeList.getLength());
    Assert.assertEquals("pi-5", nodeList.item(0).getNodeValue());
    Assert.assertEquals("pi-6", nodeList.item(1).getNodeValue());
  }

  @Test
  public void axes111() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes111.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/@*/ancestor::*/near-north/*[4]/@*/following::node()",
            doc, XPathConstants.NODESET);
    Assert.assertEquals(34, nodeList.getLength());
    Assert.assertEquals("#text", nodeList.item(0).getNodeName());
    Assert.assertEquals("near-south-west", nodeList.item(1).getNodeName());
    Assert.assertEquals("#text", nodeList.item(2).getNodeName());
    Assert.assertEquals("#comment", nodeList.item(3).getNodeName());
    Assert.assertEquals("#text", nodeList.item(4).getNodeName());
    Assert.assertEquals("a-pi", nodeList.item(5).getNodeName());
    Assert.assertEquals("#text", nodeList.item(6).getNodeName());
    Assert.assertEquals("near-south", nodeList.item(7).getNodeName());
    Assert.assertEquals("#text", nodeList.item(8).getNodeName());
    Assert.assertEquals("#comment", nodeList.item(9).getNodeName());
    Assert.assertEquals("#text", nodeList.item(10).getNodeName());
    Assert.assertEquals("a-pi", nodeList.item(11).getNodeName());
    Assert.assertEquals("#text", nodeList.item(12).getNodeName());
    Assert.assertEquals("south", nodeList.item(13).getNodeName());
    Assert.assertEquals("#text", nodeList.item(14).getNodeName());
    Assert.assertEquals("far-south", nodeList.item(15).getNodeName());
    Assert.assertEquals("#text", nodeList.item(16).getNodeName());
    Assert.assertEquals("#text", nodeList.item(17).getNodeName());
    Assert.assertEquals("#text", nodeList.item(18).getNodeName());
    Assert.assertEquals("near-south-east", nodeList.item(19).getNodeName());
    Assert.assertEquals("#text", nodeList.item(20).getNodeName());
    Assert.assertEquals("#text", nodeList.item(21).getNodeName());
    Assert.assertEquals("near-east", nodeList.item(22).getNodeName());
    Assert.assertEquals("#text", nodeList.item(23).getNodeName());
    Assert.assertEquals("east", nodeList.item(24).getNodeName());
    Assert.assertEquals("#text", nodeList.item(25).getNodeName());
    Assert.assertEquals("far-east", nodeList.item(26).getNodeName());
    Assert.assertEquals("#text", nodeList.item(27).getNodeName());
    Assert.assertEquals("#text", nodeList.item(28).getNodeName());
    Assert.assertEquals("#text", nodeList.item(29).getNodeName());
    Assert.assertEquals("north-north-east1", nodeList.item(30).getNodeName());
    Assert.assertEquals("#text", nodeList.item(31).getNodeName());
    Assert.assertEquals("north-north-east2", nodeList.item(32).getNodeName());
    Assert.assertEquals("#text", nodeList.item(33).getNodeName());
  }

  @Test
  public void axes112() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes112.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/@*/following::text()", doc, XPathConstants.NODESET);
    Assert.assertEquals(20, nodeList.getLength());
    Assert.assertEquals("#text", nodeList.item(0).getNodeName());
    Assert.assertEquals("#text", nodeList.item(1).getNodeName());
    Assert.assertEquals("#text", nodeList.item(2).getNodeName());
    Assert.assertEquals("#text", nodeList.item(3).getNodeName());
    Assert.assertEquals("#text", nodeList.item(4).getNodeName());
    Assert.assertEquals("#text", nodeList.item(5).getNodeName());
    Assert.assertEquals("#text", nodeList.item(6).getNodeName());
    Assert.assertEquals("#text", nodeList.item(7).getNodeName());
    Assert.assertEquals("#text", nodeList.item(8).getNodeName());
    Assert.assertEquals("#text", nodeList.item(9).getNodeName());
    Assert.assertEquals("#text", nodeList.item(10).getNodeName());
    Assert.assertEquals("#text", nodeList.item(11).getNodeName());
    Assert.assertEquals("#text", nodeList.item(12).getNodeName());
    Assert.assertEquals("#text", nodeList.item(13).getNodeName());
    Assert.assertEquals("#text", nodeList.item(14).getNodeName());
    Assert.assertEquals("#text", nodeList.item(15).getNodeName());
    Assert.assertEquals("#text", nodeList.item(16).getNodeName());
    Assert.assertEquals("#text", nodeList.item(17).getNodeName());
    Assert.assertEquals("#text", nodeList.item(18).getNodeName());
    Assert.assertEquals("#text", nodeList.item(19).getNodeName());
  }

  @Test
  @Ignore
  public void axes113() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes113.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("", doc, XPathConstants.NODESET);
    generateAsserts(nodeList);
  }

  @Test
  public void axes114() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes114.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate(
            "//baz/preceding::foo[1]/@att1 | (//baz/preceding::foo)[1]/@att1", doc, XPathConstants.NODESET);
    Assert.assertEquals(2, nodeList.getLength());
    Assert.assertEquals("c", nodeList.item(0).getNodeValue());
    Assert.assertEquals("a", nodeList.item(1).getNodeValue());
  }

  @Test
  public void axes115() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes115.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate(
            "//baz/preceding-sibling::foo[1]/@att1 | (//baz/preceding-sibling::foo)[1]/@att1", doc,
            XPathConstants.NODESET);
    Assert.assertEquals(2, nodeList.getLength());
    Assert.assertEquals("c", nodeList.item(0).getNodeValue());
    Assert.assertEquals("a", nodeList.item(1).getNodeValue());
  }

  @Test
  @Ignore
  public void axes116() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes116.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("", doc, XPathConstants.NODESET);
    generateAsserts(nodeList);
  }

  @Test
  public void axes117() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes117.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    Assert.assertEquals(16., xpath.evaluate("count(//@*)", doc, XPathConstants.NUMBER));
    Assert.assertEquals(12., xpath.evaluate("count(//@title)", doc, XPathConstants.NUMBER));
    Assert.assertEquals(14., xpath.evaluate("count(//section//@*)", doc, XPathConstants.NUMBER));
    Assert.assertEquals(11., xpath.evaluate("count(//section//@title)", doc, XPathConstants.NUMBER));
    Assert.assertEquals(16., xpath.evaluate("count(chapter//@*)", doc, XPathConstants.NUMBER));
    Assert.assertEquals(12., xpath.evaluate("count(chapter//@title)", doc, XPathConstants.NUMBER));
    Assert.assertEquals(5., xpath.evaluate("count(chapter/section[1]//@*)", doc, XPathConstants.NUMBER));
    Assert.assertEquals(3., xpath.evaluate("count(chapter/section[1]//@title)", doc, XPathConstants.NUMBER));
    Assert.assertEquals(4., xpath.evaluate("count(chapter/section[2]//@*)", doc, XPathConstants.NUMBER));
    Assert.assertEquals(4., xpath.evaluate("count(chapter/section[2]//@title)", doc, XPathConstants.NUMBER));
    Assert.assertEquals(5., xpath.evaluate("count(chapter/section[3]//@*)", doc, XPathConstants.NUMBER));
    Assert.assertEquals(4., xpath.evaluate("count(chapter/section[3]//@title)", doc, XPathConstants.NUMBER));
  }

  @Test
  public void axes119() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes119.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("/far-north/north/near-north/center/ancestor-or-self::*", doc,
            XPathConstants.NODESET);
    Assert.assertEquals(4, nodeList.getLength());
    Assert.assertEquals("far-north", nodeList.item(0).getNodeName());
    Assert.assertEquals("north", nodeList.item(1).getNodeName());
    Assert.assertEquals("near-north", nodeList.item(2).getNodeName());
    Assert.assertEquals("center", nodeList.item(3).getNodeName());
  }

  @Test
  @Ignore
  public void axes120() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes120.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("", doc, XPathConstants.NODESET);
    generateAsserts(nodeList);
  }

  @Test
  public void axes121() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes121.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("/descendant::*", doc, XPathConstants.NODESET);
    Assert.assertEquals(13, nodeList.getLength());
    Assert.assertEquals("far-north", nodeList.item(0).getNodeName());
    Assert.assertEquals("north", nodeList.item(1).getNodeName());
    Assert.assertEquals("near-north", nodeList.item(2).getNodeName());
    Assert.assertEquals("far-west", nodeList.item(3).getNodeName());
    Assert.assertEquals("west", nodeList.item(4).getNodeName());
    Assert.assertEquals("near-west", nodeList.item(5).getNodeName());
    Assert.assertEquals("center", nodeList.item(6).getNodeName());
    Assert.assertEquals("near-south", nodeList.item(7).getNodeName());
    Assert.assertEquals("south", nodeList.item(8).getNodeName());
    Assert.assertEquals("near-south-west", nodeList.item(9).getNodeName());
    Assert.assertEquals("near-east", nodeList.item(10).getNodeName());
    Assert.assertEquals("east", nodeList.item(11).getNodeName());
    Assert.assertEquals("far-east", nodeList.item(12).getNodeName());
  }

  @Test
  @Ignore
  public void axes122() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes122.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("", doc, XPathConstants.NODESET);
    generateAsserts(nodeList);
  }

  @Test
  public void axes123() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes123.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/@center-attr-1/../..", doc, XPathConstants.NODESET);
    Assert.assertEquals(1, nodeList.getLength());
    Assert.assertEquals("near-north", nodeList.item(0).getNodeName());
  }

  @Test
  public void axes124() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes124.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//center/comment()/../comment()", doc, XPathConstants.NODESET);
    Assert.assertEquals(1, nodeList.getLength());
    Assert.assertEquals("Center Comment", nodeList.item(0).getNodeValue());
  }

  @Test
  public void axes125() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes125.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//inner/preceding::node()", doc, XPathConstants.NODESET);
    Assert.assertEquals(4, nodeList.getLength());
    Assert.assertEquals("#text", nodeList.item(0).getNodeName());
    Assert.assertEquals("connection", nodeList.item(1).getNodeName());
    Assert.assertEquals("#text", nodeList.item(2).getNodeName());
    Assert.assertEquals("#text", nodeList.item(3).getNodeName());
    Assert.assertEquals("D\n  ", nodeList.item(0).getNodeValue());
    Assert.assertEquals(null, nodeList.item(1).getNodeValue());
    Assert.assertEquals("C\n  ", nodeList.item(2).getNodeValue());
    Assert.assertEquals("S\n    ", nodeList.item(3).getNodeValue());
  }

  @Test
  public void axes126() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes126.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//inner/preceding::node()", doc, XPathConstants.NODESET);
    Assert.assertEquals(12, nodeList.getLength());
    Assert.assertEquals("#text", nodeList.item(0).getNodeName());
    Assert.assertEquals("#comment", nodeList.item(1).getNodeName());
    Assert.assertEquals("#text", nodeList.item(2).getNodeName());
    Assert.assertEquals("connection", nodeList.item(3).getNodeName());
    Assert.assertEquals("#text", nodeList.item(4).getNodeName());
    Assert.assertEquals("a-pi", nodeList.item(5).getNodeName());
    Assert.assertEquals("#text", nodeList.item(6).getNodeName());
    Assert.assertEquals("#text", nodeList.item(7).getNodeName());
    Assert.assertEquals("#comment", nodeList.item(8).getNodeName());
    Assert.assertEquals("#text", nodeList.item(9).getNodeName());
    Assert.assertEquals("a-pi", nodeList.item(10).getNodeName());
    Assert.assertEquals("#text", nodeList.item(11).getNodeName());
    Assert.assertEquals("D\n  ", nodeList.item(0).getNodeValue());
    Assert.assertEquals(" Comment 1 ", nodeList.item(1).getNodeValue());
    Assert.assertEquals("\n  ", nodeList.item(2).getNodeValue());
    Assert.assertEquals(null, nodeList.item(3).getNodeValue());
    Assert.assertEquals("C\n  ", nodeList.item(4).getNodeValue());
    Assert.assertEquals("pi-1", nodeList.item(5).getNodeValue());
    Assert.assertEquals("\n  ", nodeList.item(6).getNodeValue());
    Assert.assertEquals("S\n    ", nodeList.item(7).getNodeValue());
    Assert.assertEquals(" Comment 2 ", nodeList.item(8).getNodeValue());
    Assert.assertEquals("\n    ", nodeList.item(9).getNodeValue());
    Assert.assertEquals("pi-2", nodeList.item(10).getNodeValue());
    Assert.assertEquals("\n    ", nodeList.item(11).getNodeValue());
  }

  @Test
  public void axes127() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes127.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//inner/following::node()", doc, XPathConstants.NODESET);
    Assert.assertEquals(4, nodeList.getLength());
    Assert.assertEquals("#text", nodeList.item(0).getNodeName());
    Assert.assertEquals("#text", nodeList.item(1).getNodeName());
    Assert.assertEquals("connection", nodeList.item(2).getNodeName());
    Assert.assertEquals("#text", nodeList.item(3).getNodeName());

    Assert.assertEquals("I\n  ", nodeList.item(0).getNodeValue());
    Assert.assertEquals("X\n  ", nodeList.item(1).getNodeValue());
    Assert.assertEquals(null, nodeList.item(2).getNodeValue());
    Assert.assertEquals("C\n", nodeList.item(3).getNodeValue());
  }

  @Test
  public void axes128() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes128.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("//inner/preceding::node()", doc, XPathConstants.NODESET);
    Assert.assertEquals(12, nodeList.getLength());
    Assert.assertEquals("#text", nodeList.item(0).getNodeName());
    Assert.assertEquals("#comment", nodeList.item(1).getNodeName());
    Assert.assertEquals("#text", nodeList.item(2).getNodeName());
    Assert.assertEquals("connection", nodeList.item(3).getNodeName());
    Assert.assertEquals("#text", nodeList.item(4).getNodeName());
    Assert.assertEquals("a-pi", nodeList.item(5).getNodeName());
    Assert.assertEquals("#text", nodeList.item(6).getNodeName());
    Assert.assertEquals("#text", nodeList.item(7).getNodeName());
    Assert.assertEquals("#comment", nodeList.item(8).getNodeName());
    Assert.assertEquals("#text", nodeList.item(9).getNodeName());
    Assert.assertEquals("a-pi", nodeList.item(10).getNodeName());
    Assert.assertEquals("#text", nodeList.item(11).getNodeName());

    Assert.assertEquals("D\n  ", nodeList.item(0).getNodeValue());
    Assert.assertEquals(" Comment 1 ", nodeList.item(1).getNodeValue());
    Assert.assertEquals("\n  ", nodeList.item(2).getNodeValue());
    Assert.assertEquals(null, nodeList.item(3).getNodeValue());
    Assert.assertEquals("C\n  ", nodeList.item(4).getNodeValue());
    Assert.assertEquals("pi-1", nodeList.item(5).getNodeValue());
    Assert.assertEquals("\n  ", nodeList.item(6).getNodeValue());
    Assert.assertEquals("S\n    ", nodeList.item(7).getNodeValue());
    Assert.assertEquals(" Comment 2 ", nodeList.item(8).getNodeValue());
    Assert.assertEquals("\n    ", nodeList.item(9).getNodeValue());
    Assert.assertEquals("pi-2", nodeList.item(10).getNodeValue());
    Assert.assertEquals("\n    ", nodeList.item(11).getNodeValue());
  }

  @Test
  @Ignore
  public void axes129() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes129.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("", doc, XPathConstants.NODESET);
    generateAsserts(nodeList);
  }

  @Test
  @Ignore
  public void axes130() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes130.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("", doc, XPathConstants.NODESET);
    generateAsserts(nodeList);
  }

  @Test
  @Ignore
  public void axes131() throws Exception {
    final InputSource xml = getInputSource(PACKAGE + "axes131.xml");
    final Document doc = XMLUnit.buildTestDocument(xml);
    final NodeList nodeList = (NodeList) xpath.evaluate("", doc, XPathConstants.NODESET);
    generateAsserts(nodeList);
  }

}
