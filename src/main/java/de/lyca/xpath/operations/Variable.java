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
package de.lyca.xpath.operations;

import java.util.List;

import javax.xml.transform.TransformerException;

import de.lyca.xml.utils.QName;
import de.lyca.xpath.Expression;
import de.lyca.xpath.ExpressionOwner;
import de.lyca.xpath.XPathContext;
import de.lyca.xpath.XPathVisitor;
import de.lyca.xpath.axes.PathComponent;
import de.lyca.xpath.axes.WalkerFactory;
import de.lyca.xpath.objects.XNodeSet;
import de.lyca.xpath.objects.XObject;
import de.lyca.xpath.res.XPATHErrorResources;
import de.lyca.xpath.res.XPATHMessages;

/**
 * The variable reference expression executer.
 */
public class Variable extends Expression implements PathComponent {
  static final long serialVersionUID = -4334975375609297049L;
  /**
   * Tell if fixupVariables was called.
   * 
   * @serial
   */
  private boolean m_fixUpWasCalled = false;

  /**
   * The qualified name of the variable.
   * 
   * @serial
   */
  protected QName m_qname;

  /**
   * The index of the variable, which is either an absolute index to a global, or, if higher than the globals area, must
   * be adjusted by adding the offset to the current stack frame.
   */
  protected int m_index;

  /**
   * Set the index for the variable into the stack. For advanced use only. You must know what you are doing to use this.
   * 
   * @param index a global or local index.
   */
  public void setIndex(int index) {
    m_index = index;
  }

  /**
   * Set the index for the variable into the stack. For advanced use only.
   * 
   * @return index a global or local index.
   */
  public int getIndex() {
    return m_index;
  }

  /**
   * Set whether or not this is a global reference. For advanced use only.
   * 
   * @param isGlobal true if this should be a global variable reference.
   */
  public void setIsGlobal(boolean isGlobal) {
    m_isGlobal = isGlobal;
  }

  /**
   * Set the index for the variable into the stack. For advanced use only.
   * 
   * @return true if this should be a global variable reference.
   */
  public boolean getGlobal() {
    return m_isGlobal;
  }

  protected boolean m_isGlobal = false;

  /**
   * This function is used to fixup variables from QNames to stack frame indexes at stylesheet build time.
   * 
   * @param vars List of QNames that correspond to variables. This list should be searched backwards for the first
   *        qualified name that corresponds to the variable reference qname. The position of the QName in the list from
   *        the start of the list will be its position in the stack frame (but variables above the globalsTop value will
   *        need to be offset to the current stack frame).
   */
  @Override
  public void fixupVariables(List<QName> vars, int globalsSize) {
    m_fixUpWasCalled = true;

    for (int i = vars.size() - 1; i >= 0; i--) {
      final QName qn = vars.get(i);
      // System.out.println("qn: "+qn);
      if (qn.equals(m_qname)) {

        if (i < globalsSize) {
          m_isGlobal = true;
          m_index = i;
        } else {
          m_index = i - globalsSize;
        }

        return;
      }
    }

    final java.lang.String msg = XPATHMessages.createXPATHMessage(XPATHErrorResources.ER_COULD_NOT_FIND_VAR,
        new Object[] { m_qname.toString() });

    final TransformerException te = new TransformerException(msg, this);

    throw new de.lyca.xml.utils.WrappedRuntimeException(te);

  }

  /**
   * Set the qualified name of the variable.
   * 
   * @param qname Must be a non-null reference to a qualified name.
   */
  public void setQName(QName qname) {
    m_qname = qname;
  }

  /**
   * Get the qualified name of the variable.
   * 
   * @return A non-null reference to a qualified name.
   */
  public QName getQName() {
    return m_qname;
  }

  /**
   * Execute an expression in the XPath runtime context, and return the result of the expression.
   * 
   * 
   * @param xctxt The XPath runtime context.
   * 
   * @return The result of the expression in the form of a <code>XObject</code>.
   * 
   * @throws TransformerException if a runtime exception occurs.
   */
  @Override
  public XObject execute(XPathContext xctxt) throws TransformerException {
    return execute(xctxt, false);
  }

  /**
   * Dereference the variable, and return the reference value. Note that lazy evaluation will occur. If a variable
   * within scope is not found, a warning will be sent to the error listener, and an empty nodeset will be returned.
   * 
   * 
   * @param xctxt The runtime execution context.
   * 
   * @return The evaluated variable, or an empty nodeset if not found.
   * 
   * @throws TransformerException TODO
   */
  @Override
  public XObject execute(XPathContext xctxt, boolean destructiveOK) throws TransformerException {
    XObject result;
    // Is the variable fetched always the same?
    // XObject result = xctxt.getVariable(m_qname);
    if (m_fixUpWasCalled) {
      if (m_isGlobal) {
        result = xctxt.getVarStack().getGlobalVariable(xctxt, m_index, destructiveOK);
      } else {
        result = xctxt.getVarStack().getLocalVariable(xctxt, m_index, destructiveOK);
      }
    } else {
      result = xctxt.getVarStack().getVariableOrParam(xctxt, m_qname);
    }

    if (null == result) {
      // This should now never happen...
      warn(xctxt, XPATHErrorResources.WG_ILLEGAL_VARIABLE_REFERENCE, new Object[] { m_qname.getLocalPart() }); // "VariableReference
                                                                                                               // given
                                                                                                               // for
                                                                                                               // variable
                                                                                                               // out "+
      // (new RuntimeException()).printStackTrace();
      // error(xctxt, XPATHErrorResources.ER_COULDNOT_GET_VAR_NAMED,
      // new Object[]{ m_qname.getLocalPart() });
      // //"Could not get variable named "+varName);

      result = new XNodeSet(xctxt.getDTMManager());
    }

    return result;
    // }
    // else
    // {
    // // Hack city... big time. This is needed to evaluate xpaths from
    // extensions,
    // // pending some bright light going off in my head. Some sort of callback?
    // synchronized(this)
    // {
    // de.lyca.xalan.templates.ElemVariable vvar= getElemVariable();
    // if(null != vvar)
    // {
    // m_index = vvar.getIndex();
    // m_isGlobal = vvar.getIsTopLevel();
    // m_fixUpWasCalled = true;
    // return execute(xctxt);
    // }
    // }
    // throw new
    // javax.xml.transform.TransformerException(XSLMessages.createXPATHMessage(XPATHErrorResources.ER_VAR_NOT_RESOLVABLE,
    // new Object[]{m_qname.toString()}));
    // //"Variable not resolvable: "+m_qname);
    // }
  }

  /**
   * Tell if this expression returns a stable number that will not change during iterations within the expression. This
   * is used to determine if a proximity position predicate can indicate that no more searching has to occur.
   * 
   * 
   * @return true if the expression represents a stable number.
   */
  @Override
  public boolean isStableNumber() {
    return true;
  }

  /**
   * Get the analysis bits for this walker, as defined in the WalkerFactory.
   * 
   * @return One of WalkerFactory#BIT_DESCENDANT, etc.
   */
  @Override
  public int getAnalysisBits() {
    return WalkerFactory.BIT_FILTER;
  }

  /**
   * @see de.lyca.xpath.XPathVisitable#callVisitors(ExpressionOwner, XPathVisitor)
   */
  @Override
  public void callVisitors(ExpressionOwner owner, XPathVisitor visitor) {
    visitor.visitVariableRef(owner, this);
  }

  /**
   * @see Expression#deepEquals(Expression)
   */
  @Override
  public boolean deepEquals(Expression expr) {
    if (!isSameClass(expr))
      return false;

    if (!m_qname.equals(((Variable) expr).m_qname))
      return false;

    return true;
  }

  static final java.lang.String PSUEDOVARNAMESPACE = "http://xml.apache.org/xalan/psuedovar";

  /**
   * Tell if this is a psuedo variable reference, declared by Xalan instead of by the user.
   * 
   * @return TODO
   */
  public boolean isPsuedoVarRef() {
    final java.lang.String ns = m_qname.getNamespaceURI();
    if (null != ns && ns.equals(PSUEDOVARNAMESPACE)) {
      if (m_qname.getLocalName().startsWith("#"))
        return true;
    }
    return false;
  }

}
