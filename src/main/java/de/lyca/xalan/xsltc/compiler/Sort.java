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
package de.lyca.xalan.xsltc.compiler;

import static com.sun.codemodel.JExpr._new;
import static com.sun.codemodel.JExpr._super;
import static com.sun.codemodel.JExpr._this;
import static com.sun.codemodel.JExpr.cast;
import static com.sun.codemodel.JExpr.direct;
import static com.sun.codemodel.JExpr.lit;
import static com.sun.codemodel.JExpr.newArray;
import static com.sun.codemodel.JMod.FINAL;
import static com.sun.codemodel.JMod.PUBLIC;
import static com.sun.codemodel.JMod.STATIC;
import static de.lyca.xalan.xsltc.DOM.GET_AXIS_ITERATOR;
import static de.lyca.xalan.xsltc.compiler.Constants.DOCUMENT_PNAME;
import static de.lyca.xalan.xsltc.compiler.Constants.TRANSLET_PNAME;
import static de.lyca.xml.dtm.DTMAxisIterator.SET_START_NODE;

import java.util.ArrayList;
import java.util.List;

import com.sun.codemodel.JArray;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JSwitch;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

import de.lyca.xalan.xsltc.DOM;
import de.lyca.xalan.xsltc.Translet;
import de.lyca.xalan.xsltc.TransletException;
import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.IntType;
import de.lyca.xalan.xsltc.compiler.util.Messages;
import de.lyca.xalan.xsltc.compiler.util.StringType;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;
import de.lyca.xalan.xsltc.dom.NodeSortRecord;
import de.lyca.xalan.xsltc.dom.NodeSortRecordFactory;
import de.lyca.xalan.xsltc.dom.SortingIterator;
import de.lyca.xalan.xsltc.runtime.AbstractTranslet;
import de.lyca.xml.dtm.Axis;
import de.lyca.xml.dtm.DTMAxisIterator;
import de.lyca.xml.utils.XML11Char;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 * @author Morten Jorgensen
 */
final class Sort extends Instruction implements Closure {

  private Expression _select;
  private AttributeValue _order;
  private AttributeValue _caseOrder;
  private AttributeValue _dataType;
  private AttributeValue _lang; // bug! see 26869, see XALANJ-2546

  private String _className = null;
  private List<VariableRefBase> _closureVars = null;
  private boolean _needsSortRecordFactory = false;

  // -- Begin Closure interface --------------------

  /**
   * Returns true if this closure is compiled in an inner class (i.e. if this is
   * a real closure).
   */
  @Override
  public boolean inInnerClass() {
    return _className != null;
  }

  /**
   * Returns a reference to its parent closure or null if outermost.
   */
  @Override
  public Closure getParentClosure() {
    return null;
  }

  /**
   * Returns the name of the auxiliary class or null if this predicate is
   * compiled inside the Translet.
   */
  @Override
  public String getInnerClassName() {
    return _className;
  }

  /**
   * Add new variable to the closure.
   */
  @Override
  public void addVariable(VariableRefBase variableRef) {
    if (_closureVars == null) {
      _closureVars = new ArrayList<>();
    }

    // Only one reference per variable
    if (!_closureVars.contains(variableRef)) {
      _closureVars.add(variableRef);
      _needsSortRecordFactory = true;
    }
  }

  // -- End Closure interface ----------------------

  private void setInnerClassName(String className) {
    _className = className;
  }

  /**
   * Parse the attributes of the xsl:sort element
   */
  @Override
  public void parseContents(Parser parser) {

    final SyntaxTreeNode parent = getParent();
    if (!(parent instanceof ApplyTemplates) && !(parent instanceof ForEach)) {
      reportError(this, parser, Messages.get().straySortErr());
      return;
    }

    if (hasContents()) {
      // TODO better error reporting ILLEGAL_CHILD
      reportError(this, parser, Messages.get().internalErr("Childs not allowed in xsl:sort"));
    }
    // Parse the select expression (node string value if no expression)
    _select = parser.parseExpression(this, "select", "string(.)");

    // Get the sort order; default is 'ascending'
    String val = getAttribute("order");
    if (val.isEmpty()) {
      val = "ascending";
    }
    _order = AttributeValue.create(this, val, parser);
    if (_order instanceof SimpleAttributeValue && !("ascending".equals(val) || "descending".equals(val))) {
      // TODO better error reporting ER_ATTRIB_VALUE_NOT_FOUND
      reportError(this, parser, Messages.get().internalErr("order '" + val + "' is unknown"));
    }

    // Get the sort data type; default is text
    val = getAttribute("data-type");
    if (val.isEmpty()) {
      try {
        final Type type = _select.typeCheck(parser.getSymbolTable());
        if (type instanceof IntType) {
          val = "number";
        } else {
          val = "text";
        }
      } catch (final TypeCheckError e) {
        val = "text";
      }
    }

    _dataType = AttributeValue.create(this, val, parser);
    if (_dataType instanceof SimpleAttributeValue
        && !("text".equals(val) || "number".equals(val) || val.contains(":") && XML11Char.isXML11ValidQName(val))) {
      // TODO better error reporting ER_ATTRIB_VALUE_NOT_FOUND
      reportError(this, parser, Messages.get().internalErr("datatype '" + val + "' not qname-but-not-ncname"));
    }

    val = getAttribute("lang");
    _lang = AttributeValue.create(this, val, parser);
    // Get the case order; default is language dependant
    val = getAttribute("case-order");
    _caseOrder = AttributeValue.create(this, val, parser);
    if (!val.isEmpty() && _caseOrder instanceof SimpleAttributeValue
        && !("upper-first".equals(val) || "lower-first".equals(val))) {
      // TODO better error reporting ER_ATTRIB_VALUE_NOT_FOUND / ER_ILLEGAL_ATTRIBUTE_VALUE
      reportError(this, parser, Messages.get().internalErr("case-order '" + val + "' is unknown"));
    }
  }

  /**
   * Run type checks on the attributes; expression must return a string which we
   * will use as a sort key
   */
  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {
    final Type tselect = _select.typeCheck(stable);

    // If the sort data-type is not set we use the natural data-type
    // of the data we will sort
    if (!(tselect instanceof StringType)) {
      _select = new CastExpr(_select, Type.String);
    }

    _order.typeCheck(stable);
    _caseOrder.typeCheck(stable);
    _dataType.typeCheck(stable);
    _lang.typeCheck(stable);
    return Type.Void;
  }

  /**
   * These two methods are needed in the static methods that compile the
   * overloaded NodeSortRecord.compareType() and NodeSortRecord.sortOrder()
   * 
   * @param ctx
   *          TODO
   */
  public void translateSortType(CompilerContext ctx) {
    _dataType.translate(ctx);
  }

  public void translateSortOrder(CompilerContext ctx) {
    _order.translate(ctx);
  }

  public void translateCaseOrder(CompilerContext ctx) {
    _caseOrder.translate(ctx);
  }

  public void translateLang(CompilerContext ctx) {
    _lang.translate(ctx);
  }

  /**
   * This method compiles code for the select expression for this xsl:sort
   * element. The method is called from the static code-generating methods in
   * this class.
   * 
   * @param ctx
   *          TODO
   */
  public void translateSelect(CompilerContext ctx) {
    _select.translate(ctx);
  }

  public JExpression compileSelect(CompilerContext ctx) {
    return _select.toJExpression(ctx);
  }

  /**
   * This method should not produce any code
   */
  @Override
  public void translate(CompilerContext ctx) {
    // empty
  }

  /**
   * Compiles code that instantiates a SortingIterator object. This object's
   * constructor needs references to the current iterator and a node sort record
   * producing objects as its parameters.
   * 
   * @param ctx
   *          TODO
   */
  public static JExpression translateSortIterator(CompilerContext ctx, Expression nodeSet, List<Sort> sortObjects) {
    // FIXME
    // final ConstantPoolGen cpg = classGen.getConstantPool();
    // final InstructionList il = methodGen.getInstructionList();
    // final InstructionFactory factory = new InstructionFactory(classGen, cpg);

    // SortingIterator.SortingIterator(NodeIterator,NodeSortRecordFactory);
    // final int init = cpg.addMethodref(SORT_ITERATOR, "<init>", "(" +
    // NODE_ITERATOR_SIG + NODE_SORT_FACTORY_SIG + ")V");

    // Backwards branches are prohibited if an uninitialized object is
    // on the stack by section 4.9.4 of the JVM Specification, 2nd Ed.
    // We don't know whether this code might contain backwards branches
    // so we mustn't create the new object until after we've created
    // the suspect arguments to its constructor. Instead we calculate
    // the values of the arguments to the constructor first, store them
    // in temporary variables, create the object and reload the
    // arguments from the temporaries to avoid the problem.

    // final LocalVariableGen nodesTemp =
    // methodGen.addLocalVariable("sort_tmp1",
    // Util.getJCRefType(NODE_ITERATOR_SIG),
    // null, null);

    // final LocalVariableGen sortRecordFactoryTemp =
    // methodGen.addLocalVariable("sort_tmp2",
    // Util.getJCRefType(NODE_SORT_FACTORY_SIG), null, null);

    // Get the current node iterator
    JClass dtmAxisIterator = ctx.ref(DTMAxisIterator.class);
    if (nodeSet == null) { // apply-templates default
      final JClass axis = ctx.ref(Axis.class);
      ctx.currentBlock().decl(dtmAxisIterator, ctx.nextTmpIterator(),
          ctx.currentDom().invoke(GET_AXIS_ITERATOR).arg(axis.staticRef(Axis.CHILD.name())));
    } else {
      ctx.currentBlock().decl(dtmAxisIterator, ctx.nextTmpIterator(), nodeSet.toJExpression(ctx));
      // nodeSet.translate(ctx);
    }

    // nodesTemp.setStart(il.append(new ASTORE(nodesTemp.getIndex())));

    // Compile the code for the NodeSortRecord producing class and pass
    // that as the last argument to the SortingIterator constructor.
    return compileSortRecordFactory(sortObjects, ctx);
    // sortRecordFactoryTemp.setStart(il.append(new
    // ASTORE(sortRecordFactoryTemp.getIndex())));

    // il.append(new NEW(cpg.addClass(SORT_ITERATOR)));
    // il.append(DUP);
    // nodesTemp.setEnd(il.append(new ALOAD(nodesTemp.getIndex())));
    // sortRecordFactoryTemp.setEnd(il.append(new
    // ALOAD(sortRecordFactoryTemp.getIndex())));
    // il.append(new INVOKESPECIAL(init));
  }

  /**
   * Compiles code that instantiates a NodeSortRecordFactory object which will
   * produce NodeSortRecord objects of a specific type.
   * 
   * @param ctx
   *          TODO
   */
  public static JExpression compileSortRecordFactory(List<Sort> sortObjects, CompilerContext ctx) {
    // FIXME
    final JClass sortRecordClass = compileSortRecord(sortObjects, ctx);

    boolean needsSortRecordFactory = false;
    final int nsorts = sortObjects.size();
    for (int i = 0; i < nsorts; i++) {
      final Sort sort = sortObjects.get(i);
      needsSortRecordFactory |= sort._needsSortRecordFactory;
    }

    JClass nodeSortRecordFactory = ctx.ref(NodeSortRecordFactory.class);
    if (needsSortRecordFactory) {
      nodeSortRecordFactory = compileSortRecordFactory(sortObjects, ctx, sortRecordClass);
    }

    // final ConstantPoolGen cpg = definedClass.getConstantPool();
    // final InstructionList il = method.getInstructionList();

    // Backwards branches are prohibited if an uninitialized object is
    // on the stack by section 4.9.4 of the JVM Specification, 2nd Ed.
    // We don't know whether this code might contain backwards branches
    // so we mustn't create the new object until after we've created
    // the suspect arguments to its constructor. Instead we calculate
    // the values of the arguments to the constructor first, store them
    // in temporary variables, create the object and reload the
    // arguments from the temporaries to avoid the problem.

    // Compile code that initializes the static _sortOrder
    // final LocalVariableGen sortOrderTemp =
    // method.addLocalVariable("sort_order_tmp",
    // Util.getJCRefType("[" + STRING_SIG), null, null);
    // il.append(new PUSH(cpg, nsorts));
    // il.append(new ANEWARRAY(cpg.addClass(STRING)));
    JClass stringClass = ctx.ref(String.class);
    JClass stringArray = ctx.ref(String[].class);
    JArray sortOrderArray = newArray(stringClass);
    for (int level = 0; level < nsorts; level++) {
      final Sort sort = sortObjects.get(level);
      sortOrderArray.add(sort._order.toJExpression(ctx));
    }
    // JVar sortOrderRef = ctx.currentBlock().decl(stringArray,
    // "sort_order_tmp", sortOrderArray);

    // for (int level = 0; level < nsorts; level++) {
    // final Sort sort = sortObjects.get(level);
    // il.append(DUP);
    // il.append(new PUSH(cpg, level));
    // sort.translateSortOrder(definedClass, method, method.body());
    // il.append(AASTORE);
    // }
    // sortOrderTemp.setStart(il.append(new ASTORE(sortOrderTemp.getIndex())));

    // final LocalVariableGen sortTypeTemp =
    // method.addLocalVariable("sort_type_tmp",
    // Util.getJCRefType("[" + STRING_SIG), null, null);
    // il.append(new PUSH(cpg, nsorts));
    // il.append(new ANEWARRAY(cpg.addClass(STRING)));
    JArray sortTypeArray = newArray(stringClass);
    for (int level = 0; level < nsorts; level++) {
      final Sort sort = sortObjects.get(level);
      sortTypeArray.add(sort._dataType.toJExpression(ctx));
    }
    // JVar sortTypeRef = ctx.currentBlock().decl(stringArray, "sort_type_tmp",
    // sortTypeArray);

    // for (int level = 0; level < nsorts; level++) {
    // final Sort sort = sortObjects.get(level);
    // il.append(DUP);
    // il.append(new PUSH(cpg, level));
    // sort.translateSortType(definedClass, method, method.body());
    // il.append(AASTORE);
    // }
    // sortTypeTemp.setStart(il.append(new ASTORE(sortTypeTemp.getIndex())));

    // final LocalVariableGen sortLangTemp =
    // method.addLocalVariable("sort_lang_tmp",
    // Util.getJCRefType("[" + STRING_SIG), null, null);
    // il.append(new PUSH(cpg, nsorts));
    // il.append(new ANEWARRAY(cpg.addClass(STRING)));
    JArray sortLangArray = newArray(stringClass);
    for (int level = 0; level < nsorts; level++) {
      final Sort sort = sortObjects.get(level);
      sortLangArray.add(sort._lang.toJExpression(ctx));
    }
    // JVar sortLangRef = ctx.currentBlock().decl(stringArray, "sort_lang_tmp",
    // sortLangArray);

    // for (int level = 0; level < nsorts; level++) {
    // final Sort sort = sortObjects.get(level);
    // il.append(DUP);
    // il.append(new PUSH(cpg, level));
    // sort.translateLang(definedClass, method, method.body());
    // il.append(AASTORE);
    // }
    // sortLangTemp.setStart(il.append(new ASTORE(sortLangTemp.getIndex())));

    // final LocalVariableGen sortCaseOrderTemp =
    // method.addLocalVariable("sort_case_order_tmp",
    // Util.getJCRefType("[" + STRING_SIG), null, null);
    // il.append(new PUSH(cpg, nsorts));
    // il.append(new ANEWARRAY(cpg.addClass(STRING)));
    JArray sortCaseOrderArray = newArray(stringClass);
    for (int level = 0; level < nsorts; level++) {
      final Sort sort = sortObjects.get(level);
      sortCaseOrderArray.add(sort._caseOrder.toJExpression(ctx));
    }
    // JVar sortCaseOrderRef = ctx.currentBlock().decl(stringArray,
    // "sort_case_tmp", sortCaseOrderArray);

    // for (int level = 0; level < nsorts; level++) {
    // final Sort sort = sortObjects.get(level);
    // il.append(DUP);
    // il.append(new PUSH(cpg, level));
    // sort.translateCaseOrder(definedClass, method, method.body());
    // il.append(AASTORE);
    // }
    // sortCaseOrderTemp.setStart(il.append(new
    // ASTORE(sortCaseOrderTemp.getIndex())));

    JClass sortingIterator = ctx.ref(SortingIterator.class);
    JClass dtmAxisIterator = ctx.ref(DTMAxisIterator.class);
    // il.append(new NEW(cpg.addClass(sortRecordFactoryClass)));
    // il.append(DUP);
    // il.append(method.loadDOM());
    // il.append(new PUSH(cpg, sortRecordClass));
    // il.append(definedClass.loadTranslet());

    // sortOrderTemp.setEnd(il.append(new ALOAD(sortOrderTemp.getIndex())));
    // sortTypeTemp.setEnd(il.append(new ALOAD(sortTypeTemp.getIndex())));
    // sortLangTemp.setEnd(il.append(new ALOAD(sortLangTemp.getIndex())));
    // sortCaseOrderTemp.setEnd(il.append(new
    // ALOAD(sortCaseOrderTemp.getIndex())));

    // il.append(new INVOKESPECIAL(cpg.addMethodref(sortRecordFactoryClass,
    // "<init>", "(" + DOM_INTF_SIG + STRING_SIG
    // + TRANSLET_INTF_SIG + "[" + STRING_SIG + "[" + STRING_SIG + "[" +
    // STRING_SIG + "[" + STRING_SIG + ")V")));
    JVar nsrf = ctx.currentBlock().decl(nodeSortRecordFactory, ctx.nextSortFactory(),
        _new(nodeSortRecordFactory).arg(ctx.currentDom()).arg(sortRecordClass.binaryName()).arg(_this())
            .arg(sortOrderArray).arg(sortTypeArray).arg(sortLangArray).arg(sortCaseOrderArray));

    // Initialize closure variables in sortRecordFactory
    final List<VariableRefBase> dups = new ArrayList<>();

    for (int j = 0; j < nsorts; j++) {
      final Sort sort = sortObjects.get(j);
      final int length = sort._closureVars == null ? 0 : sort._closureVars.size();

      for (int i = 0; i < length; i++) {
        final VariableRefBase varRef = sort._closureVars.get(i);

        // Discard duplicate variable references
        if (dups.contains(varRef)) {
          continue;
        }

        final VariableBase var = varRef.getVariable();

        ctx.currentBlock().assign(nsrf.ref(var.getEscapedName()), var._select.toJExpression(ctx));
        // Store variable in new closure
        // il.append(DUP);
        // il.append(var.loadInstruction());
        // il.append(new PUTFIELD(cpg.addFieldref(sortRecordFactoryClass,
        // var.getEscapedName(), var.getType()
        // .toSignature())));
        dups.add(varRef);
      }
    }
    String currentTmpIterator = ctx.currentTmpIterator();
    JVar iterator = ctx.currentBlock().decl(dtmAxisIterator, ctx.nextTmpIterator(),
        _new(sortingIterator).arg(direct(currentTmpIterator)).arg(nsrf).invoke(SET_START_NODE).arg(ctx.currentNode()));
    return iterator;
  }

  public static JClass compileSortRecordFactory(List<Sort> sortObjects, CompilerContext ctx, JClass sortRecordClass) {
    final XSLTC xsltc = ctx.xsltc();
    final String className = xsltc.getHelperClassName();

    // This generates a new class for handling this specific sort
    CompilerContext factoryCtx;
    JDefinedClass nodeSortRecordFactory;
    try {
      nodeSortRecordFactory = ctx.clazz()._class(PUBLIC | STATIC | FINAL, className)
          ._extends(NodeSortRecordFactory.class);
    } catch (JClassAlreadyExistsException e) {
      throw new RuntimeException(e);
    }
    factoryCtx = new CompilerContext(ctx.owner(), nodeSortRecordFactory, ctx.stylesheet(), xsltc);

    // final NodeSortRecordFactGenerator sortRecordFactory = new
    // NodeSortRecordFactGenerator(className, NODE_SORT_FACTORY,
    // className + ".java", ACC_PUBLIC | ACC_SUPER | ACC_FINAL, new String[] {},
    // xsltc.getStylesheet());
    //
    // final ConstantPoolGen cpg = sortRecordFactory.getConstantPool();

    // Add a new instance variable for each var in closure
    final int nsorts = sortObjects.size();
    final List<VariableRefBase> dups = new ArrayList<>();

    for (int j = 0; j < nsorts; j++) {
      final Sort sort = sortObjects.get(j);
      final int length = sort._closureVars == null ? 0 : sort._closureVars.size();

      for (int i = 0; i < length; i++) {
        final VariableRefBase varRef = sort._closureVars.get(i);

        // Discard duplicate variable references
        if (dups.contains(varRef)) {
          continue;
        }

        JType jcType = varRef.getType().toJCType();
        final VariableBase var = varRef.getVariable();
        String escapedName = var.getEscapedName();
        JVar field = factoryCtx.addPublicField(jcType, escapedName);
        // sortRecordFactory.addField(new Field(ACC_PUBLIC,
        // cpg.addUtf8(var.getEscapedName()), cpg.addUtf8(var.getType()
        // .toSignature()), null, cpg.getConstantPool()));
        dups.add(varRef);
      }
    }

    // Define a constructor for this class
    JMethod constructor = nodeSortRecordFactory.constructor(PUBLIC)._throws(TransletException.class);
    JVar document = constructor.param(ctx.ref(DOM.class), DOCUMENT_PNAME);
    JVar clazzName = constructor.param(ctx.ref(String.class), "className");
    JVar translet = constructor.param(ctx.ref(Translet.class), TRANSLET_PNAME);
    JVar order = constructor.param(ctx.ref(String[].class), "order");
    JVar type = constructor.param(ctx.ref(String[].class), "type");
    JVar lang = constructor.param(ctx.ref(String[].class), "lang");
    JVar caseOrder = constructor.param(ctx.ref(String[].class), "caseOrder");
    constructor.body().invoke("super").arg(document).arg(clazzName).arg(translet).arg(order).arg(type).arg(lang)
        .arg(caseOrder);

    // final org.apache.bcel.generic.Type[] argTypes = new
    // org.apache.bcel.generic.Type[7];
    // argTypes[0] = Util.getJCRefType(DOM_INTF_SIG);
    // argTypes[1] = Util.getJCRefType(STRING_SIG);
    // argTypes[2] = Util.getJCRefType(TRANSLET_INTF_SIG);
    // argTypes[3] = Util.getJCRefType("[" + STRING_SIG);
    // argTypes[4] = Util.getJCRefType("[" + STRING_SIG);
    // argTypes[5] = Util.getJCRefType("[" + STRING_SIG);
    // argTypes[6] = Util.getJCRefType("[" + STRING_SIG);

    // final String[] argNames = new String[7];
    // argNames[0] = DOCUMENT_PNAME;
    // argNames[1] = "className";
    // argNames[2] = TRANSLET_PNAME;
    // argNames[3] = "order";
    // argNames[4] = "type";
    // argNames[5] = "lang";
    // argNames[6] = "case_order";

    // InstructionList il = new InstructionList();
    // final MethodGenerator constructor = new MethodGenerator(ACC_PUBLIC,
    // org.apache.bcel.generic.Type.VOID, argTypes,
    // argNames, "<init>", className, il, cpg);

    // Push all parameters onto the stack and called super.<init>()
    // il.append(ALOAD_0);
    // il.append(ALOAD_1);
    // il.append(ALOAD_2);
    // il.append(new ALOAD(3));
    // il.append(new ALOAD(4));
    // il.append(new ALOAD(5));
    // il.append(new ALOAD(6));
    // il.append(new ALOAD(7));
    // il.append(new INVOKESPECIAL(cpg.addMethodref(NODE_SORT_FACTORY, "<init>",
    // "(" + DOM_INTF_SIG + STRING_SIG
    // + TRANSLET_INTF_SIG + "[" + STRING_SIG + "[" + STRING_SIG + "[" +
    // STRING_SIG + "[" + STRING_SIG + ")V")));
    // il.append(RETURN);

    // Override the definition of makeNodeSortRecord()
    JMethod makeNodeSortRecord = factoryCtx.method(JMod.PUBLIC | JMod.FINAL, NodeSortRecord.class, "makeNodeSortRecord")
        ._throws(ExceptionInInitializerError.class)._throws(LinkageError.class)._throws(IllegalAccessException.class)
        ._throws(InstantiationException.class)._throws(SecurityException.class)._throws(TransletException.class);
    JVar node = factoryCtx.param(int.class, "node");
    JVar last = factoryCtx.param(int.class, "last");
    // il = new InstructionList();
    // final MethodGenerator makeNodeSortRecord = new
    // MethodGenerator(ACC_PUBLIC, Util.getJCRefType(NODE_SORT_RECORD_SIG),
    // new org.apache.bcel.generic.Type[] { org.apache.bcel.generic.Type.INT,
    // org.apache.bcel.generic.Type.INT },
    // new String[] { "node", "last" }, "makeNodeSortRecord", className, il,
    // cpg);

    JVar nsr = makeNodeSortRecord.body().decl(sortRecordClass, "nsr",
        cast(sortRecordClass, _super().invoke("makeNodeSortRecord").arg(node).arg(last)));
    // il.append(ALOAD_0);
    // il.append(ILOAD_1);
    // il.append(ILOAD_2);
    // il.append(new INVOKESPECIAL(cpg
    // .addMethodref(NODE_SORT_FACTORY, "makeNodeSortRecord", "(II)" +
    // NODE_SORT_RECORD_SIG)));
    // il.append(DUP);
    // il.append(new CHECKCAST(cpg.addClass(sortRecordClass)));

    // Initialize closure in record class
    final int ndups = dups.size();
    for (int i = 0; i < ndups; i++) {
      final VariableRefBase varRef = dups.get(i);
      final VariableBase var = varRef.getVariable();
      JType jcType = varRef.getType().toJCType();

      String escapedName = var.getEscapedName();
      makeNodeSortRecord.body().assign(nsr.ref(escapedName), _this().ref(escapedName));
      // il.append(DUP);

      // Get field from factory class
      // il.append(ALOAD_0);
      // il.append(new GETFIELD(cpg.addFieldref(className, var.getEscapedName(),
      // varType.toSignature())));

      // Put field in record class
      // il.append(new PUTFIELD(cpg.addFieldref(sortRecordClass,
      // var.getEscapedName(), varType.toSignature())));
    }
    // il.append(POP);
    // il.append(ARETURN);
    makeNodeSortRecord.body()._return(nsr);

    // constructor.setMaxLocals();
    // constructor.setMaxStack();
    // sortRecordFactory.addMethod(constructor);
    // makeNodeSortRecord.setMaxLocals();
    // makeNodeSortRecord.setMaxStack();
    // sortRecordFactory.addMethod(makeNodeSortRecord);
    // FIXME
    // xsltc.dumpClass(sortRecordFactory.getJavaClass());

    return nodeSortRecordFactory;
  }

  /**
   * Create a new auxillary class extending NodeSortRecord.
   * 
   * @param ctx
   *          TODO
   */
  private static JClass compileSortRecord(List<Sort> sortObjects, CompilerContext ctx) {
    // FIXME
    final XSLTC xsltc = ctx.xsltc();
    final String className = xsltc.getHelperClassName();

    // This generates a new class for handling this specific sort
    CompilerContext sortCtx;
    JDefinedClass nodeSortRecord;
    try {
      nodeSortRecord = ctx.clazz()._class(PUBLIC | STATIC | FINAL, className)._extends(NodeSortRecord.class);
    } catch (JClassAlreadyExistsException e) {
      throw new RuntimeException(e);
    }
    sortCtx = new CompilerContext(ctx.owner(), nodeSortRecord, ctx.stylesheet(), xsltc);
    // final NodeSortRecordGenerator sortRecord = new
    // NodeSortRecordGenerator(className, NODE_SORT_RECORD, "sort$0.java",
    // ACC_PUBLIC | ACC_SUPER | ACC_FINAL, new String[] {},
    // classGen.getStylesheet());

    // final ConstantPoolGen cpg = sortRecord.getConstantPool();

    JMethod constructor = compileInit(sortObjects, sortCtx, className);
    // Add a new instance variable for each var in closure
    final int nsorts = sortObjects.size();
    final List<VariableRefBase> dups = new ArrayList<>();

    for (int j = 0; j < nsorts; j++) {
      final Sort sort = sortObjects.get(j);

      // Set the name of the inner class in this sort object
      sort.setInnerClassName(className);

      final int length = sort._closureVars == null ? 0 : sort._closureVars.size();
      for (int i = 0; i < length; i++) {
        final VariableRefBase varRef = sort._closureVars.get(i);

        // Discard duplicate variable references
        if (dups.contains(varRef)) {
          continue;
        }

        JType jcType = varRef.getType().toJCType();
        final VariableBase var = varRef.getVariable();
        String escapedName = var.getEscapedName();
        JVar field = sortCtx.addPublicField(jcType, escapedName);
        // JVar param = constructor.param(jcType, escapedName);
        // constructor.body().assign(_this().ref(field), param);

        // nodeSortRecord.field(JMod.PUBLIC, var.getType().toJCType(),
        // var.getEscapedName());
        // sortRecord.addField(new Field(ACC_PUBLIC,
        // cpg.addUtf8(var.getEscapedName()), cpg.addUtf8(var.getType()
        // .toSignature()), null, cpg.getConstantPool()));
        dups.add(varRef);
      }
    }

    compileExtract(sortObjects, sortCtx, className);

    // xsltc.dumpClass(nodeSortRecord.owner(), nodeSortRecord);
    return nodeSortRecord;
  }

  /**
   * Create a constructor for the new class. Updates the reference to the
   * collator in the super calls only when the stylesheet specifies a new
   * language in xsl:sort.
   */
  private static JMethod compileInit(List<Sort> sortObjects, CompilerContext sortCtx, String className) {
    JMethod constructor = sortCtx.clazz().constructor(PUBLIC);
    constructor.body().invoke("super");
    return constructor;
  }

  /**
   * Compiles a method that overloads NodeSortRecord.extractValueFromDOM()
   */
  private static void compileExtract(List<Sort> sortObjects, CompilerContext sortCtx, String className) {
    // String NodeSortRecord.extractValueFromDOM(DOM dom, int current, int
    // level, AbstractTranslet translet, int last);
    JMethod extractValueFromDOM = sortCtx.method(JMod.PUBLIC | JMod.FINAL, String.class, "extractValueFromDOM")
        ._throws(TransletException.class);
    sortCtx.param(DOM.class, DOCUMENT_PNAME);
    JVar currentParam = sortCtx.param(int.class, "current");
    JVar levelParam = sortCtx.param(int.class, "level");
    sortCtx.param(AbstractTranslet.class, "translet");
    sortCtx.param(int.class, "last");

    sortCtx.pushBlock(extractValueFromDOM.body());
    sortCtx.pushNode(currentParam);

    // Values needed for the switch statement
    final int levels = sortObjects.size();

    // Compile switch statement only if the key has multiple levels
    if (levels > 1) {
      JSwitch _switch = sortCtx.currentBlock()._switch(levelParam);
      for (int level = 0; level < levels; level++) {
        final Sort sort = sortObjects.get(level);
        JBlock body = _switch._case(lit(level)).body();
        sortCtx.pushBlock(body);
        body._return(sort.compileSelect(sortCtx));
        sortCtx.popBlock();
      }
      // Will never be reached
      _switch._default().body()._return(lit(""));
    } else {
      JBlock body = extractValueFromDOM.body();
      body._return(sortObjects.get(0).compileSelect(sortCtx));
    }
    sortCtx.popNode();
    sortCtx.popBlock();
  }

}
