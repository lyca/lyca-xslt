package de.lyca.xpath;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathVariableResolver;

import org.w3c.dom.Document;

public class XPathVariableResolverBuilder {

  private final Map<QName, Object> nameToValue = new HashMap<>();

  public XPathVariableResolver build() {
    return new VariableResolver(nameToValue);
  }

  public XPathVariableResolver build(Document doc) {
    return new VariableResolver(nameToValue);
  }

  public XPathVariableResolverBuilder var(String qname, Object value) {
    nameToValue.put(QName.valueOf(qname), value);
    return this;
  }

  private static class VariableResolver implements XPathVariableResolver {
    private final Map<QName, Object> nameToValue;

    public VariableResolver(Map<QName, Object> nameToValue) {
      this.nameToValue = new HashMap<>(nameToValue);
    }

    @Override
    public Object resolveVariable(QName variableName) {
      return nameToValue.get(variableName);
    }

  }
}
