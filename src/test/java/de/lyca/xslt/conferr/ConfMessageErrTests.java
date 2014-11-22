package de.lyca.xslt.conferr;

import static de.lyca.xslt.ResourceUtils.getSource;

import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

import org.custommonkey.xmlunit.Transform;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class ConfMessageErrTests {

  private static final String PACKAGE = '/' + ConfMessageErrTests.class.getPackage().getName().replace('.', '/')
          + "/messageerr/";

  @Test
  @Ignore
  public void messageerr01() throws Exception {
    final String name = PACKAGE + "messageerr01";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
    final MessageListener messageListener = new MessageListener();
    t.setErrorListener(messageListener);
    t.getResultString();
    Assert.assertEquals("This message came from the MESSAGE01 test.", messageListener.getFirstMessage());
  }

  @Test
  @Ignore
  public void messageerr02() throws Exception {
    final String name = PACKAGE + "messageerr02";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
    final MessageListener messageListener = new MessageListener();
    t.setErrorListener(messageListener);
    t.getResultString();
    Assert.assertEquals("This message came from the MESSAGEerr02 test.", messageListener.getFirstMessage());

  }

  @Test
  @Ignore
  public void messageerr03() throws Exception {
    final String name = PACKAGE + "messageerr03";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final Transform t = new Transform(xml, xsl);
    final MessageListener messageListener = new MessageListener();
    t.setErrorListener(messageListener);
    t.getResultString();
    Assert.assertEquals("This message came from the MESSAGE03 test.", messageListener.getFirstMessage());
  }

  private static class MessageListener implements ErrorListener {

    private final List<String> messages = new ArrayList<>();

    public String getFirstMessage() {
      return messages.isEmpty() ? null : messages.get(0);
    }

    public String getLastMessage() {
      return messages.isEmpty() ? null : messages.get(messages.size() - 1);
    }

    @Override
    public void warning(TransformerException exception) throws TransformerException {
      messages.add(exception.getMessage());
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
