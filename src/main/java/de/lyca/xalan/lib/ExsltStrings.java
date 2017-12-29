/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the  "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.lyca.xalan.lib;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import de.lyca.xpath.NodeSet;

/**
 * This class contains EXSLT strings extension functions.
 * 
 * It is accessed by specifying a namespace URI as follows:
 * 
 * <pre>
 *    xmlns:str="http://exslt.org/strings"
 * </pre>
 * 
 * The documentation for each function has been copied from the relevant EXSLT
 * Implementer page.
 * 
 * @see <a href="http://www.exslt.org/">EXSLT</a>
 * 
 * @xsl.usage general
 */
public class ExsltStrings extends ExsltBase {
  /**
   * The str:align function aligns a string within another string.
   * <p>
   * The first argument gives the target string to be aligned. The second
   * argument gives the padding string within which it is to be aligned.
   * <p>
   * If the target string is shorter than the padding string then a range of
   * characters in the padding string are repaced with those in the target
   * string. Which characters are replaced depends on the value of the third
   * argument, which gives the type of alignment. It can be one of 'left',
   * 'right' or 'center'. If no third argument is given or if it is not one of
   * these values, then it defaults to left alignment.
   * <p>
   * With left alignment, the range of characters replaced by the target string
   * begins with the first character in the padding string. With right
   * alignment, the range of characters replaced by the target string ends with
   * the last character in the padding string. With center alignment, the range
   * of characters replaced by the target string is in the middle of the padding
   * string, such that either the number of unreplaced characters on either side
   * of the range is the same or there is one less on the left than there is on
   * the right.
   * <p>
   * If the target string is longer than the padding string, then it is
   * truncated to be the same length as the padding string and returned.
   * 
   * @param targetStr
   *          The target string
   * @param paddingStr
   *          The padding string
   * @param type
   *          The type of alignment
   * 
   * @return The string after alignment
   */
  public static String align(String targetStr, String paddingStr, String type) {
    if (targetStr.length() >= paddingStr.length())
      return targetStr.substring(0, paddingStr.length());

    if (type.equals("right"))
      return paddingStr.substring(0, paddingStr.length() - targetStr.length()) + targetStr;
    else if (type.equals("center")) {
      final int startIndex = (paddingStr.length() - targetStr.length()) / 2;
      return paddingStr.substring(0, startIndex) + targetStr + paddingStr.substring(startIndex + targetStr.length());
    }
    // Default is left
    else
      return targetStr + paddingStr.substring(targetStr.length());
  }

  /**
   * See above
   */
  public static String align(String targetStr, String paddingStr) {
    return align(targetStr, paddingStr, "left");
  }

  /**
   * The str:concat function takes a node set and returns the concatenation of
   * the string values of the nodes in that node set. If the node set is empty,
   * it returns an empty string.
   * 
   * @param nl
   *          A node set
   * @return The concatenation of the string values of the nodes in that node
   *         set
   */
  public static String concat(NodeList nl) {
    final StringBuilder sb = new StringBuilder();
    for (int i = 0; i < nl.getLength(); i++) {
      final Node node = nl.item(i);
      final String value = toString(node);

      if (value != null && value.length() > 0) {
        sb.append(value);
      }
    }

    return sb.toString();
  }

  /**
   * The str:padding function creates a padding string of a certain length. The
   * first argument gives the length of the padding string to be created. The
   * second argument gives a string to be used to create the padding. This
   * string is repeated as many times as is necessary to create a string of the
   * length specified by the first argument; if the string is more than a
   * character long, it may have to be truncated to produce the required length.
   * If no second argument is specified, it defaults to a space (' '). If the
   * second argument is an empty string, str:padding returns an empty string.
   * 
   * @param length
   *          The length of the padding string to be created
   * @param pattern
   *          The string to be used as pattern
   * 
   * @return A padding string of the given length
   */
  public static String padding(double length, String pattern) {
    if (pattern == null || pattern.length() == 0)
      return "";

    final StringBuilder sb = new StringBuilder();
    final int len = (int) length;
    int numAdded = 0;
    int index = 0;
    while (numAdded < len) {
      if (index == pattern.length()) {
        index = 0;
      }

      sb.append(pattern.charAt(index));
      index++;
      numAdded++;
    }

    return sb.toString();
  }

  /**
   * See above
   */
  public static String padding(double length) {
    return padding(length, " ");
  }

  /**
   * The str:split function splits up a string and returns a node set of token
   * elements, each containing one token from the string.
   * <p>
   * The first argument is the string to be split. The second argument is a
   * pattern string. The string given by the first argument is split at any
   * occurrence of this pattern. For example:
   * 
   * <pre>
   * str:split('a, simple, list', ', ') gives the node set consisting of: 
   * 
   * <token>a</token>
   * <token>simple</token>
   * <token>list</token>
   * </pre>
   * 
   * If the second argument is omitted, the default is the string '&#x20;' (i.e.
   * a space).
   * 
   * @param str
   *          The string to be split
   * @param pattern
   *          The pattern
   * 
   * @return A node set of split tokens
   */
  public static NodeList split(String str, String pattern) {

    final NodeSet resultSet = new NodeSet();
    resultSet.setShouldCacheNodes(true);

    boolean done = false;
    int fromIndex = 0;
    int matchIndex = 0;
    String token = null;

    while (!done && fromIndex < str.length()) {
      matchIndex = str.indexOf(pattern, fromIndex);
      if (matchIndex >= 0) {
        token = str.substring(fromIndex, matchIndex);
        fromIndex = matchIndex + pattern.length();
      } else {
        done = true;
        token = str.substring(fromIndex);
      }

      final Document doc = DocumentHolder.m_doc;
      synchronized (doc) {
        final Element element = doc.createElement("token");
        final Text text = doc.createTextNode(token);
        element.appendChild(text);
        resultSet.addNode(element);
      }
    }

    return resultSet;
  }

  /**
   * See above
   */
  public static NodeList split(String str) {
    return split(str, " ");
  }

  /**
   * The str:tokenize function splits up a string and returns a node set of
   * token elements, each containing one token from the string.
   * <p>
   * The first argument is the string to be tokenized. The second argument is a
   * string consisting of a number of characters. Each character in this string
   * is taken as a delimiting character. The string given by the first argument
   * is split at any occurrence of any of these characters. For example:
   * 
   * <pre>
   * str:tokenize('2001-06-03T11:40:23', '-T:') gives the node set consisting of: 
   * 
   * <token>2001</token>
   * <token>06</token>
   * <token>03</token>
   * <token>11</token>
   * <token>40</token>
   * <token>23</token>
   * </pre>
   * 
   * If the second argument is omitted, the default is the string
   * '&#x9;&#xA;&#xD;&#x20;' (i.e. whitespace characters).
   * <p>
   * If the second argument is an empty string, the function returns a set of
   * token elements, each of which holds a single character.
   * <p>
   * Note: This one is different from the tokenize extension function in the
   * Xalan namespace. The one in Xalan returns a set of Text nodes, while this
   * one wraps the Text nodes inside the token Element nodes.
   * 
   * @param toTokenize
   *          The string to be tokenized
   * @param delims
   *          The delimiter string
   * 
   * @return A node set of split token elements
   */
  public static NodeList tokenize(String toTokenize, String delims) {

    final NodeSet resultSet = new NodeSet();

    if (delims != null && delims.length() > 0) {
      final StringTokenizer lTokenizer = new StringTokenizer(toTokenize, delims);

      final Document doc = DocumentHolder.m_doc;
      synchronized (doc) {
        while (lTokenizer.hasMoreTokens()) {
          final Element element = doc.createElement("token");
          element.appendChild(doc.createTextNode(lTokenizer.nextToken()));
          resultSet.addNode(element);
        }
      }
    }
    // If the delimiter is an empty string, create one token Element for
    // every single character.
    else {

      final Document doc = DocumentHolder.m_doc;
      synchronized (doc) {
        for (int i = 0; i < toTokenize.length(); i++) {
          final Element element = doc.createElement("token");
          element.appendChild(doc.createTextNode(toTokenize.substring(i, i + 1)));
          resultSet.addNode(element);
        }
      }
    }

    return resultSet;
  }

  /**
   * See above
   */
  public static NodeList tokenize(String toTokenize) {
    return tokenize(toTokenize, " \t\n\r");
  }

  public static String decodeUri(String uri) {
    return decode(uri, StandardCharsets.UTF_8);
  }

  public static String decodeUri(String uri, String encoding) {
    try {
      return decode(uri, Charset.forName(encoding));
    } catch (Exception e) {
      return "";
    }
  }

  // Idea copied from private java.net.URI#decode. Adjusted to work as specified
  // by EXSLT
  private static String decode(String s, Charset encoding) {
    if (s == null || s.isEmpty() || s.indexOf('%') < 0)
      return s;

    int n = s.length();
    StringBuilder sb = new StringBuilder(n);
    ByteBuffer bb = ByteBuffer.allocate(n);
    CharBuffer cb = CharBuffer.allocate(n);
    CharsetDecoder dec = encoding.newDecoder().onMalformedInput(CodingErrorAction.REPLACE)
        .onUnmappableCharacter(CodingErrorAction.REPLACE);

    // This is not horribly efficient, but it will do for now
    char c = s.charAt(0);
    boolean betweenBrackets = false;

    for (int i = 0; i < n;) {
      assert c == s.charAt(i); // Loop invariant
      if (c == '[') {
        betweenBrackets = true;
      } else if (betweenBrackets && c == ']') {
        betweenBrackets = false;
      }
      if (c != '%' || betweenBrackets) {
        sb.append(c);
        if (++i >= n)
          break;
        c = s.charAt(i);
        continue;
      }
      bb.clear();
      for (;;) {
        assert (n - i >= 2);
        bb.put(decode(s.charAt(++i), s.charAt(++i)));
        if (++i >= n)
          break;
        c = s.charAt(i);
        if (c != '%')
          break;
      }
      bb.flip();
      cb.clear();
      dec.reset();
      CoderResult cr = dec.decode(bb, cb, true);
      assert cr.isUnderflow();
      cr = dec.flush(cb);
      assert cr.isUnderflow();
      sb.append(cb.flip().toString());
    }

    return sb.toString();
  }

  private static byte decode(char c1, char c2) {
    return (byte) (((decode(c1) & 0xf) << 4) | (decode(c2) & 0xf));
  }

  private static int decode(char c) {
    if ((c >= '0') && (c <= '9'))
      return c - '0';
    if ((c >= 'a') && (c <= 'f'))
      return c - 'a' + 10;
    if ((c >= 'A') && (c <= 'F'))
      return c - 'A' + 10;
    assert false;
    return -1;
  }

  public static String encodeUri(String uri, boolean encodeReserved) {
    return encode(uri, encodeReserved, StandardCharsets.UTF_8);
  }

  public static String encodeUri(String uri, boolean encodeReserved, String encoding) {
    try {
      return encode(uri, encodeReserved, Charset.forName(encoding));
    } catch (Exception e) {
      return "";
    }
  }

  // Idea copied from private java.net.URI#encode. Adjusted to work as specified
  // by EXSLT
  private static String encode(String s, boolean encodeReserved, Charset encoding) {
    if (s.isEmpty())
      return s;

    String ns = Normalizer.normalize(s, Normalizer.Form.NFC);
    ByteBuffer bb = null;
    bb = encoding.encode(ns);

    StringBuilder sb = new StringBuilder();
    while (bb.hasRemaining()) {
      int b = bb.get() & 0xff;
      if (Arrays.binarySearch(alphaNum, b) >= 0 || Arrays.binarySearch(marks, b) >= 0
          || !encodeReserved && Arrays.binarySearch(reserved, b) >= 0)
        sb.append((char) b);
      else if (encodeReserved && Arrays.binarySearch(reserved, b) >= 0)
        appendEscape(sb, (byte) b);
      else
        appendEscape(sb, (byte) b);
    }
    return sb.toString();
  }

  private final static int[] reserved = { //
      // '$', '&'. '+', ',', '/', ':', ':', '=', '?', '@'
      0x24, 0x26, 0x2b, 0x2c, 0x2f, 0x3a, 0x3b, 0x3d, 0x3f, 0x40 //
  };

  private final static int[] alphaNum = { //
      // '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
      0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, //
      // 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
      0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4a, 0x4b, 0x4c, 0x4d, 0x4e, //
      // 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
      0x4f, 0x50, 0x51, 0x52, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5a, //
      // 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
      0x61, 0x62, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6a, 0x6b, 0x6c, 0x6d, 0x6e, //
      // 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
      0x6f, 0x70, 0x71, 0x72, 0x73, 0x74, 0x75, 0x76, 0x77, 0x78, 0x79, 0x7a //
  };

  private final static int[] marks = { //
      // '!', '\'', '(', ')', '*', '-', '.', '_', '~'
      0x21, 0x27, 0x28, 0x29, 0x2a, 0x2d, 0x2e, 0x5f, 0x7e };

  private final static char[] hexDigits = { //
      '0', '1', '2', '3', '4', '5', '6', '7', //
      '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' //
  };

  private static void appendEscape(StringBuilder sb, byte b) {
    sb.append('%');
    sb.append(hexDigits[(b >> 4) & 0x0f]);
    sb.append(hexDigits[(b >> 0) & 0x0f]);
  }

  /**
   * This class is not loaded until first referenced (see Java Language
   * Specification by Gosling/Joy/Steele, section 12.4.1)
   * 
   * The static members are created when this class is first referenced, as a
   * lazy initialization not needing checking against null or any
   * synchronization.
   * 
   */
  private static class DocumentHolder {
    // Reuse the Document object to reduce memory usage.
    private static final Document m_doc;
    static {
      try {
        m_doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
      }

      catch (final ParserConfigurationException pce) {
        throw new de.lyca.xml.utils.WrappedRuntimeException(pce);
      }

    }
  }

}
