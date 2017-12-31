package de.lyca.xslt.conf;

import static de.lyca.xslt.ResourceUtils.getSource;
import static de.lyca.xslt.ResourceUtils.readResource;
import static java.lang.System.lineSeparator;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.lyca.xslt.Transform;

@RunWith(Parameterized.class)
public class ConfMessageTests {

  private static final String PACKAGE = '/' + ConfMessageTests.class.getPackage().getName().replace('.', '/')
      + "/message/";

  @Parameters(name = "{0}")
  public static Collection<Object[]> params() {
    Collection<Object[]> result = new ArrayList<>();
    for (int i = 1; i < 17; i++) {
      result
          .add(new Object[] { String.format("message%02d", i), EXPECTED_ERRORS[i - 1][0], EXPECTED_ERRORS[i - 1][1] });
    }
    return result;
  }

  private String name;
  private String firstExpectedError;
  private String lastExpectedError;

  public ConfMessageTests(String name, String firstExpectedError, String lastExpectedError) {
    this.name = PACKAGE + name;
    this.firstExpectedError = firstExpectedError;
    this.lastExpectedError = lastExpectedError;
  }

  @Test
  public void confMessageTests() throws Exception {
    final Source xsl = getSource(name + ".xsl");
    final Source xml = getSource(name + ".xml");
    final String expected = readResource(name + ".out", UTF_8);
    final Transform t = new Transform(xml, xsl);
    final MessageListener messageListener = new MessageListener();
    t.setErrorListener(messageListener);
    Assert.assertEquals(expected, t.getResultString());
    Assert.assertEquals(firstExpectedError, messageListener.getFirstMessage());
    Assert.assertEquals(lastExpectedError == null ? firstExpectedError : lastExpectedError,
        messageListener.getLastMessage());
  }

  private static final String[][] EXPECTED_ERRORS = { //
      { "This message came from the MESSAGE01 test.", null }, //
      { "This message came from the MESSAGE02 test.", null }, //
      { "Message from MESSAGE03", null }, //
      { "Message from MESSAGE04: XYZ", null }, //
      { "Message from MESSAGE05: <a>X</a>", null }, //
      { "Message from MESSAGE06: condition true", null }, //
      { "Message from MESSAGE07: X" + lineSeparator(), null }, //
      { "Message from MESSAGE08: X" + lineSeparator(), null }, //
      { "This is a bulletin!", "Message from MESSAGE09: This is the original message" }, //
      { "Message from MESSAGE10: <how when=\"now\">X</how>", null }, //
      { "Message from MESSAGE11: <!--X-->Anything between the colon and this?", null }, //
      { "Message from MESSAGE12: <?junk X?>Anything between the colon and this?", null }, //
      { "Message from MESSAGE13: <a/>Anything between the colon and this?", null }, //
      { "Message from MESSAGE14: (17) Anything between the colon and this?", null }, //
      { "Message from MESSAGE15: We got inside", null }, //
      { "This message came from the MESSAGE16 test.", null } //
  };

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
