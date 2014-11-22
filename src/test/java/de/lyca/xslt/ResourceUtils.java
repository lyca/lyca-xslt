package de.lyca.xslt;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.InputSource;

public class ResourceUtils {

  private ResourceUtils() {
    // only static utility methods
  }

  public static URI getResourceURI(String resource) throws URISyntaxException {
    final URL url = ResourceUtils.class.getResource(resource);
    if (url == null)
      throw new ResourceNotFoundException(resource);
    return url.toURI();
  }

  public static Path getResourcePath(String resource) throws URISyntaxException {
    return Paths.get(getResourceURI(resource));
  }

  public static File getResourceFile(String resource) throws URISyntaxException {
    return getResourcePath(resource).toFile();
  }

  public static String readResource(String resource, Charset encoding) throws IOException, URISyntaxException {
    final byte[] encoded = Files.readAllBytes(getResourcePath(resource));
    return encoding.decode(ByteBuffer.wrap(encoded)).toString();
  }

  public static Source getSource(String resource) throws URISyntaxException {
    return new StreamSource(getSystemID(resource));
  }

  public static String getSystemID(String resource) throws URISyntaxException {
    return getResourceURI(resource).toASCIIString();
  }

  public static InputSource getInputSource(String resource) throws URISyntaxException {
    return new InputSource(getSystemID(resource));
  }

  private static class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource) {
      super("Resource '" + resource + "' not found");
    }
  }
}
