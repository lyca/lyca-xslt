package de.lyca.xpath;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

public class NamespaceContextBuilder {

  private static final Set<String> DEFAULT_PREFIXES = new HashSet<>();
  private static final Map<String, String> DEFAULT_PREFIX_TO_NAMESPACE = new HashMap<>();
  private static final Map<String, Set<String>> DEFAULT_NAMESPACE_TO_PREFIXES = new HashMap<>();
  static {
    DEFAULT_PREFIXES.add(XMLConstants.XML_NS_PREFIX);
    DEFAULT_PREFIXES.add(XMLConstants.XMLNS_ATTRIBUTE);
    DEFAULT_PREFIX_TO_NAMESPACE.put(XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI);
    DEFAULT_PREFIX_TO_NAMESPACE.put(XMLConstants.XMLNS_ATTRIBUTE, XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
    DEFAULT_NAMESPACE_TO_PREFIXES.put(XMLConstants.XML_NS_URI,
            new HashSet<>(Arrays.asList(new String[] { XMLConstants.XML_NS_PREFIX })));
    DEFAULT_NAMESPACE_TO_PREFIXES.put(XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
            new HashSet<>(Arrays.asList(new String[] { XMLConstants.XMLNS_ATTRIBUTE })));
  }

  /** prefix to namespaceURI */
  private final Map<String, String> prefix2ns;
  /** namespaceURI to prefixes */
  private final Map<String, Set<String>> ns2prefixes;

  public NamespaceContextBuilder() {
    prefix2ns = new HashMap<>(DEFAULT_PREFIX_TO_NAMESPACE);
    ns2prefixes = new HashMap<>(DEFAULT_NAMESPACE_TO_PREFIXES);
  }

  public NamespaceContextBuilder add(String prefix, String namespaceURI) {
    if (DEFAULT_PREFIXES.contains(prefix))
      throw new IllegalArgumentException("Cannot override default prefix " + prefix);
    prefix2ns.put(prefix, namespaceURI);
    Set<String> prefixes = ns2prefixes.get(prefix);
    if (prefixes == null) {
      prefixes = new HashSet<>();
      ns2prefixes.put(namespaceURI, prefixes);
    }
    prefixes.add(prefix);
    return this;
  }

  public NamespaceContext build() {
    return new XPathNamespaceContext(prefix2ns, ns2prefixes);
  }

  private static class XPathNamespaceContext implements NamespaceContext {

    private static final Iterator<String> XML_NS_PREFIX_ITERATOR = Arrays.asList(
            new String[] { XMLConstants.XML_NS_PREFIX }).iterator();
    private static final Iterator<String> XMLS_ATTRIBUTE_ITERATOR = Arrays.asList(
            new String[] { XMLConstants.XMLNS_ATTRIBUTE }).iterator();

    /** prefix to namespaceURI */
    private final Map<String, String> prefix2ns;
    /** namespaceURI to prefixes */
    private final Map<String, Set<String>> ns2prefixes;

    public XPathNamespaceContext(Map<String, String> prefix2ns, Map<String, Set<String>> ns2prefixes) {
      this.prefix2ns = new HashMap<>(prefix2ns);
      this.ns2prefixes = new HashMap<>(ns2prefixes);
    }

    @Override
    public String getNamespaceURI(String prefix) {
      if (prefix == null)
        throw new IllegalArgumentException("prefix cannot be null.");
      if (prefix.isEmpty())
        return XMLConstants.NULL_NS_URI;
      if (XMLConstants.XML_NS_PREFIX.equals(prefix))
        return XMLConstants.XML_NS_URI;
      if (XMLConstants.XMLNS_ATTRIBUTE.equals(prefix))
        return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
      final String namespaceURI = prefix2ns.get(prefix);
      if (namespaceURI == null || namespaceURI.isEmpty())
        return XMLConstants.NULL_NS_URI;
      return namespaceURI;
    }

    @Override
    public String getPrefix(String namespaceURI) {
      if (namespaceURI == null)
        throw new IllegalArgumentException("namespaceURI cannot be null.");
      if (XMLConstants.XML_NS_URI.equals(namespaceURI))
        return XMLConstants.XML_NS_PREFIX;
      if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(namespaceURI))
        return XMLConstants.XMLNS_ATTRIBUTE;
      final Set<String> prefixes = ns2prefixes.get(namespaceURI);
      return prefixes == null || prefixes.isEmpty() ? "" : prefixes.iterator().next();
    }

    @Override
    public Iterator<String> getPrefixes(String namespaceURI) {
      if (namespaceURI == null)
        throw new IllegalArgumentException("namespaceURI cannot be null.");
      if (XMLConstants.XML_NS_URI.equals(namespaceURI))
        return XML_NS_PREFIX_ITERATOR;
      if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(namespaceURI))
        return XMLS_ATTRIBUTE_ITERATOR;
      final Set<String> prefixes = ns2prefixes.get(namespaceURI);
      return prefixes == null ? Collections.<String> emptyIterator() : prefixes.iterator();
    }

  }

}
