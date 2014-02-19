package de.lyca.xpath;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.xml.sax.InputSource;

public class ResourceUtils {

  private ResourceUtils() {
    // only static utility methods
  }

  public static String readResource(String resource, Charset encoding) throws IOException, URISyntaxException {
    final byte[] encoded = Files.readAllBytes(Paths.get(ResourceUtils.class.getResource(resource).toURI()));
    return encoding.decode(ByteBuffer.wrap(encoded)).toString();
  }

  public static InputSource getInputSource(String resource) throws URISyntaxException {
    return new InputSource(ResourceUtils.class.getResource(resource).toURI().toASCIIString());
  }

}
