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

import static com.sun.codemodel.JExpr._null;
import static com.sun.codemodel.JExpr._this;
import static com.sun.codemodel.JExpr.lit;
import static com.sun.codemodel.JMod.FINAL;
import static com.sun.codemodel.JMod.PUBLIC;
import static com.sun.codemodel.JMod.STATIC;
import static de.lyca.xalan.xsltc.compiler.Constants.CHARACTERSW;
import static de.lyca.xalan.xsltc.compiler.Constants.ITERATOR_PNAME;
import static de.lyca.xml.dtm.DTMAxisIterator.SET_START_NODE;

import java.util.ArrayList;
import java.util.List;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JVar;

import de.lyca.xalan.xsltc.DOM;
import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.RealType;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;
import de.lyca.xalan.xsltc.dom.AnyNodeCounter;
import de.lyca.xalan.xsltc.dom.MultipleNodeCounter;
import de.lyca.xalan.xsltc.dom.NodeCounter;
import de.lyca.xalan.xsltc.dom.SingleNodeCounter;
import de.lyca.xalan.xsltc.runtime.AbstractTranslet;
import de.lyca.xml.dtm.DTMAxisIterator;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 */
final class Number extends Instruction implements Closure {
  private static final int LEVEL_SINGLE = 0;
  private static final int LEVEL_MULTIPLE = 1;
  private static final int LEVEL_ANY = 2;

  static final private Class<?>[] CLASS_NAMES = { SingleNodeCounter.class, // LEVEL_SINGLE
      MultipleNodeCounter.class, // LEVEL_MULTIPLE
      AnyNodeCounter.class // LEVEL_ANY
  };

  static final private String[] FIELD_NAMES = { "___single_node_counter", // LEVEL_SINGLE
      "___multiple_node_counter", // LEVEL_MULTIPLE
      "___any_node_counter" // LEVEL_ANY
  };

  private Pattern _from = null;
  private Pattern _count = null;
  private Expression _value = null;

  private AttributeValueTemplate _lang = null;
  private AttributeValueTemplate _format = null;
  private AttributeValueTemplate _letterValue = null;
  private AttributeValueTemplate _groupingSeparator = null;
  private AttributeValueTemplate _groupingSize = null;

  private int _level = LEVEL_SINGLE;
  private boolean _formatNeeded = false;

  private String _className = null;
  private List<VariableRefBase> _closureVars = null;

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
    }
  }

  // -- End Closure interface ----------------------

  @Override
  public void parseContents(Parser parser) {
    final int count = _attributes.getLength();

    for (int i = 0; i < count; i++) {
      final String name = _attributes.getQName(i);
      final String value = _attributes.getValue(i);

      if (name.equals("value")) {
        _value = parser.parseExpression(this, name, null);
      } else if (name.equals("count")) {
        _count = parser.parsePattern(this, name, null);
      } else if (name.equals("from")) {
        _from = parser.parsePattern(this, name, null);
      } else if (name.equals("level")) {
        if (value.equals("single")) {
          _level = LEVEL_SINGLE;
        } else if (value.equals("multiple")) {
          _level = LEVEL_MULTIPLE;
        } else if (value.equals("any")) {
          _level = LEVEL_ANY;
        }
      } else if (name.equals("format")) {
        _format = new AttributeValueTemplate(value, parser, this);
        _formatNeeded = true;
      } else if (name.equals("lang")) {
        _lang = new AttributeValueTemplate(value, parser, this);
        _formatNeeded = true;
      } else if (name.equals("letter-value")) {
        _letterValue = new AttributeValueTemplate(value, parser, this);
        _formatNeeded = true;
      } else if (name.equals("grouping-separator")) {
        _groupingSeparator = new AttributeValueTemplate(value, parser, this);
        _formatNeeded = true;
      } else if (name.equals("grouping-size")) {
        _groupingSize = new AttributeValueTemplate(value, parser, this);
        _formatNeeded = true;
      }
    }
  }

  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {
    if (_value != null) {
      final Type tvalue = _value.typeCheck(stable);
      if (tvalue instanceof RealType == false) {
        _value = new CastExpr(_value, Type.Real);
      }
    }
    if (_count != null) {
      _count.typeCheck(stable);
    }
    if (_from != null) {
      _from.typeCheck(stable);
    }
    if (_format != null) {
      _format.typeCheck(stable);
    }
    if (_lang != null) {
      _lang.typeCheck(stable);
    }
    if (_letterValue != null) {
      _letterValue.typeCheck(stable);
    }
    if (_groupingSeparator != null) {
      _groupingSeparator.typeCheck(stable);
    }
    if (_groupingSize != null) {
      _groupingSize.typeCheck(stable);
    }
    return Type.Void;
  }

  /**
   * True if the has specified a value for this instance of number.
   */
  public boolean hasValue() {
    return _value != null;
  }

  /**
   * Returns <tt>true</tt> if this instance of number has neither a from nor a
   * count pattern.
   */
  public boolean isDefault() {
    return _from == null && _count == null;
  }

  private JVar compileDefault(CompilerContext ctx) {
 // FIXME
//    int index;
//    final ConstantPoolGen cpg = classGen.getConstantPool();
//    final InstructionList il = methodGen.getInstructionList();

    final JFieldVar[] fieldIndexes = getXSLTC().getNumberFieldIndexes();

    if (fieldIndexes[_level] == null) {
//      final Field defaultNode = new Field(ACC_PRIVATE, cpg.addUtf8(FieldNames[_level]), cpg.addUtf8(NODE_COUNTER_SIG),
//              null, cpg.getConstantPool());
      // Add a new private field to this class
//      classGen.addField(defaultNode);
      fieldIndexes[_level] = ctx.addPrivateField(NodeCounter.class, FIELD_NAMES[_level]);

      // Get a reference to the newly added field
//      fieldIndexes[_level] = cpg.addFieldref(classGen.getClassName(), FieldNames[_level], NODE_COUNTER_SIG);
    }

    // Check if field is initialized (runtime)
//    il.append(classGen.loadTranslet());
//    il.append(new GETFIELD(fieldIndexes[_level]));
//    final BranchHandle ifBlock1 = il.append(new IFNONNULL(null));

    JBlock _then =  ctx.currentBlock()._if(fieldIndexes[_level].eq(_null()))._then();
    // Create an instance of DefaultNodeCounter
//    index = cpg.addMethodref(ClassNames[_level], "getDefaultNodeCounter", "(" + TRANSLET_INTF_SIG + DOM_INTF_SIG
//            + NODE_ITERATOR_SIG + ")" + NODE_COUNTER_SIG);
//    il.append(classGen.loadTranslet());
//    il.append(methodGen.loadDOM());
//    il.append(methodGen.loadIterator());
//    il.append(new INVOKESTATIC(index));
//    il.append(DUP);

    // Store the node counter in the field
//    il.append(classGen.loadTranslet());
//    il.append(SWAP);
//    il.append(new PUTFIELD(fieldIndexes[_level]));
//    final BranchHandle ifBlock2 = il.append(new GOTO(null));

    _then.assign(
        fieldIndexes[_level],
        ctx.ref(CLASS_NAMES[_level]).staticInvoke("getDefaultNodeCounter").arg(_this()).arg(ctx.currentDom())
            .arg(ctx.param(ITERATOR_PNAME)));

    return fieldIndexes[_level];
    // Backpatch conditionals
//    ifBlock1.setTarget(il.append(classGen.loadTranslet()));
//    il.append(new GETFIELD(fieldIndexes[_level]));
//
//    ifBlock2.setTarget(il.append(NOP));
  }

  /**
   * Compiles a constructor for the class <tt>_className</tt> that inherits from
   * {Any,Single,Multiple}NodeCounter. This constructor simply calls the same
   * constructor in the super class.
   */
  private void compileConstructor(CompilerContext nodeCounterCtx) {
    JMethod constructor = nodeCounterCtx.clazz().constructor(PUBLIC);
    JVar translet = constructor.param(AbstractTranslet.class, "translet");
    JVar dom = constructor.param(DOM.class, "document");
    JVar iterator = constructor.param(DTMAxisIterator.class, "iterator");
    constructor.body().invoke("super").arg(translet).arg(dom).arg(iterator);

//    MethodGenerator cons;
//    final InstructionList il = new InstructionList();
//    final ConstantPoolGen cpg = classGen.getConstantPool();
//
//    cons = new MethodGenerator(ACC_PUBLIC, org.apache.bcel.generic.Type.VOID,
//            new org.apache.bcel.generic.Type[] { Util.getJCRefType(TRANSLET_INTF_SIG), Util.getJCRefType(DOM_INTF_SIG),
//                    Util.getJCRefType(NODE_ITERATOR_SIG) }, new String[] { "dom", "translet", "iterator" }, "<init>",
//            _className, il, cpg);
//
//    il.append(ALOAD_0); // this
//    il.append(ALOAD_1); // translet
//    il.append(ALOAD_2); // DOM
//    il.append(new ALOAD(3));// iterator
//
//    final int index = cpg.addMethodref(ClassNames[_level], "<init>", "(" + TRANSLET_INTF_SIG + DOM_INTF_SIG
//            + NODE_ITERATOR_SIG + ")V");
//    il.append(new INVOKESPECIAL(index));
//    il.append(RETURN);
//
//    classGen.addMethod(cons);
  }

//  /**
//   * This method compiles code that is common to matchesFrom() and
//   * matchesCount() in the auxillary class.
//   */
//  private void compileLocals(NodeCounterGenerator nodeCounterGen, MatchGenerator matchGen, InstructionList il) {
//    int field;
//    LocalVariableGen local;
//    final ConstantPoolGen cpg = nodeCounterGen.getConstantPool();
//
//    // Get NodeCounter._iterator and store locally
//    local = matchGen.addLocalVariable("iterator", Util.getJCRefType(NODE_ITERATOR_SIG), null, null);
//    field = cpg.addFieldref(NODE_COUNTER, "_iterator", ITERATOR_FIELD_SIG);
//    il.append(ALOAD_0); // 'this' pointer on stack
//    il.append(new GETFIELD(field));
//    local.setStart(il.append(new ASTORE(local.getIndex())));
//    matchGen.setIteratorIndex(local.getIndex());
//
//    // Get NodeCounter._translet and store locally
//    local = matchGen.addLocalVariable("translet", Util.getJCRefType(TRANSLET_SIG), null, null);
//    field = cpg.addFieldref(NODE_COUNTER, "_translet", "Lde/lyca/xalan/xsltc/Translet;");
//    il.append(ALOAD_0); // 'this' pointer on stack
//    il.append(new GETFIELD(field));
//    il.append(new CHECKCAST(cpg.addClass(TRANSLET_CLASS)));
//    local.setStart(il.append(new ASTORE(local.getIndex())));
//    nodeCounterGen.setTransletIndex(local.getIndex());
//
//    // Get NodeCounter._document and store locally
//    local = matchGen.addLocalVariable("document", Util.getJCRefType(DOM_INTF_SIG), null, null);
//    field = cpg.addFieldref(_className, "_document", DOM_INTF_SIG);
//    il.append(ALOAD_0); // 'this' pointer on stack
//    il.append(new GETFIELD(field));
//    // Make sure we have the correct DOM type on the stack!!!
//    local.setStart(il.append(new ASTORE(local.getIndex())));
//    matchGen.setDomIndex(local.getIndex());
//  }

  private JExpression compilePatterns(CompilerContext ctx) {
 // FIXME
//    MatchGenerator matchGen;
//    NodeCounterGenerator nodeCounterGen;
//
    _className = ctx.xsltc().getHelperClassName();
//    nodeCounterGen = new NodeCounterGenerator(_className, ClassNames[_level], toString(), ACC_PUBLIC | ACC_SUPER, null,
//            classGen.getStylesheet());
//    InstructionList il = null;
//    ConstantPoolGen cpg = nodeCounterGen.getConstantPool();
    CompilerContext nodeCounterCtx;
    JDefinedClass nodeCounter;
    try {
      nodeCounter = ctx.clazz()._class(PUBLIC | STATIC | FINAL, _className)._extends(CLASS_NAMES[_level]);
    } catch (JClassAlreadyExistsException e) {
      throw new RuntimeException(e);
    }
    nodeCounterCtx = new CompilerContext(ctx.owner(), nodeCounter, ctx.stylesheet(), ctx.xsltc());

    // Add a new instance variable for each var in closure
    final int closureLen = _closureVars == null ? 0 : _closureVars.size();

    for (int i = 0; i < closureLen; i++) {
      final VariableBase var = _closureVars.get(i).getVariable();
      nodeCounterCtx.field(JMod.PUBLIC, var.getType().toJCType(), var.getEscapedName());
//      nodeCounterGen.addField(new Field(ACC_PUBLIC, cpg.addUtf8(var.getEscapedName()), cpg.addUtf8(var.getType()
//              .toSignature()), null, cpg.getConstantPool()));
    }

    // Add a single constructor to the class
    compileConstructor(nodeCounterCtx);

    /*
     * Compile method matchesFrom()
     */
    if (_from != null) {
      JMethod matchesFrom = nodeCounterCtx.method(PUBLIC | FINAL, boolean.class, "matchesFrom");
      JVar node = nodeCounterCtx.param(ctx.owner().INT, "node");
      nodeCounterCtx.pushBlock(matchesFrom.body());
      nodeCounterCtx.pushNode(node);
//      il = new InstructionList();
//      matchGen = new MatchGenerator(ACC_PUBLIC | ACC_FINAL, org.apache.bcel.generic.Type.BOOLEAN,
//          new org.apache.bcel.generic.Type[] { org.apache.bcel.generic.Type.INT, }, new String[] { "node", },
//          "matchesFrom", _className, il, cpg);

//      compileLocals(nodeCounterGen, matchGen, il);

      // Translate Pattern
//      il.append(matchGen.loadContextNode());
      nodeCounterCtx.currentBlock()._return(_from.toJExpression(nodeCounterCtx));
//      _from.synthesize(nodeCounterGen, matchGen);
//      il.append(IRETURN);

//      nodeCounterGen.addMethod(matchGen);
      nodeCounterCtx.popNode();
      nodeCounterCtx.popBlock();
    }

    /*
     * Compile method matchesCount()
     */
    if (_count != null) {
      JMethod matchesCount = nodeCounterCtx.method(PUBLIC | FINAL, boolean.class, "matchesCount");
      JVar node = nodeCounterCtx.param(ctx.owner().INT, "node");
      nodeCounterCtx.pushBlock(matchesCount.body());
      nodeCounterCtx.pushNode(node);
//      il = new InstructionList();
//      matchGen = new MatchGenerator(ACC_PUBLIC | ACC_FINAL, org.apache.bcel.generic.Type.BOOLEAN,
//              new org.apache.bcel.generic.Type[] { org.apache.bcel.generic.Type.INT, }, new String[] { "node", },
//              "matchesCount", _className, il, cpg);

//      compileLocals(nodeCounterGen, matchGen, il);

      // Translate Pattern
//      il.append(matchGen.loadContextNode());
      nodeCounterCtx.currentBlock()._return(_count.toJExpression(nodeCounterCtx));
//      _count.translate(nodeCounterCtx);
//      _count.synthesize(nodeCounterGen, matchGen);

//      il.append(IRETURN);

//      nodeCounterGen.addMethod(matchGen);
      nodeCounterCtx.popNode();
      nodeCounterCtx.popBlock();
    }

//    getXSLTC().dumpClass(nodeCounterGen.getJavaClass());
//
//    // Push an instance of the newly created class
//    cpg = classGen.getConstantPool();
//    il = methodGen.getInstructionList();
//
//    final int index = cpg.addMethodref(_className, "<init>", "(" + TRANSLET_INTF_SIG + DOM_INTF_SIG + NODE_ITERATOR_SIG
//            + ")V");
//    il.append(new NEW(cpg.addClass(_className)));
//    il.append(DUP);
//    il.append(classGen.loadTranslet());
//    il.append(methodGen.loadDOM());
//    il.append(methodGen.loadIterator());
//    il.append(new INVOKESPECIAL(index));
//
//    // Initialize closure variables
//    for (int i = 0; i < closureLen; i++) {
//      final VariableRefBase varRef = _closureVars.get(i);
//      final VariableBase var = varRef.getVariable();
//      final Type varType = var.getType();
//
//      // Store variable in new closure
//      il.append(DUP);
//      il.append(var.loadInstruction());
//      il.append(new PUTFIELD(cpg.addFieldref(_className, var.getEscapedName(), varType.toSignature())));
//    }
    return JExpr._new(nodeCounter).arg(JExpr._this()).arg(ctx.currentDom()).arg(ctx.param(ITERATOR_PNAME));
  }

  @Override
  public void translate(CompilerContext ctx) {
 // FIXME
//    int index;
//    final ConstantPoolGen cpg = classGen.getConstantPool();
//    final InstructionList il = methodGen.getInstructionList();

    // Push "this" for the call to characters()
//    il.append(classGen.loadTranslet());

    JExpression numberFieldIndex = null;
    if (hasValue()) {
//      compileDefault(classGen, methodGen);
//      _value.translate(classGen, methodGen);
      JExpression value = ctx.ref(Math.class).staticInvoke("floor").arg(JExpr.lit(0.5).plus(_value.toJExpression(ctx)));
      // Using java.lang.Math.floor(number + 0.5) to return a double value
//      il.append(new PUSH(cpg, 0.5));
//      il.append(DADD);
//      index = cpg.addMethodref(MATH_CLASS, "floor", "(D)D");
//      il.append(new INVOKESTATIC(index));

      // Call setValue on the node counter
//      index = cpg.addMethodref(NODE_COUNTER, "setValue", "(D)" + NODE_COUNTER_SIG);
//      il.append(new INVOKEVIRTUAL(index));
      numberFieldIndex = compileDefault(ctx).invoke("setValue").arg(value);
    } else if (isDefault()) {
      numberFieldIndex = compileDefault(ctx);
    } else {
      numberFieldIndex = compilePatterns(ctx);
    }

    // Call setStartNode()
    if (!hasValue()) {
      numberFieldIndex = numberFieldIndex.invoke(SET_START_NODE).arg(ctx.currentNode());
//      il.append(methodGen.loadContextNode());
//      index = cpg.addMethodref(NODE_COUNTER, SET_START_NODE, "(I)" + NODE_COUNTER_SIG);
//      il.append(new INVOKEVIRTUAL(index));
    }

    // Call getCounter() with or without args
    if (_formatNeeded) {
      JExpression format;
      if (_format != null) {
        format = _format.toJExpression(ctx);
      } else {
        format = lit("1");
      }

      JExpression lang;
      if (_lang != null) {
        lang = _lang.toJExpression(ctx);
      } else {
        lang = lit("en"); // TODO ??
      }

      JExpression letterValue;
      if (_letterValue != null) {
        letterValue = _letterValue.toJExpression(ctx);
      } else {
        letterValue = lit("");
      }

      JExpression groupingSeparator;
      if (_groupingSeparator != null) {
        groupingSeparator = _groupingSeparator.toJExpression(ctx);
      } else {
        groupingSeparator = lit("");
      }

      JExpression groupingSize;
      if (_groupingSize != null) {
        groupingSize = _groupingSize.toJExpression(ctx);
      } else {
        groupingSize = lit("0");
      }

      ctx.currentBlock()
          .invoke(CHARACTERSW)
          .arg(
              numberFieldIndex.invoke("getCounter").arg(format).arg(lang).arg(letterValue).arg(groupingSeparator)
                  .arg(groupingSize)).arg(ctx.currentHandler());

//      index = cpg.addMethodref(NODE_COUNTER, "getCounter", "(" + STRING_SIG + STRING_SIG + STRING_SIG + STRING_SIG
//              + STRING_SIG + ")" + STRING_SIG);
//      il.append(new INVOKEVIRTUAL(index));
    } else {
      ctx.currentBlock().invoke(CHARACTERSW).arg(numberFieldIndex.invoke("setDefaultFormatting").invoke("getCounter"))
          .arg(ctx.currentHandler());
      //      index = cpg.addMethodref(NODE_COUNTER, "setDefaultFormatting", "()" + NODE_COUNTER_SIG);
//      il.append(new INVOKEVIRTUAL(index));

//      index = cpg.addMethodref(NODE_COUNTER, "getCounter", "()" + STRING_SIG);
//      il.append(new INVOKEVIRTUAL(index));
    }

    // Output the resulting string to the handler
//    il.append(methodGen.loadHandler());
//    index = cpg.addMethodref(TRANSLET_CLASS, CHARACTERSW, CHARACTERSW_SIG);
//    il.append(new INVOKEVIRTUAL(index));
  }
}
