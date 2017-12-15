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
package de.lyca.xpath.jaxp;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathVariableResolver;

import de.lyca.xml.utils.QName;
import de.lyca.xpath.VariableStack;
import de.lyca.xpath.XPathContext;
import de.lyca.xpath.objects.XObject;
import de.lyca.xpath.res.XPATHErrorResources;
import de.lyca.xpath.res.XPATHMessages;

/**
 * Overrides {@link VariableStack} and delegates the call to
 * {@link javax.xml.xpath.XPathVariableResolver}.
 * 
 * @author Ramesh Mandava ( ramesh.mandava@sun.com )
 */
public class JAXPVariableStack extends VariableStack {

  private final XPathVariableResolver resolver;

  public JAXPVariableStack(XPathVariableResolver resolver) {
    super(2);
    this.resolver = resolver;
  }

  @Override
  public XObject getVariableOrParam(XPathContext xctxt, QName qname) throws TransformerException,
          IllegalArgumentException {
    if (qname == null) {
      // JAXP 1.3 spec says that if variable name is null then
      // we need to through IllegalArgumentException
      final String fmsg = XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_ARG_CANNOT_BE_NULL,
              new Object[] { "Variable qname" });
      throw new IllegalArgumentException(fmsg);
    }
    final javax.xml.namespace.QName name = new javax.xml.namespace.QName(qname.getNamespace(), qname.getLocalPart());
    final Object varValue = resolver.resolveVariable(name);
    if (varValue == null) {
      final String fmsg = XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_RESOLVE_VARIABLE_RETURNS_NULL,
              new Object[] { name.toString() });
      throw new TransformerException(fmsg);
    }
    return XObject.create(varValue, xctxt);
  }

}
