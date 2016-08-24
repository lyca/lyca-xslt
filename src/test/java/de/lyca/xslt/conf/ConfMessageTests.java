package de.lyca.xslt.conf;

import static de.lyca.xslt.ResourceUtils.getSource;
import static de.lyca.xslt.ResourceUtils.readResource;
import static java.lang.System.lineSeparator;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

import org.custommonkey.xmlunit.Transform;
import org.junit.Assert;
import org.junit.Test;

//@RunWith(Parameterized.class)
public class ConfMessageTests {

  private static final String PACKAGE = '/' + ConfMessageTests.class.getPackage().getName().replace('.', '/')
      + "/message/";

  // @Parameters(name = "{0}")
  // public static Collection<Object> params() {
  // Collection<Object> result = new ArrayList<>();
  // for (int i = 1; i < 17; i++) {
  // result.add(String.format("message%02d", i));
  // }
  // return result;
  // }
  //
  // private String name;
  //
  // public ConfMessageTests(String name) {
  // this.name = PACKAGE + name;
  // }
  //
  // @Test
  // public void confMessageTests() throws Exception {
  // final Source xsl = getSource(name + ".xsl");
  // final Source xml = getSource(name + ".xml");
  // final String expected = readResource(name + ".out", UTF_8);
  // final Transform t = new Transform(xml, xsl);
  // Assert.assertEquals(expected, t.getResultString());
  // }

  @Test
  public void message01() throws Exception {
    final String name = PACKAGE + "message01";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    final MessageListener messageListener = new MessageListener();
    t.setErrorListener(messageListener);
    Assert.assertEquals(expected, t.getResultString());
    Assert.assertEquals("This message came from the MESSAGE01 test.", messageListener.getFirstMessage());
  }

  @Test
  public void message02() throws Exception {
    final String name = PACKAGE + "message02";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    final MessageListener messageListener = new MessageListener();
    t.setErrorListener(messageListener);
    Assert.assertEquals(expected, t.getResultString());
    Assert.assertEquals("This message came from the MESSAGE02 test.", messageListener.getFirstMessage());

  }

  @Test
  public void message03() throws Exception {
    final String name = PACKAGE + "message03";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    final MessageListener messageListener = new MessageListener();
    t.setErrorListener(messageListener);
    Assert.assertEquals(expected, t.getResultString());
    Assert.assertEquals("Message from MESSAGE03", messageListener.getFirstMessage());
  }

  @Test
  public void message04() throws Exception {
    final String name = PACKAGE + "message04";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    final MessageListener messageListener = new MessageListener();
    t.setErrorListener(messageListener);
    Assert.assertEquals(expected, t.getResultString());
    Assert.assertEquals("Message from MESSAGE04: XYZ", messageListener.getFirstMessage());
  }

  @Test
  public void message05() throws Exception {
    final String name = PACKAGE + "message05";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    final MessageListener messageListener = new MessageListener();
    t.setErrorListener(messageListener);
    Assert.assertEquals(expected, t.getResultString());
    Assert.assertEquals("Message from MESSAGE05: <a>X</a>", messageListener.getFirstMessage());
  }

  @Test
  public void message06() throws Exception {
    final String name = PACKAGE + "message06";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    final MessageListener messageListener = new MessageListener();
    t.setErrorListener(messageListener);
    Assert.assertEquals(expected, t.getResultString());
    Assert.assertEquals("Message from MESSAGE06: condition true", messageListener.getFirstMessage());
  }

  @Test
  public void message07() throws Exception {
    final String name = PACKAGE + "message07";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    final MessageListener messageListener = new MessageListener();
    t.setErrorListener(messageListener);
    Assert.assertEquals(expected, t.getResultString());
    Assert.assertEquals("Message from MESSAGE07: X" + lineSeparator(), messageListener.getFirstMessage());
  }

  @Test
  public void message08() throws Exception {
    final String name = PACKAGE + "message08";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    final MessageListener messageListener = new MessageListener();
    t.setErrorListener(messageListener);
    Assert.assertEquals(expected, t.getResultString());
    Assert.assertEquals("Message from MESSAGE08: X" + lineSeparator(), messageListener.getFirstMessage());
  }

  @Test
  public void message09() throws Exception {
    final String name = PACKAGE + "message09";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    final MessageListener messageListener = new MessageListener();
    t.setErrorListener(messageListener);
    Assert.assertEquals(expected, t.getResultString());
    Assert.assertEquals("This is a bulletin!", messageListener.getFirstMessage());
    Assert.assertEquals("Message from MESSAGE09: This is the original message", messageListener.getLastMessage());
  }

  @Test
  public void message10() throws Exception {
    final String name = PACKAGE + "message10";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    final MessageListener messageListener = new MessageListener();
    t.setErrorListener(messageListener);
    Assert.assertEquals(expected, t.getResultString());
    Assert.assertEquals("Message from MESSAGE10: <how when=\"now\">X</how>", messageListener.getFirstMessage());
  }

  @Test
  public void message11() throws Exception {
    final String name = PACKAGE + "message11";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    final MessageListener messageListener = new MessageListener();
    t.setErrorListener(messageListener);
    Assert.assertEquals(expected, t.getResultString());
    Assert.assertEquals("Message from MESSAGE11: <!--X-->Anything between the colon and this?",
        messageListener.getFirstMessage());
  }

  @Test
  public void message12() throws Exception {
    final String name = PACKAGE + "message12";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    final MessageListener messageListener = new MessageListener();
    t.setErrorListener(messageListener);
    Assert.assertEquals(expected, t.getResultString());
    Assert.assertEquals("Message from MESSAGE12: <?junk X?>Anything between the colon and this?",
        messageListener.getFirstMessage());
  }

  @Test
  public void message13() throws Exception {
    final String name = PACKAGE + "message13";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    final MessageListener messageListener = new MessageListener();
    t.setErrorListener(messageListener);
    Assert.assertEquals(expected, t.getResultString());
    Assert.assertEquals("Message from MESSAGE13: <a/>Anything between the colon and this?",
        messageListener.getFirstMessage());
  }

  @Test
  public void message14() throws Exception {
    final String name = PACKAGE + "message14";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    final MessageListener messageListener = new MessageListener();
    t.setErrorListener(messageListener);
    Assert.assertEquals(expected, t.getResultString());
    Assert.assertEquals("Message from MESSAGE14: (17) Anything between the colon and this?",
        messageListener.getFirstMessage());
  }

  @Test
  public void message15() throws Exception {
    final String name = PACKAGE + "message15";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    final MessageListener messageListener = new MessageListener();
    t.setErrorListener(messageListener);
    Assert.assertEquals(expected, t.getResultString());
    Assert.assertEquals("Message from MESSAGE15: We got inside", messageListener.getFirstMessage());
  }

  @Test
  public void message16() throws Exception {
    final String name = PACKAGE + "message16";
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    final MessageListener messageListener = new MessageListener();
    t.setErrorListener(messageListener);
    Assert.assertEquals(expected, t.getResultString());
    Assert.assertEquals("This message came from the MESSAGE16 test.", messageListener.getFirstMessage());
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
