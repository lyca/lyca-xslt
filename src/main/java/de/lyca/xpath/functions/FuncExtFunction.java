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
package de.lyca.xpath.functions;

import java.util.ArrayList;
import java.util.List;

import de.lyca.xml.utils.QName;
import de.lyca.xpath.Expression;
import de.lyca.xpath.ExpressionNode;
import de.lyca.xpath.ExpressionOwner;
import de.lyca.xpath.ExtensionsProvider;
import de.lyca.xpath.XPathContext;
import de.lyca.xpath.XPathVisitor;
import de.lyca.xpath.objects.XNull;
import de.lyca.xpath.objects.XObject;
import de.lyca.xpath.res.XPATHErrorResources;
import de.lyca.xpath.res.XPATHMessages;

/**
 * An object of this class represents an extension call expression. When the
 * expression executes, it calls ExtensionsTable#extFunction, and then converts
 * the result to the appropriate XObject.
 * 
 * @xsl.usage advanced
 */
public class FuncExtFunction extends Function {
  static final long serialVersionUID = 5196115554693708718L;

  /**
   * The namespace for the extension function, which should not normally be null
   * or empty.
   * 
   * @serial
   */
  String m_namespace;

  /**
   * The local name of the extension.
   * 
   * @serial
   */
  String m_extensionName;

  /**
   * Unique method key, which is passed to ExtensionsTable#extFunction in order
   * to allow caching of the method.
   * 
   * @serial
   */
  Object m_methodKey;

  /**
   * Array of static expressions which represent the parameters to the function.
   * 
   * @serial
   */
  List<Expression> m_argVec = new ArrayList<>();

  /**
   * This function is used to fixup variables from QNames to stack frame indexes
   * at stylesheet build time.
   * 
   * @param vars
   *          List of QNames that correspond to variables. This list should be
   *          searched backwards for the first qualified name that corresponds
   *          to the variable reference qname. The position of the QName in the
   *          list from the start of the list will be its position in the stack
   *          frame (but variables above the globalsTop value will need to be
   *          offset to the current stack frame). NEEDSDOC @param globalsSize
   */
  @Override
  public void fixupVariables(List<QName> vars, int globalsSize) {

    if (null != m_argVec) {
      final int nArgs = m_argVec.size();

      for (int i = 0; i < nArgs; i++) {
        final Expression arg = m_argVec.get(i);

        arg.fixupVariables(vars, globalsSize);
      }
    }
  }

  /**
   * Return the namespace of the extension function.
   * 
   * @return The namespace of the extension function.
   */
  public String getNamespace() {
    return m_namespace;
  }

  /**
   * Return the name of the extension function.
   * 
   * @return The name of the extension function.
   */
  public String getFunctionName() {
    return m_extensionName;
  }

  /**
   * Return the method key of the extension function.
   * 
   * @return The method key of the extension function.
   */
  public Object getMethodKey() {
    return m_methodKey;
  }

  /**
   * Return the nth argument passed to the extension function.
   * 
   * @param n
   *          The argument number index.
   * @return The Expression object at the given index.
   */
  public Expression getArg(int n) {
    if (n >= 0 && n < m_argVec.size())
      return m_argVec.get(n);
    else
      return null;
  }

  /**
   * Return the number of arguments that were passed into this extension
   * function.
   * 
   * @return The number of arguments.
   */
  public int getArgCount() {
    return m_argVec.size();
  }

  /**
   * Create a new FuncExtFunction based on the qualified name of the extension,
   * and a unique method key.
   * 
   * @param namespace
   *          The namespace for the extension function, which should not
   *          normally be null or empty.
   * @param extensionName
   *          The local name of the extension.
   * @param methodKey
   *          Unique method key, which is passed to ExtensionsTable#extFunction
   *          in order to allow caching of the method.
   */
  public FuncExtFunction(java.lang.String namespace, java.lang.String extensionName, Object methodKey) {
    // try{throw new Exception("FuncExtFunction() " + namespace + " " +
    // extensionName);} catch (Exception e){e.printStackTrace();}
    m_namespace = namespace;
    m_extensionName = extensionName;
    m_methodKey = methodKey;
  }

  /**
   * Execute the function. The function must return a valid object.
   * 
   * @param xctxt
   *          The current execution context.
   * @return A valid XObject.
   * 
   * @throws javax.xml.transform.TransformerException
   */
  @Override
  public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException {
    if (xctxt.isSecureProcessing())
      throw new javax.xml.transform.TransformerException(XPATHMessages.createXPATHMessage(
              XPATHErrorResources.ER_EXTENSION_FUNCTION_CANNOT_BE_INVOKED, new Object[] { toString() }));

    XObject result;
    final int nArgs = m_argVec.size();
    final List<XObject> argVec = new ArrayList<>(nArgs);

    for (int i = 0; i < nArgs; i++) {
      final Expression arg = m_argVec.get(i);

      final XObject xobj = arg.execute(xctxt);
      /*
       * Should cache the arguments for func:function
       */
      xobj.allowDetachToRelease(false);
      argVec.add(xobj);
    }
    // dml
    final ExtensionsProvider extProvider = (ExtensionsProvider) xctxt.getOwnerObject();
    final Object val = extProvider.extFunction(this, argVec);

    if (null != val) {
      result = XObject.create(val, xctxt);
    } else {
      result = new XNull();
    }

    return result;
  }

  /**
   * Set an argument expression for a function. This method is called by the
   * XPath compiler.
   * 
   * @param arg
   *          non-null expression that represents the argument.
   * @param argNum
   *          The argument number index.
   * 
   * @throws WrongNumberArgsException
   *           If the argNum parameter is beyond what is specified for this
   *           function.
   */
  @Override
  public void setArg(Expression arg, int argNum) throws WrongNumberArgsException {
    m_argVec.add(arg);
    arg.exprSetParent(this);
  }

  /**
   * Check that the number of arguments passed to this function is correct.
   * 
   * 
   * @param argNum
   *          The number of arguments that is being passed to the function.
   * 
   * @throws WrongNumberArgsException
   */
  @Override
  public void checkNumberArgs(int argNum) throws WrongNumberArgsException {
  }

  class ArgExtOwner implements ExpressionOwner {

    Expression m_exp;

    ArgExtOwner(Expression exp) {
      m_exp = exp;
    }

    /**
     * @see ExpressionOwner#getExpression()
     */
    @Override
    public Expression getExpression() {
      return m_exp;
    }

    /**
     * @see ExpressionOwner#setExpression(Expression)
     */
    @Override
    public void setExpression(Expression exp) {
      exp.exprSetParent(FuncExtFunction.this);
      m_exp = exp;
    }
  }

  /**
   * Call the visitors for the function arguments.
   */
  @Override
  public void callArgVisitors(XPathVisitor visitor) {
    for (int i = 0; i < m_argVec.size(); i++) {
      final Expression exp = m_argVec.get(i);
      exp.callVisitors(new ArgExtOwner(exp), visitor);
    }

  }

  /**
   * Set the parent node. For an extension function, we also need to set the
   * parent node for all argument expressions.
   * 
   * @param n
   *          The parent node
   */
  @Override
  public void exprSetParent(ExpressionNode n) {

    super.exprSetParent(n);

    final int nArgs = m_argVec.size();

    for (int i = 0; i < nArgs; i++) {
      final Expression arg = m_argVec.get(i);

      arg.exprSetParent(n);
    }
  }

  /**
   * Constructs and throws a WrongNumberArgException with the appropriate
   * message for this function object. This class supports an arbitrary number
   * of arguments, so this method must never be called.
   * 
   * @throws WrongNumberArgsException
   */
  @Override
  protected void reportWrongNumberArgs() throws WrongNumberArgsException {
    final String fMsg = XPATHMessages
            .createXPATHMessage(
                    XPATHErrorResources.ER_INCORRECT_PROGRAMMER_ASSERTION,
                    new Object[] { "Programmer's assertion:  the method FunctionMultiArgs.reportWrongNumberArgs() should never be called." });

    throw new RuntimeException(fMsg);
  }

  /**
   * Return the name of the extesion function in string format
   */
  @Override
  public String toString() {
    if (m_namespace != null && m_namespace.length() > 0)
      return "{" + m_namespace + "}" + m_extensionName;
    else
      return m_extensionName;
  }
}
