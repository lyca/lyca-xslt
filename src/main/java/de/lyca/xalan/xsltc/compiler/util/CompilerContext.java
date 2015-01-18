/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.lyca.xalan.xsltc.compiler.util;

import static com.sun.codemodel.JExpr.direct;
import static de.lyca.xalan.xsltc.compiler.Constants.DOCUMENT_PNAME;
import static de.lyca.xalan.xsltc.compiler.Constants.DOM_FIELD;
import static de.lyca.xalan.xsltc.compiler.Constants.TRANSLET_PNAME;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

import de.lyca.xalan.xsltc.compiler.Stylesheet;
import de.lyca.xalan.xsltc.compiler.XSLTC;

public class CompilerContext {

  private final JCodeModel owner;
  private final JDefinedClass clazz;
  private final Stylesheet stylesheet;
  private final XSLTC xsltc;

  private final Deque<JBlock> blocks = new ArrayDeque<>();
  private final Deque<JExpression> handlers = new ArrayDeque<>();
  private final Deque<MethodContext> methods = new ArrayDeque<>();

  private final AtomicInteger resultTreeFrag = new AtomicInteger();
  private final AtomicInteger nameValue = new AtomicInteger();
  private final AtomicInteger elementName = new AtomicInteger();
  private final AtomicInteger current = new AtomicInteger();
  private final AtomicInteger parent = new AtomicInteger();
  private final AtomicInteger sortFactory = new AtomicInteger();
  private final AtomicInteger decimalFormatting = new AtomicInteger();

  public CompilerContext(JCodeModel owner, JDefinedClass clazz, Stylesheet stylesheet, XSLTC xsltc) {
    this.owner = owner;
    this.clazz = clazz;
    this.stylesheet = stylesheet;
    this.xsltc = xsltc;
  }

  public JCodeModel owner() {
    return owner;
  }

  public JDefinedClass clazz() {
    return clazz;
  }

  public Stylesheet stylesheet() {
    return stylesheet;
  }

  public XSLTC xsltc() {
    return xsltc;
  }

  public JFieldVar addPublicField(Class<?> type, String name) {
    return clazz.field(JMod.PUBLIC, type, name);
  }

  public JFieldVar addPublicField(JType type, String name) {
    return clazz.field(JMod.PUBLIC, type, name);
  }

  public JFieldVar addPrivateField(Class<?> type, String name) {
    return clazz.field(JMod.PRIVATE, type, name);
  }
  
  public JFieldVar addPrivateField(JType type, String name) {
    return clazz.field(JMod.PRIVATE, type, name);
  }
  
  public JFieldVar addProtectedStaticField(Class<?> type, String name) {
    return clazz.field(JMod.PROTECTED | JMod.STATIC, type, name);
  }

  public JFieldVar field(int mods, JType type, String name) {
    return clazz.field(mods, type, name);
  }

  public JFieldVar field(String name) {
    return clazz.fields().get(name);
  }

  public JClass ref(Class<?> type) {
    return owner.ref(type);
  }

  public JMethod method(int mods, Class<?> type, String name) {
    JMethod method = clazz.method(mods, type, name);
    methods.push(new MethodContext(method));
    return method;
  }

  public void pushMethodContext(MethodContext mctx) {
    methods.push(mctx);
  }

  public MethodContext currentMethodContext() {
    return methods.peek();
  }

  public JMethod currentMethod() {
    return methods.peek() == null ? null : methods.peek().method;
  }

  public MethodContext popMethodContext() {
    return methods.pop();
  }

  public JVar param(Class<?> type, String name) {
    MethodContext mctx = methods.peek();
    JVar param = mctx.method.param(type, name);
    mctx.methodParams.put(name, param);
    return param;
  }

  public JVar param(JType type, String name) {
    MethodContext mctx = methods.peek();
    JVar param = mctx.method.param(type, name);
    mctx.methodParams.put(name, param);
    return param;
  }

  public JVar param(String name) {
    return methods.peek().methodParams.get(name);
  }

  public void pushBlock(JBlock block) {
    blocks.push(block);
  }

  public JBlock popBlock() {
    return blocks.pop();
  }

  public JBlock currentBlock() {
    return blocks.peek();
  }

  public void pushHandler(JExpression handler) {
    handlers.push(handler);
  }

  public JExpression popHandler() {
    return handlers.pop();
  }

  public JExpression currentHandler() {
    return handlers.peek();
  }

  public void pushNode(JVar node) {
    methods.peek().currentNodes.push(node);
  }

  public JVar popNode() {
    return methods.peek().currentNodes.pop();
  }

  public JVar currentNode() {
    return methods.peek().currentNodes.peek();
  }

  public void addParent(JVar node) {
    methods.peek().currentParents.add(node);
  }
  
  public JVar pollParent() {
    return methods.peek().currentParents.poll();
  }
  
  public JVar currentParent() {
    return methods.peek().currentParents.peekLast();
  }
  
  public String currentTmpIterator() {
    return currentMethodContext().currentTmpIterator();
  }

  public String nextTmpIterator() {
    return currentMethodContext().nextTmpIterator();
  }

  public String currentResultTreeFrag() {
    return "resultTreeFrag" + resultTreeFrag.get();
  }

  public String nextResultTreeFrag() {
    return "resultTreeFrag" + resultTreeFrag.incrementAndGet();
  }

  public String currentNameValue() {
    return "nameValue" + nameValue.get();
  }

  public String nextNameValue() {
    return "nameValue" + nameValue.incrementAndGet();
  }

  public String currentElementName() {
    return "elementName" + elementName.get();
  }

  public String nextElementName() {
    return "elementName" + elementName.incrementAndGet();
  }

  public String currentCurrent() {
    return "current" + current.get();
  }

  public String nextCurrent() {
    return "current" + current.incrementAndGet();
  }

  public String nextParent() {
    return "parent" + parent.incrementAndGet();
  }

  public String nextSortFactory() {
    return "sortFactory" + sortFactory.incrementAndGet();
  }

  public String nextDecimalFormatting() {
    return "__$dfs" + decimalFormatting.incrementAndGet();
  }

  public JExpression currentDom() {
    // FIXME find better way
    JExpression document = param(DOCUMENT_PNAME);
    if (document == null) {
      if (param(TRANSLET_PNAME) == null) {
        document = direct("_document");
      } else {
        document = ((JExpression) JExpr.cast(clazz().outer(), param(TRANSLET_PNAME))).ref(DOM_FIELD);
      }
    }
    return document;
  }

  public boolean isInnerClass() {
    return clazz().outer() != null;
  }

  public static class MethodContext {
    private final JMethod method;
    private final HashMap<String, JVar> methodParams = new HashMap<>();
    private final Deque<JVar> currentNodes = new ArrayDeque<>();
    private final Deque<JVar> currentParents = new ArrayDeque<>();
    private final AtomicInteger iterator = new AtomicInteger();

    public MethodContext(JMethod method) {
      this.method = method;
    }

    public String currentTmpIterator() {
      return "iterator" + iterator.get();
    }

    public String nextTmpIterator() {
      return "iterator" + iterator.incrementAndGet();
    }

  }

}
