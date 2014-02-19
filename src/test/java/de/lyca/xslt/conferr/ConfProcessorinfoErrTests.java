package de.lyca.xslt.conferr;

import static de.lyca.xslt.ResourceUtils.getSource;

import javax.xml.transform.Source;

import org.custommonkey.xmlunit.Transform;
import org.junit.Ignore;
import org.junit.Test;

public class ConfProcessorinfoErrTests {

  private static final String PACKAGE = '/' + ConfProcessorinfoErrTests.class.getPackage().getName().replace('.', '/')
          + "/processorinfoerr/";

  @Test
  public void processorinfoerr01() throws Exception {
    final String name = PACKAGE + "processorinfoerr01";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  public void processorinfoerr02() throws Exception {
    final String name = PACKAGE + "processorinfoerr02";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  public void processorinfoerr03() throws Exception {
    final String name = PACKAGE + "processorinfoerr03";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  @Ignore
  public void processorinfoerr04() throws Exception {
    final String name = PACKAGE + "processorinfoerr04";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  @Ignore
  public void processorinfoerr05() throws Exception {
    final String name = PACKAGE + "processorinfoerr05";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

  @Test
  public void processorinfoerr06() throws Exception {
    final String name = PACKAGE + "processorinfoerr06";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
  }

}
