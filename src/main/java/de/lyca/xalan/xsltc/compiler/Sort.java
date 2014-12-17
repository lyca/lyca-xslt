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

package de.lyca.xalan.xsltc.compiler;

import static com.sun.codemodel.JExpr._new;
import static com.sun.codemodel.JExpr._this;
import static com.sun.codemodel.JExpr.direct;
import static com.sun.codemodel.JExpr.newArray;
import static com.sun.codemodel.JMod.PUBLIC;

import java.util.ArrayList;
import java.util.List;

import org.apache.bcel.classfile.Field;
import org.apache.bcel.generic.ALOAD;
import org.apache.bcel.generic.CHECKCAST;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.GETFIELD;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.PUTFIELD;

import com.sun.codemodel.JArray;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JVar;

import de.lyca.xalan.xsltc.DOM;
import de.lyca.xalan.xsltc.compiler.util.ErrorMsg;
import de.lyca.xalan.xsltc.compiler.util.IntType;
import de.lyca.xalan.xsltc.compiler.util.MethodGenerator;
import de.lyca.xalan.xsltc.compiler.util.NodeSortRecordFactGenerator;
import de.lyca.xalan.xsltc.compiler.util.StringType;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;
import de.lyca.xalan.xsltc.compiler.util.Util;
import de.lyca.xalan.xsltc.dom.NodeSortRecord;
import de.lyca.xalan.xsltc.dom.NodeSortRecordFactory;
import de.lyca.xalan.xsltc.dom.SortingIterator;
import de.lyca.xalan.xsltc.runtime.AbstractTranslet;
import de.lyca.xml.dtm.DTMAxisIterator;

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
      reportError(this, parser, ErrorMsg.STRAY_SORT_ERR, null);
      return;
    }

    // Parse the select expression (node string value if no expression)
    _select = parser.parseExpression(this, "select", "string(.)");

    // Get the sort order; default is 'ascending'
    String val = getAttribute("order");
    if (val.length() == 0) {
      val = "ascending";
    }
    _order = AttributeValue.create(this, val, parser);

    // Get the sort data type; default is text
    val = getAttribute("data-type");
    if (val.length() == 0) {
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

    val = getAttribute("lang");
    _lang = AttributeValue.create(this, val, parser);
    // Get the case order; default is language dependant
    val = getAttribute("case-order");
    _caseOrder = AttributeValue.create(this, val, parser);

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
   */
  public void translateSortType(JDefinedClass definedClass, JMethod method, JBlock body) {
    _dataType.translate(definedClass, method, body);
  }

  public void translateSortOrder(JDefinedClass definedClass, JMethod method, JBlock body) {
    _order.translate(definedClass, method, body);
  }

  public void translateCaseOrder(JDefinedClass definedClass, JMethod method, JBlock body) {
    _caseOrder.translate(definedClass, method, body);
  }

  public void translateLang(JDefinedClass definedClass, JMethod method, JBlock body) {
    _lang.translate(definedClass, method, body);
  }

  /**
   * This method compiles code for the select expression for this xsl:sort
   * element. The method is called from the static code-generating methods in
   * this class.
   */
  public void translateSelect(JDefinedClass definedClass, JMethod method, JBlock body) {
    _select.translate(definedClass, method, body);
  }

  public JExpression compileSelect(JDefinedClass definedClass, JMethod method) {
    return _select.compile(definedClass, method);
  }

  /**
   * This method should not produce any code
   */
  @Override
  public void translate(JDefinedClass definedClass, JMethod method, JBlock body) {
    // empty
  }

  /**
   * Compiles code that instantiates a SortingIterator object. This object's
   * constructor needs referencdes to the current iterator and a node sort
   * record producing objects as its parameters.
   */
  public static JExpression translateSortIterator(JDefinedClass definedClass, JMethod method, Expression nodeSet,
          List<Sort> sortObjects) {
    // FIXME
//    final ConstantPoolGen cpg = classGen.getConstantPool();
//    final InstructionList il = methodGen.getInstructionList();
//    final InstructionFactory factory = new InstructionFactory(classGen, cpg);

    // SortingIterator.SortingIterator(NodeIterator,NodeSortRecordFactory);
//    final int init = cpg.addMethodref(SORT_ITERATOR, "<init>", "(" + NODE_ITERATOR_SIG + NODE_SORT_FACTORY_SIG + ")V");

    // Backwards branches are prohibited if an uninitialized object is
    // on the stack by section 4.9.4 of the JVM Specification, 2nd Ed.
    // We don't know whether this code might contain backwards branches
    // so we mustn't create the new object until after we've created
    // the suspect arguments to its constructor. Instead we calculate
    // the values of the arguments to the constructor first, store them
    // in temporary variables, create the object and reload the
    // arguments from the temporaries to avoid the problem.

//    final LocalVariableGen nodesTemp = methodGen.addLocalVariable("sort_tmp1", Util.getJCRefType(NODE_ITERATOR_SIG),
//            null, null);

//    final LocalVariableGen sortRecordFactoryTemp = methodGen.addLocalVariable("sort_tmp2",
//            Util.getJCRefType(NODE_SORT_FACTORY_SIG), null, null);

    // Get the current node iterator
    if (nodeSet == null) { // apply-templates default
//      final int children = cpg.addInterfaceMethodref(DOM_INTF, "getAxisIterator", "(Lde/lyca/xml/dtm/Axis;)" + NODE_ITERATOR_SIG);
//      il.append(methodGen.loadDOM());
//      il.append(factory.createFieldAccess("de.lyca.xml.dtm.Axis", Axis.CHILD.name(), Type.Axis.toJCType(), org.apache.bcel.Constants.GETSTATIC));
//      il.append(new INVOKEINTERFACE(children, 2));
    } else {
      nodeSet.translate(definedClass, method, method.body());
    }

//    nodesTemp.setStart(il.append(new ASTORE(nodesTemp.getIndex())));

    // Compile the code for the NodeSortRecord producing class and pass
    // that as the last argument to the SortingIterator constructor.
    return compileSortRecordFactory(sortObjects, definedClass, method);
//    sortRecordFactoryTemp.setStart(il.append(new ASTORE(sortRecordFactoryTemp.getIndex())));

//    il.append(new NEW(cpg.addClass(SORT_ITERATOR)));
//    il.append(DUP);
//    nodesTemp.setEnd(il.append(new ALOAD(nodesTemp.getIndex())));
//    sortRecordFactoryTemp.setEnd(il.append(new ALOAD(sortRecordFactoryTemp.getIndex())));
//    il.append(new INVOKESPECIAL(init));
  }

  /**
   * Compiles code that instantiates a NodeSortRecordFactory object which will
   * produce NodeSortRecord objects of a specific type.
   */
  public static JExpression compileSortRecordFactory(List<Sort> sortObjects, JDefinedClass definedClass, JMethod method) {
 // FIXME
    final String sortRecordClass = compileSortRecord(sortObjects, definedClass, method);

    boolean needsSortRecordFactory = false;
    final int nsorts = sortObjects.size();
    for (int i = 0; i < nsorts; i++) {
      final Sort sort = sortObjects.get(i);
      needsSortRecordFactory |= sort._needsSortRecordFactory;
    }

    String sortRecordFactoryClass = NODE_SORT_FACTORY;
    if (needsSortRecordFactory) {
      sortRecordFactoryClass = compileSortRecordFactory(sortObjects, definedClass, method, sortRecordClass);
    }

//    final ConstantPoolGen cpg = definedClass.getConstantPool();
//    final InstructionList il = method.getInstructionList();

    // Backwards branches are prohibited if an uninitialized object is
    // on the stack by section 4.9.4 of the JVM Specification, 2nd Ed.
    // We don't know whether this code might contain backwards branches
    // so we mustn't create the new object until after we've created
    // the suspect arguments to its constructor. Instead we calculate
    // the values of the arguments to the constructor first, store them
    // in temporary variables, create the object and reload the
    // arguments from the temporaries to avoid the problem.

    // Compile code that initializes the static _sortOrder
//    final LocalVariableGen sortOrderTemp = method.addLocalVariable("sort_order_tmp",
//            Util.getJCRefType("[" + STRING_SIG), null, null);
//    il.append(new PUSH(cpg, nsorts));
//    il.append(new ANEWARRAY(cpg.addClass(STRING)));
    JClass stringClass = definedClass.owner().ref(String.class);
    JClass stringArray = definedClass.owner().ref(String[].class);
    JArray sortOrderArray = newArray(stringClass);
    for (int level = 0; level < nsorts; level++) {
      final Sort sort = sortObjects.get(level);
      sortOrderArray.add(sort._order.compile(definedClass, method));
    }
    JVar sortOrderRef = method.body().decl(stringArray, "sort_order_tmp", sortOrderArray);

    
//    for (int level = 0; level < nsorts; level++) {
//      final Sort sort = sortObjects.get(level);
//      il.append(DUP);
//      il.append(new PUSH(cpg, level));
//      sort.translateSortOrder(definedClass, method, method.body());
//      il.append(AASTORE);
//    }
//    sortOrderTemp.setStart(il.append(new ASTORE(sortOrderTemp.getIndex())));

//    final LocalVariableGen sortTypeTemp = method.addLocalVariable("sort_type_tmp",
//            Util.getJCRefType("[" + STRING_SIG), null, null);
//    il.append(new PUSH(cpg, nsorts));
//    il.append(new ANEWARRAY(cpg.addClass(STRING)));
    JArray sortTypeArray = newArray(stringClass);
    for (int level = 0; level < nsorts; level++) {
      final Sort sort = sortObjects.get(level);
      sortTypeArray.add(sort._dataType.compile(definedClass, method));
    }
    JVar sortTypeRef = method.body().decl(stringArray, "sort_type_tmp", sortTypeArray);

//    for (int level = 0; level < nsorts; level++) {
//      final Sort sort = sortObjects.get(level);
//      il.append(DUP);
//      il.append(new PUSH(cpg, level));
//      sort.translateSortType(definedClass, method, method.body());
//      il.append(AASTORE);
//    }
//    sortTypeTemp.setStart(il.append(new ASTORE(sortTypeTemp.getIndex())));

//    final LocalVariableGen sortLangTemp = method.addLocalVariable("sort_lang_tmp",
//            Util.getJCRefType("[" + STRING_SIG), null, null);
//    il.append(new PUSH(cpg, nsorts));
//    il.append(new ANEWARRAY(cpg.addClass(STRING)));
    JArray sortLangArray = newArray(stringClass);
    for (int level = 0; level < nsorts; level++) {
      final Sort sort = sortObjects.get(level);
      sortLangArray.add(sort._lang.compile(definedClass, method));
    }
    JVar sortLangRef = method.body().decl(stringArray, "sort_lang_tmp", sortLangArray);

//    for (int level = 0; level < nsorts; level++) {
//      final Sort sort = sortObjects.get(level);
//      il.append(DUP);
//      il.append(new PUSH(cpg, level));
//      sort.translateLang(definedClass, method, method.body());
//      il.append(AASTORE);
//    }
//    sortLangTemp.setStart(il.append(new ASTORE(sortLangTemp.getIndex())));

//    final LocalVariableGen sortCaseOrderTemp = method.addLocalVariable("sort_case_order_tmp",
//            Util.getJCRefType("[" + STRING_SIG), null, null);
//    il.append(new PUSH(cpg, nsorts));
//    il.append(new ANEWARRAY(cpg.addClass(STRING)));
    JArray sortCaseOrderArray = newArray(stringClass);
    for (int level = 0; level < nsorts; level++) {
      final Sort sort = sortObjects.get(level);
      sortCaseOrderArray.add(sort._caseOrder.compile(definedClass, method));
    }
    JVar sortCaseOrderRef = method.body().decl(stringArray, "sort_case_tmp", sortCaseOrderArray);

//    for (int level = 0; level < nsorts; level++) {
//      final Sort sort = sortObjects.get(level);
//      il.append(DUP);
//      il.append(new PUSH(cpg, level));
//      sort.translateCaseOrder(definedClass, method, method.body());
//      il.append(AASTORE);
//    }
//    sortCaseOrderTemp.setStart(il.append(new ASTORE(sortCaseOrderTemp.getIndex())));

    JClass sortingIterator = definedClass.owner().ref(SortingIterator.class);
    JClass nodeSortRecordFactory = definedClass.owner().ref(NodeSortRecordFactory.class);
    JClass dtmAxisIterator = definedClass.owner().ref(DTMAxisIterator.class);
    JInvocation nsrf = _new(nodeSortRecordFactory).arg(method.listParams()[0]).arg(sortRecordClass).arg(_this()).arg(sortOrderRef).arg(sortTypeRef).arg(sortLangRef).arg(sortCaseOrderRef);
    JVar iterator = method.body().decl(dtmAxisIterator, "tmpSortIterator", _new(sortingIterator).arg(direct("tmpIterator")).arg(nsrf).invoke("setStartNode").arg(direct("node")));
//    il.append(new NEW(cpg.addClass(sortRecordFactoryClass)));
//    il.append(DUP);
//    il.append(method.loadDOM());
//    il.append(new PUSH(cpg, sortRecordClass));
//    il.append(definedClass.loadTranslet());

//    sortOrderTemp.setEnd(il.append(new ALOAD(sortOrderTemp.getIndex())));
//    sortTypeTemp.setEnd(il.append(new ALOAD(sortTypeTemp.getIndex())));
//    sortLangTemp.setEnd(il.append(new ALOAD(sortLangTemp.getIndex())));
//    sortCaseOrderTemp.setEnd(il.append(new ALOAD(sortCaseOrderTemp.getIndex())));

//    il.append(new INVOKESPECIAL(cpg.addMethodref(sortRecordFactoryClass, "<init>", "(" + DOM_INTF_SIG + STRING_SIG
//            + TRANSLET_INTF_SIG + "[" + STRING_SIG + "[" + STRING_SIG + "[" + STRING_SIG + "[" + STRING_SIG + ")V")));

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

        // Store variable in new closure
//        il.append(DUP);
//        il.append(var.loadInstruction());
//        il.append(new PUTFIELD(cpg.addFieldref(sortRecordFactoryClass, var.getEscapedName(), var.getType()
//                .toSignature())));
        dups.add(varRef);
      }
    }
    return iterator;
  }

  public static String compileSortRecordFactory(List<Sort> sortObjects, JDefinedClass definedClass,
          JMethod method, String sortRecordClass) {
    final XSLTC xsltc = sortObjects.get(0).getXSLTC();
    final String className = xsltc.getHelperClassName();

    final NodeSortRecordFactGenerator sortRecordFactory = new NodeSortRecordFactGenerator(className, NODE_SORT_FACTORY,
            className + ".java", ACC_PUBLIC | ACC_SUPER | ACC_FINAL, new String[] {}, xsltc.getStylesheet());

    final ConstantPoolGen cpg = sortRecordFactory.getConstantPool();

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

        final VariableBase var = varRef.getVariable();
        sortRecordFactory.addField(new Field(ACC_PUBLIC, cpg.addUtf8(var.getEscapedName()), cpg.addUtf8(var.getType()
                .toSignature()), null, cpg.getConstantPool()));
        dups.add(varRef);
      }
    }

    // Define a constructor for this class
    final org.apache.bcel.generic.Type[] argTypes = new org.apache.bcel.generic.Type[7];
    argTypes[0] = Util.getJCRefType(DOM_INTF_SIG);
    argTypes[1] = Util.getJCRefType(STRING_SIG);
    argTypes[2] = Util.getJCRefType(TRANSLET_INTF_SIG);
    argTypes[3] = Util.getJCRefType("[" + STRING_SIG);
    argTypes[4] = Util.getJCRefType("[" + STRING_SIG);
    argTypes[5] = Util.getJCRefType("[" + STRING_SIG);
    argTypes[6] = Util.getJCRefType("[" + STRING_SIG);

    final String[] argNames = new String[7];
    argNames[0] = DOCUMENT_PNAME;
    argNames[1] = "className";
    argNames[2] = TRANSLET_PNAME;
    argNames[3] = "order";
    argNames[4] = "type";
    argNames[5] = "lang";
    argNames[6] = "case_order";

    InstructionList il = new InstructionList();
    final MethodGenerator constructor = new MethodGenerator(ACC_PUBLIC, org.apache.bcel.generic.Type.VOID, argTypes,
            argNames, "<init>", className, il, cpg);

    // Push all parameters onto the stack and called super.<init>()
    il.append(ALOAD_0);
    il.append(ALOAD_1);
    il.append(ALOAD_2);
    il.append(new ALOAD(3));
    il.append(new ALOAD(4));
    il.append(new ALOAD(5));
    il.append(new ALOAD(6));
    il.append(new ALOAD(7));
    il.append(new INVOKESPECIAL(cpg.addMethodref(NODE_SORT_FACTORY, "<init>", "(" + DOM_INTF_SIG + STRING_SIG
            + TRANSLET_INTF_SIG + "[" + STRING_SIG + "[" + STRING_SIG + "[" + STRING_SIG + "[" + STRING_SIG + ")V")));
    il.append(RETURN);

    // Override the definition of makeNodeSortRecord()
    il = new InstructionList();
    final MethodGenerator makeNodeSortRecord = new MethodGenerator(ACC_PUBLIC, Util.getJCRefType(NODE_SORT_RECORD_SIG),
            new org.apache.bcel.generic.Type[] { org.apache.bcel.generic.Type.INT, org.apache.bcel.generic.Type.INT },
            new String[] { "node", "last" }, "makeNodeSortRecord", className, il, cpg);

    il.append(ALOAD_0);
    il.append(ILOAD_1);
    il.append(ILOAD_2);
    il.append(new INVOKESPECIAL(cpg
            .addMethodref(NODE_SORT_FACTORY, "makeNodeSortRecord", "(II)" + NODE_SORT_RECORD_SIG)));
    il.append(DUP);
    il.append(new CHECKCAST(cpg.addClass(sortRecordClass)));

    // Initialize closure in record class
    final int ndups = dups.size();
    for (int i = 0; i < ndups; i++) {
      final VariableRefBase varRef = dups.get(i);
      final VariableBase var = varRef.getVariable();
      final Type varType = var.getType();

      il.append(DUP);

      // Get field from factory class
      il.append(ALOAD_0);
      il.append(new GETFIELD(cpg.addFieldref(className, var.getEscapedName(), varType.toSignature())));

      // Put field in record class
      il.append(new PUTFIELD(cpg.addFieldref(sortRecordClass, var.getEscapedName(), varType.toSignature())));
    }
    il.append(POP);
    il.append(ARETURN);

    constructor.setMaxLocals();
    constructor.setMaxStack();
    sortRecordFactory.addMethod(constructor);
    makeNodeSortRecord.setMaxLocals();
    makeNodeSortRecord.setMaxStack();
    sortRecordFactory.addMethod(makeNodeSortRecord);
    // FIXME
    // xsltc.dumpClass(sortRecordFactory.getJavaClass());

    return className;
  }

  /**
   * Create a new auxillary class extending NodeSortRecord.
   */
  private static String compileSortRecord(List<Sort> sortObjects, JDefinedClass definedClass, JMethod method) {
 // FIXME
    final XSLTC xsltc = sortObjects.get(0).getXSLTC();
    final String className = xsltc.getHelperClassName();

    // This generates a new class for handling this specific sort
    JDefinedClass nodeSortRecord;
    try {
      nodeSortRecord = definedClass._class(JMod.PUBLIC | JMod.FINAL, className)._extends(NodeSortRecord.class);
    } catch (JClassAlreadyExistsException e) {
      throw new RuntimeException(e);
    }
    //    final NodeSortRecordGenerator sortRecord = new NodeSortRecordGenerator(className, NODE_SORT_RECORD, "sort$0.java",
//            ACC_PUBLIC | ACC_SUPER | ACC_FINAL, new String[] {}, classGen.getStylesheet());

//    final ConstantPoolGen cpg = sortRecord.getConstantPool();

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

        final VariableBase var = varRef.getVariable();
        nodeSortRecord.field(JMod.PUBLIC, var.getType().toJCType(), var.getEscapedName());
//        sortRecord.addField(new Field(ACC_PUBLIC, cpg.addUtf8(var.getEscapedName()), cpg.addUtf8(var.getType()
//                .toSignature()), null, cpg.getConstantPool()));
        dups.add(varRef);
      }
    }

    compileInit(sortObjects, nodeSortRecord, className);
    compileExtract(sortObjects, nodeSortRecord, className);

//    xsltc.dumpClass(nodeSortRecord.owner(), nodeSortRecord);
    return nodeSortRecord.binaryName();
  }

  /**
   * Create a constructor for the new class. Updates the reference to the
   * collator in the super calls only when the stylesheet specifies a new
   * language in xsl:sort.
   */
  private static void compileInit(List<Sort> sortObjects, JDefinedClass nodeSortRecord, String className) {
    nodeSortRecord.constructor(PUBLIC).body().invoke("super");
  }

  /**
   * Compiles a method that overloads NodeSortRecord.extractValueFromDOM()
   */
  private static void compileExtract(List<Sort> sortObjects, JDefinedClass nodeSortRecord, String className) {
 // FIXME
//    final InstructionList il = new InstructionList();

    // String NodeSortRecord.extractValueFromDOM(DOM dom, int current, int level, AbstractTranslet translet, int last);
    JMethod extractValueFromDOM = nodeSortRecord.method(JMod.PUBLIC | JMod.FINAL, String.class, "extractValueFromDOM");
    JVar domParam = extractValueFromDOM.param(DOM.class, "dom");
    JVar currentParam = extractValueFromDOM.param(int.class, "node");
    JVar levelParam = extractValueFromDOM.param(int.class, "level");
    JVar transletParam = extractValueFromDOM.param(AbstractTranslet.class, "translet");
    JVar lastParam = extractValueFromDOM.param(int.class, "last");
//    final CompareGenerator extractMethod = new CompareGenerator(ACC_PUBLIC | ACC_FINAL,
//            org.apache.bcel.generic.Type.STRING, new org.apache.bcel.generic.Type[] { Util.getJCRefType(DOM_INTF_SIG),
//                    org.apache.bcel.generic.Type.INT, org.apache.bcel.generic.Type.INT,
//                    Util.getJCRefType(TRANSLET_SIG), org.apache.bcel.generic.Type.INT }, new String[] { "dom",
//                    "current", "level", "translet", "last" }, "extractValueFromDOM", className, il, cpg);

    // Values needed for the switch statement
    final int levels = sortObjects.size();
    final int match[] = new int[levels];
    final JExpression[] target = new JExpression[levels];
//    InstructionHandle tblswitch = null;

    // Compile switch statement only if the key has multiple levels
    if (levels > 1) {
      // Put the parameter to the swtich statement on the stack
//      il.append(new ILOAD(extractMethod.getLocalIndex("level")));
      // Append the switch statement here later on
//      tblswitch = il.append(new NOP());o
    }

    // Append all the cases for the switch statment
    for (int level = 0; level < levels; level++) {
      match[level] = level;
      final Sort sort = sortObjects.get(level);
      target[level] = sort.compileSelect(nodeSortRecord, extractValueFromDOM);
//      il.append(ARETURN);
    }

    // Compile def. target for switch statement if key has multiple levels
    if (levels > 1) {
      // Append the default target - it will _NEVER_ be reached
//      final InstructionHandle defaultTarget = il.append(new PUSH(cpg, EMPTYSTRING));
//      il.insert(tblswitch, new TABLESWITCH(match, target, defaultTarget));
//      il.append(ARETURN);
    } else{
      JClass stringClass = nodeSortRecord.owner().ref(String.class);
      extractValueFromDOM.body()._return(stringClass.staticInvoke("valueOf").arg(target[0]));
    }
  }
}
