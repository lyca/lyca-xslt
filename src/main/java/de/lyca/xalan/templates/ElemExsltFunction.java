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
/*
 * $Id$
 */
package de.lyca.xalan.templates;

import javax.xml.transform.TransformerException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.lyca.xalan.extensions.ExtensionNamespaceSupport;
import de.lyca.xalan.transformer.TransformerImpl;
import de.lyca.xpath.VariableStack;
import de.lyca.xpath.XPathContext;
import de.lyca.xpath.objects.XObject;

/**
 * Implement func:function.
 * 
 * @xsl.usage advanced
 */
public class ElemExsltFunction extends ElemTemplate {
  static final long serialVersionUID = 272154954793534771L;

  /**
   * Get an integer representation of the element type.
   * 
   * @return An integer representation of the element, defined in the Constants
   *         class.
   * @see de.lyca.xalan.templates.Constants
   */
  @Override
  public int getXSLToken() {
    return Constants.EXSLT_ELEMNAME_FUNCTION;
  }

  /**
   * Return the node name, defined in the Constants class.
   * 
   * @see de.lyca.xalan.templates.Constants
   * @return The node name
   * 
   */
  @Override
  public String getNodeName() {
    return Constants.EXSLT_ELEMNAME_FUNCTION_STRING;
  }

  public void execute(TransformerImpl transformer, XObject[] args) throws TransformerException {
    final XPathContext xctxt = transformer.getXPathContext();
    final VariableStack vars = xctxt.getVarStack();

    // Increment the frame bottom of the variable stack by the
    // frame size
    final int thisFrame = vars.getStackFrame();
    final int nextFrame = vars.link(m_frameSize);

    if (m_inArgsSize < args.length)
      throw new TransformerException("function called with too many args");

    // Set parameters,
    // have to clear the section of the stack frame that has params.
    if (m_inArgsSize > 0) {
      vars.clearLocalSlots(0, m_inArgsSize);

      if (args.length > 0) {
        vars.setStackFrame(thisFrame);
        final NodeList children = this.getChildNodes();

        for (int i = 0; i < args.length; i++) {
          final Node child = children.item(i);
          if (children.item(i) instanceof ElemParam) {
            final ElemParam param = (ElemParam) children.item(i);
            vars.setLocalVariable(param.getIndex(), args[i], nextFrame);
          }
        }

        vars.setStackFrame(nextFrame);
      }
    }

    // Removed ElemTemplate 'push' and 'pop' of RTFContext, in order to avoid
    // losing the RTF context
    // before a value can be returned. ElemExsltFunction operates in the scope
    // of the template that called
    // the function.
    // xctxt.pushRTFContext();

    if (transformer.getDebug()) {
      transformer.getTraceManager().fireTraceEvent(this);
    }

    vars.setStackFrame(nextFrame);
    transformer.executeChildTemplates(this, true);

    // Reset the stack frame after the function call
    vars.unlink(thisFrame);

    if (transformer.getDebug()) {
      transformer.getTraceManager().fireTraceEndEvent(this);
    }

    // Following ElemTemplate 'pop' removed -- see above.
    // xctxt.popRTFContext();

  }

  /**
   * Called after everything else has been recomposed, and allows the function
   * to set remaining values that may be based on some other property that
   * depends on recomposition.
   */
  @Override
  public void compose(StylesheetRoot sroot) throws TransformerException {
    super.compose(sroot);

    // Register the function namespace (if not already registered).
    String namespace = getName().getNamespace();
    final String handlerClass = sroot.getExtensionHandlerClass();
    Object[] args = { namespace, sroot };
    ExtensionNamespaceSupport extNsSpt = new ExtensionNamespaceSupport(namespace, handlerClass, args);
    sroot.getExtensionNamespacesManager().registerExtension(extNsSpt);
    // Make sure there is a handler for the EXSLT functions namespace
    // -- for isElementAvailable().
    if (!namespace.equals(de.lyca.xml.utils.Constants.S_EXSLT_FUNCTIONS_URL)) {
      namespace = de.lyca.xml.utils.Constants.S_EXSLT_FUNCTIONS_URL;
      args = new Object[] { namespace, sroot };
      extNsSpt = new ExtensionNamespaceSupport(namespace, handlerClass, args);
      sroot.getExtensionNamespacesManager().registerExtension(extNsSpt);
    }
  }
}