package de.lyca.xslt.api.err;

import static de.lyca.xslt.ResourceUtils.getSource;
import static de.lyca.xslt.ResourceUtils.readResource;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

import org.junit.Assert;
import org.junit.Test;

import de.lyca.xslt.Transform;

public class ApiErrTests {

  private static final String PACKAGE = '/' + ApiErrTests.class.getPackage().getName().replace('.', '/') + '/';

  @Test
  public void errorListenerTest() throws Exception {
    final String name = PACKAGE + "ErrorListenerTest";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    final AssertErrorListener errorListener = new AssertErrorListener();
    t.setErrorListener(errorListener);
    Assert.assertEquals(expected, t.getResultString());
    Assert.assertEquals("ExpectedMessage from:list1", errorListener.getWarnings().first());
    Assert.assertEquals("ExpectedMessage from:list2", errorListener.getWarnings().last());
  }

  private static class AssertErrorListener implements ErrorListener {

    private final SortedSet<String> warnings = new TreeSet<String>();

    public SortedSet<String> getWarnings() {
      return warnings;
    }

    @Override
    public void warning(TransformerException exception) throws TransformerException {
      warnings.add(exception.getMessage());
    }

    @Override
    public void error(TransformerException exception) throws TransformerException {
      System.err.println(exception.getLocalizedMessage());
      throw exception;
    }

    @Override
    public void fatalError(TransformerException exception) throws TransformerException {
      System.err.println(exception.getLocalizedMessage());
      throw exception;
    }

  }

}
