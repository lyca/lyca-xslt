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
import static com.sun.codemodel.JExpr._this;
import static com.sun.codemodel.JExpr.cast;
import static com.sun.codemodel.JExpr.invoke;
import static com.sun.codemodel.JExpr.lit;
import static de.lyca.xalan.xsltc.DOM.GET_AXIS_ITERATOR;
import static de.lyca.xalan.xsltc.DOM.GET_NAMESPACE_AXIS_ITERATOR;
import static de.lyca.xalan.xsltc.DOM.GET_NODE_VALUE_ITERATOR;
import static de.lyca.xalan.xsltc.DOM.GET_TYPED_AXIS_ITERATOR;
import static de.lyca.xalan.xsltc.compiler.Constants.TRANSLET_PNAME;

import java.util.List;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JExpression;

import de.lyca.xalan.xsltc.DOM;
import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;
import de.lyca.xalan.xsltc.dom.CurrentNodeListFilter;
import de.lyca.xalan.xsltc.dom.CurrentNodeListIterator;
import de.lyca.xalan.xsltc.dom.NthIterator;
import de.lyca.xalan.xsltc.dom.SingletonIterator;
import de.lyca.xml.dtm.Axis;
import de.lyca.xml.dtm.DTM;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 * @author Morten Jorgensen
 */
final class Step extends RelativeLocationPath {

  /**
   * This step's axis as defined in class Axis.
   */
  private Axis _axis;

  /**
   * A list of predicates (filters) defined on this step - may be null
   */
  private List<Predicate> _predicates;

  /**
   * Some simple predicates can be handled by this class (and not by the
   * Predicate class) and will be removed from the above list as they are
   * handled. We use this boolean to remember if we did have any predicates.
   */
  private boolean _hadPredicates = false;

  /**
   * Type of the node test.
   */
  private int _nodeType;

  public Step(Axis axis, int nodeType, List<Predicate> predicates) {
    _axis = axis;
    _nodeType = nodeType;
    _predicates = predicates;
  }

  /**
   * Set the parser for this element and all child predicates
   */
  @Override
  public void setParser(Parser parser) {
    super.setParser(parser);
    if (_predicates != null) {
      for (final Predicate exp : _predicates) {
        exp.setParser(parser);
        exp.setParent(this);
      }
    }
  }

  /**
   * Define the axis (defined in Axis class) for this step
   */
  @Override
  public Axis getAxis() {
    return _axis;
  }

  /**
   * Get the axis (defined in Axis class) for this step
   */
  @Override
  public void setAxis(Axis axis) {
    _axis = axis;
  }

  /**
   * Returns the node-type for this step
   */
  public int getNodeType() {
    return _nodeType;
  }

  /**
   * Returns the list containing all predicates for this step.
   */
  public List<Predicate> getPredicates() {
    return _predicates;
  }

  /**
   * Returns the list containing all predicates for this step.
   */
  public void addPredicates(List<Predicate> predicates) {
    if (_predicates == null) {
      _predicates = predicates;
    } else {
      _predicates.addAll(predicates);
    }
  }

  /**
   * Returns 'true' if this step has a parent pattern. This method will return
   * 'false' if this step occurs on its own under an element like <xsl:for-each>
   * or <xsl:apply-templates>.
   */
  private boolean hasParentPattern() {
    final SyntaxTreeNode parent = getParent();
    return parent instanceof ParentPattern || parent instanceof ParentLocationPath || parent instanceof UnionPathExpr
            || parent instanceof FilterParentPath;
  }

  /**
   * Returns 'true' if this step has any predicates
   */
  private boolean hasPredicates() {
    return _predicates != null && _predicates.size() > 0;
  }

  /**
   * True if this step is the abbreviated step '.'
   */
  public boolean isAbbreviatedDot() {
    return _nodeType == NodeTest.ANODE && _axis == Axis.SELF;
  }

  /**
   * True if this step is the abbreviated step '..'
   */
  public boolean isAbbreviatedDDot() {
    return _nodeType == NodeTest.ANODE && _axis == Axis.PARENT;
  }

  /**
   * Type check this step. The abbreviated steps '.' and '@attr' are assigned
   * type node if they have no predicates. All other steps have type node-set.
   */
  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {

    // Save this value for later - important for testing for special
    // combinations of steps and patterns than can be optimised
    _hadPredicates = hasPredicates();

    // Special case for '.'
    // in the case where '.' has a context such as book/.
    // or .[false()] we can not optimize the nodeset to a single node.
    if (isAbbreviatedDot()) {
      _type = hasParentPattern() || hasPredicates() ? Type.NodeSet : Type.Node;
    } else {
      _type = Type.NodeSet;
    }

    // Type check all predicates (expressions applied to the step)
    if (_predicates != null) {
      for (final Predicate pred : _predicates) {
        pred.typeCheck(stable);
      }
    }

    // Return either Type.Node or Type.NodeSet
    return _type;
  }

  public JExpression toJExpression(CompilerContext ctx) {
    final JClass axis = ctx.ref(Axis.class);
    JExpression invocation = null;

    if (hasPredicates()) {
      invocation = compilePredicates(ctx);
    } else {
      int star = 0;
      String name = null;
      final XSLTC xsltc = getParser().getXSLTC();

      if (_nodeType >= DTM.NTYPES) {
        final List<String> ni = xsltc.getNamesIndex();

        name = ni.get(_nodeType - DTM.NTYPES);
        star = name.lastIndexOf('*');
      }

      // If it is an attribute, but not '@*', '@pre:*' or '@node()',
      // and has no parent
      if (_axis == Axis.ATTRIBUTE && _nodeType != NodeTest.ATTRIBUTE && _nodeType != NodeTest.ANODE
          && !hasParentPattern() && star == 0) {
        return invoke(ctx.currentDom(), GET_TYPED_AXIS_ITERATOR).arg(axis.staticRef(Axis.ATTRIBUTE.name()))
            .arg(lit(_nodeType));
      }

      final SyntaxTreeNode parent = getParent();
      // Special case for '.'
      if (isAbbreviatedDot()) {
        if (_type == Type.Node) {
          // Put context node on stack if using Type.Node
          return ctx.currentNode();
        } else {
          if (parent instanceof ParentLocationPath) {
            // Wrap the context node in a singleton iterator if not.
            return _new(ctx.ref(SingletonIterator.class)).arg(ctx.currentNode());
          } else {
            // DOM.getAxisIterator(int axis);
            return ctx.currentDom().invoke(GET_AXIS_ITERATOR).arg(axis.staticRef(_axis.name()));
          }
        }
      }

      // Special case for /foo/*/bar
      if (parent instanceof ParentLocationPath && parent.getParent() instanceof ParentLocationPath) {
        if (_nodeType == NodeTest.ELEMENT && !_hadPredicates) {
          _nodeType = NodeTest.ANODE;
        }
      }

      // "ELEMENT" or "*" or "@*" or ".." or "@attr" with a parent.
      switch (_nodeType) {
      case NodeTest.ATTRIBUTE:
        _axis = Axis.ATTRIBUTE;
      case NodeTest.ANODE:
        // DOM.getAxisIterator(int axis);
        invocation = invoke(ctx.currentDom(), GET_AXIS_ITERATOR).arg(axis.staticRef(_axis.name()));
        break;
      default:
        if (star > 1) {
          final String namespace;
          if (_axis == Axis.ATTRIBUTE) {
            namespace = name.substring(0, star - 2);
          } else {
            namespace = name.substring(0, star - 1);
          }

          final int nsType = xsltc.registerNamespace(namespace);
          // DTMAxisIterator getNamespaceAxisIterator(final Axis axis, final int
          // ns);
          invocation = invoke(ctx.currentDom(), GET_NAMESPACE_AXIS_ITERATOR).arg(axis.staticRef(_axis.name()))
              .arg(lit(nsType));
          break;
        }
      case NodeTest.ELEMENT:
        // Get the typed iterator we're after
        // DOM.getTypedAxisIterator(int axis, int type);
        invocation = invoke(ctx.currentDom(), GET_TYPED_AXIS_ITERATOR).arg(axis.staticRef(_axis.name()))
            .arg(lit(_nodeType));
        break;
      }
    }
    return invocation;
  }

  /**
   * Translate a step by pushing the appropriate iterator onto the stack. The
   * abbreviated steps '.' and '@attr' do not create new iterators if they are
   * not part of a LocationPath and have no filters. In these cases a node index
   * instead of an iterator is pushed onto the stack.
   */
  @Override
  public void translate(CompilerContext ctx) {
// FIXME
//    final ConstantPoolGen cpg = classGen.getConstantPool();
//    final InstructionList il = methodGen.getInstructionList();
//    final InstructionFactory factory = new InstructionFactory(classGen, cpg);
//
//    if (hasPredicates()) {
//      translatePredicates(classGen, methodGen);
//    } else {
//      int star = 0;
//      String name = null;
//      final XSLTC xsltc = getParser().getXSLTC();
//
//      if (_nodeType >= DTM.NTYPES) {
//        final List<String> ni = xsltc.getNamesIndex();
//
//        name = ni.get(_nodeType - DTM.NTYPES);
//        star = name.lastIndexOf('*');
//      }
//
//      // If it is an attribute, but not '@*', '@pre:*' or '@node()',
//      // and has no parent
//      if (_axis == Axis.ATTRIBUTE && _nodeType != NodeTest.ATTRIBUTE && _nodeType != NodeTest.ANODE
//              && !hasParentPattern() && star == 0) {
//        final int iter = cpg.addInterfaceMethodref(DOM_INTF, "getTypedAxisIterator", "(Lde/lyca/xml/dtm/Axis;I)" + NODE_ITERATOR_SIG);
//        il.append(methodGen.loadDOM());
//        il.append(factory.createFieldAccess("de.lyca.xml.dtm.Axis", Axis.ATTRIBUTE.name(), Type.Axis.toJCType(), org.apache.bcel.Constants.GETSTATIC));
//        il.append(new PUSH(cpg, _nodeType));
//        il.append(new INVOKEINTERFACE(iter, 3));
//        return;
//      }
//
//      final SyntaxTreeNode parent = getParent();
//      // Special case for '.'
//      if (isAbbreviatedDot()) {
//        if (_type == Type.Node) {
//          // Put context node on stack if using Type.Node
//          il.append(methodGen.loadContextNode());
//        } else {
//          if (parent instanceof ParentLocationPath) {
//            // Wrap the context node in a singleton iterator if not.
//            final int init = cpg.addMethodref(SINGLETON_ITERATOR, "<init>", "(" + NODE_SIG + ")V");
//            il.append(new NEW(cpg.addClass(SINGLETON_ITERATOR)));
//            il.append(DUP);
//            il.append(methodGen.loadContextNode());
//            il.append(new INVOKESPECIAL(init));
//          } else {
//            // DOM.getAxisIterator(int axis);
//            final int git = cpg.addInterfaceMethodref(DOM_INTF, "getAxisIterator", "(Lde/lyca/xml/dtm/Axis;)" + NODE_ITERATOR_SIG);
//            il.append(methodGen.loadDOM());
//            il.append(factory.createFieldAccess("de.lyca.xml.dtm.Axis", _axis.name(), Type.Axis.toJCType(), org.apache.bcel.Constants.GETSTATIC));
//            il.append(new INVOKEINTERFACE(git, 2));
//          }
//        }
//        return;
//      }
//
//      // Special case for /foo/*/bar
//      if (parent instanceof ParentLocationPath && parent.getParent() instanceof ParentLocationPath) {
//        if (_nodeType == NodeTest.ELEMENT && !_hadPredicates) {
//          _nodeType = NodeTest.ANODE;
//        }
//      }
//
//      // "ELEMENT" or "*" or "@*" or ".." or "@attr" with a parent.
//      switch (_nodeType) {
//        case NodeTest.ATTRIBUTE:
//          _axis = Axis.ATTRIBUTE;
//        case NodeTest.ANODE:
//          // DOM.getAxisIterator(int axis);
//          final int git = cpg.addInterfaceMethodref(DOM_INTF, "getAxisIterator", "(Lde/lyca/xml/dtm/Axis;)" + NODE_ITERATOR_SIG);
//          il.append(methodGen.loadDOM());
//          il.append(factory.createFieldAccess("de.lyca.xml.dtm.Axis", _axis.name(), Type.Axis.toJCType(), org.apache.bcel.Constants.GETSTATIC));
//          il.append(new INVOKEINTERFACE(git, 2));
//          break;
//        default:
//          if (star > 1) {
//            final String namespace;
//            if (_axis == Axis.ATTRIBUTE) {
//              namespace = name.substring(0, star - 2);
//            } else {
//              namespace = name.substring(0, star - 1);
//            }
//
//            final int nsType = xsltc.registerNamespace(namespace);
//            final int ns = cpg.addInterfaceMethodref(DOM_INTF, "getNamespaceAxisIterator", "(Lde/lyca/xml/dtm/Axis;I)" + NODE_ITERATOR_SIG);
//            il.append(methodGen.loadDOM());
//            il.append(factory.createFieldAccess("de.lyca.xml.dtm.Axis", _axis.name(), Type.Axis.toJCType(), org.apache.bcel.Constants.GETSTATIC));
//            il.append(new PUSH(cpg, nsType));
//            il.append(new INVOKEINTERFACE(ns, 3));
//            break;
//          }
//        case NodeTest.ELEMENT:
//          // DOM.getTypedAxisIterator(int axis, int type);
//          final int ty = cpg.addInterfaceMethodref(DOM_INTF, "getTypedAxisIterator", "(Lde/lyca/xml/dtm/Axis;I)" + NODE_ITERATOR_SIG);
//          // Get the typed iterator we're after
//          il.append(methodGen.loadDOM());
//          il.append(factory.createFieldAccess("de.lyca.xml.dtm.Axis", _axis.name(), Type.Axis.toJCType(), org.apache.bcel.Constants.GETSTATIC));
//          il.append(new PUSH(cpg, _nodeType));
//          il.append(new INVOKEINTERFACE(ty, 3));
//
//          break;
//      }
//    }
  }

  /**
   * Translate a sequence of predicates. Each predicate is translated by
   * constructing an instance of <code>CurrentNodeListIterator</code> which is
   * initialized from another iterator (recursive call), a filter and a closure
   * (call to translate on the predicate) and "this".
   * @param ctx TODO
   */
  public JExpression compilePredicates(CompilerContext ctx) {
    int idx = 0;

    if (_predicates.size() == 0) {
      return toJExpression(ctx);
    } else {
      final Predicate predicate = _predicates.get(_predicates.size() - 1);
      _predicates.remove(predicate);

      // Special case for predicates that can use the NodeValueIterator
      // instead of an auxiliary class. Certain path/predicates pairs
      // are translated into a base path, on top of which we place a
      // node value iterator that tests for the desired value:
      // foo[@attr = 'str'] -> foo/@attr + test(value='str')
      // foo[bar = 'str'] -> foo/bar + test(value='str')
      // foo/bar[. = 'str'] -> foo/bar + test(value='str')
      if (predicate.isNodeValueTest()) {
        final Step step = predicate.getStep();

//        il.append(methodGen.loadDOM());
        // If the predicate's Step is simply '.' we translate this Step
        // and place the node test on top of the resulting iterator
        JExpression iter;
        int rType;
        if (step.isAbbreviatedDot()) {
          iter = toJExpression(ctx);
          rType=DOM.RETURN_CURRENT;
//          il.append(new ICONST(DOM.RETURN_CURRENT));
        }
        // Otherwise we create a parent location path with this Step and
        // the predicates Step, and place the node test on top of that
        else {
          final ParentLocationPath path = new ParentLocationPath(this, step);
          try {
            path.typeCheck(getParser().getSymbolTable());
          } catch (final TypeCheckError e) {
          }
          iter =path.toJExpression(ctx);
          rType=DOM.RETURN_PARENT;
//          il.append(new ICONST(DOM.RETURN_PARENT));
        }
//        predicate.translate(definedClass, method);
        return invoke(ctx.currentDom(), GET_NODE_VALUE_ITERATOR).arg(iter).arg(lit(rType)).arg(predicate.toJExpression(ctx)).arg(lit(((EqualityExpr)predicate.getExpr()).getOp()));
//        idx = cpg.addInterfaceMethodref(DOM_INTF, GET_NODE_VALUE_ITERATOR, GET_NODE_VALUE_ITERATOR_SIG);
//        il.append(new INVOKEINTERFACE(idx, 5));
      }
      // Handle '//*[n]' expression
      else if (predicate.isNthDescendant()) {
        // FIXME
//        return invoke(method.listParams()[0], "getNthDescendant").arg(lit(NodeTest.ELEMENT)).arg(lit(rType)).arg(predicate.compile(definedClass, method)).arg(lit(((EqualityExpr)predicate.getExpr()).getOp()));

        //        il.append(methodGen.loadDOM());
//        // il.append(new ICONST(NodeTest.ELEMENT));
//        il.append(new ICONST(predicate.getPosType()));
//        predicate.translate(ctx);
//        il.append(new ICONST(0));
//        idx = cpg.addInterfaceMethodref(DOM_INTF, "getNthDescendant", "(IIZ)" + NODE_ITERATOR_SIG);
//        il.append(new INVOKEINTERFACE(idx, 4));
      }
      // Handle 'elem[n]' expression
      else if (predicate.isNthPositionFilter()) {
        return _new(ctx.ref(NthIterator.class)).arg(compilePredicates(ctx)).arg(predicate.toJExpression(ctx));
//        idx = cpg.addMethodref(NTH_ITERATOR_CLASS, "<init>", "(" + NODE_ITERATOR_SIG + "I)V");

        // Backwards branches are prohibited if an uninitialized object
        // is on the stack by section 4.9.4 of the JVM Specification,
        // 2nd Ed. We don't know whether this code might contain
        // backwards branches, so we mustn't create the new object until
        // after we've created the suspect arguments to its constructor.
        // Instead we calculate the values of the arguments to the
        // constructor first, store them in temporary variables, create
        // the object and reload the arguments from the temporaries to
        // avoid the problem.
//        compilePredicates(definedClass, method); // recursive call
//        final LocalVariableGen iteratorTemp = methodGen.addLocalVariable("step_tmp1",
//                Util.getJCRefType(NODE_ITERATOR_SIG), null, null);
//        iteratorTemp.setStart(il.append(new ASTORE(iteratorTemp.getIndex())));

//        predicate.compile(definedClass, method);
//        final LocalVariableGen predicateValueTemp = methodGen.addLocalVariable("step_tmp2", Util.getJCRefType("I"),
//                null, null);
//        predicateValueTemp.setStart(il.append(new ISTORE(predicateValueTemp.getIndex())));

//        il.append(new NEW(cpg.addClass(NTH_ITERATOR_CLASS)));
//        il.append(DUP);
//        iteratorTemp.setEnd(il.append(new ALOAD(iteratorTemp.getIndex())));
//        predicateValueTemp.setEnd(il.append(new ILOAD(predicateValueTemp.getIndex())));
//        il.append(new INVOKESPECIAL(idx));
      } else {
//        idx = cpg.addMethodref(CURRENT_NODE_LIST_ITERATOR, "<init>", "(" + NODE_ITERATOR_SIG
//                + CURRENT_NODE_LIST_FILTER_SIG + NODE_SIG + TRANSLET_SIG + ")V");

        // Backwards branches are prohibited if an uninitialized object
        // is on the stack by section 4.9.4 of the JVM Specification,
        // 2nd Ed. We don't know whether this code might contain
        // backwards branches, so we mustn't create the new object until
        // after we've created the suspect arguments to its constructor.
        // Instead we calculate the values of the arguments to the
        // constructor first, store them in temporary variables, create
        // the object and reload the arguments from the temporaries to
        // avoid the problem.
        // public CurrentNodeListIterator(DTMAxisIterator source, CurrentNodeListFilter filter, int currentNode, AbstractTranslet translet)

        JExpression recursive = compilePredicates(ctx); // recursive call
        JExpression currentPredicate = predicate.toJExpression(ctx);
//        final LocalVariableGen iteratorTemp = methodGen.addLocalVariable("step_tmp1",
//                Util.getJCRefType(NODE_ITERATOR_SIG), null, null);
//        iteratorTemp.setStart(il.append(new ASTORE(iteratorTemp.getIndex())));
        // FIXME predicate.translateFilter(ctx.clazz(), ctx.currentMethod());
        JExpression translet;
        if (ctx.isInnerClass()) {
          translet = cast(ctx.clazz().outer(), ctx.param(TRANSLET_PNAME));
        } else {
          translet = _this();
        }
        JExpression current;
        if (ctx.ref(CurrentNodeListFilter.class).isAssignableFrom(ctx.clazz())) {
          current = ctx.param("current");
        } else {
          current = ctx.currentNode();
        }
        return _new(ctx.ref(CurrentNodeListIterator.class)).arg(recursive).arg(currentPredicate).arg(current)
            .arg(translet);
//        final LocalVariableGen filterTemp = methodGen.addLocalVariable("step_tmp2",
//                Util.getJCRefType(CURRENT_NODE_LIST_FILTER_SIG), null, null);
//        filterTemp.setStart(il.append(new ASTORE(filterTemp.getIndex())));

        // create new CurrentNodeListIterator
//        il.append(new NEW(cpg.addClass(CURRENT_NODE_LIST_ITERATOR)));
//        il.append(DUP);

//        iteratorTemp.setEnd(il.append(new ALOAD(iteratorTemp.getIndex())));
//        filterTemp.setEnd(il.append(new ALOAD(filterTemp.getIndex())));

//        il.append(methodGen.loadCurrentNode());
//        il.append(classGen.loadTranslet());
//        if (classGen.isExternal()) {
//          final String className = classGen.getClassName();
//          il.append(new CHECKCAST(cpg.addClass(className)));
//        }
//        il.append(new INVOKESPECIAL(idx));
      }
    }
    return null;
  }

  /**
   * Returns a string representation of this step.
   */
  @Override
  public String toString() {
    final StringBuilder buffer = new StringBuilder("step(\"");
    buffer.append(_axis.getName()).append("\", ").append(_nodeType);
    if (_predicates != null) {
      for (final Predicate pred : _predicates) {
        buffer.append(", ").append(pred.toString());
      }
    }
    return buffer.append(')').toString();
  }
}
