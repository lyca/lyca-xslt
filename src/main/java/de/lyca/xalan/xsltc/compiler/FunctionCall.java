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
import static de.lyca.xalan.xsltc.compiler.Constants.ERROR;
import static de.lyca.xalan.xsltc.compiler.Constants.TRANSLET_URI;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;

import de.lyca.xalan.ObjectFactory;
import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.ErrorMsg;
import de.lyca.xalan.xsltc.compiler.util.Messages;
import de.lyca.xalan.xsltc.compiler.util.MethodType;
import de.lyca.xalan.xsltc.compiler.util.ObjectType;
import de.lyca.xalan.xsltc.compiler.util.ReferenceType;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;
import de.lyca.xalan.xsltc.runtime.BasisLibrary;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 * @author Morten Jorgensen
 * @author Erwin Bolwidt {@literal <ejb@klomp.org>}
 * @author Todd Miller
 */
class FunctionCall extends Expression {

  // Name of this function call
  private QName _fname;
  // Arguments to this function call (might not be any)
  private final List<Expression> _arguments;
  // Empty argument list, used for certain functions
  private final static List<Expression> EMPTY_ARG_LIST = Collections.emptyList();

  // Valid namespaces for Java function-call extension
  protected final static String EXT_XSLTC = TRANSLET_URI;

  protected final static String JAVA_EXT_XSLTC = EXT_XSLTC + "/java";

  protected final static String EXT_XALAN = "http://xml.apache.org/xalan";

  protected final static String JAVA_EXT_XALAN = "http://xml.apache.org/xalan/java";

  protected final static String JAVA_EXT_XALAN_OLD = "http://xml.apache.org/xslt/java";

  protected final static String EXSLT_COMMON = "http://exslt.org/common";

  protected final static String EXSLT_MATH = "http://exslt.org/math";

  protected final static String EXSLT_SETS = "http://exslt.org/sets";

  protected final static String EXSLT_DATETIME = "http://exslt.org/dates-and-times";

  protected final static String EXSLT_STRINGS = "http://exslt.org/strings";

  // Namespace format constants
  protected final static int NAMESPACE_FORMAT_JAVA = 0;
  protected final static int NAMESPACE_FORMAT_CLASS = 1;
  protected final static int NAMESPACE_FORMAT_PACKAGE = 2;
  protected final static int NAMESPACE_FORMAT_CLASS_OR_PACKAGE = 3;

  // Namespace format
  private int _namespace_format = NAMESPACE_FORMAT_JAVA;

  /**
   * Stores reference to object for non-static Java calls
   */
  Expression _thisArgument = null;

  // External Java function's class/method/signature
  private String _className;
  private Class<?> _clazz;
  private Method _chosenMethod;
  private Constructor<?> _chosenConstructor;
  private MethodType _chosenMethodType;

  // Encapsulates all unsupported external function calls
  private boolean unresolvedExternal;

  // If FunctionCall is a external java constructor
  private boolean _isExtConstructor = false;

  // If the java method is static
  private boolean _isStatic = false;

  // Legal conversions between internal and Java types.
  private static final Map<Type, Set<JavaType>> _internal2Java = new HashMap<>();

  // Legal conversions between Java and internal types.
  private static final Map<Class<?>, Type> _java2Internal = new HashMap<>();

  // The mappings between EXSLT extension namespaces and implementation classes
  private static final Map<String, String> _extensionNamespaceTable = new HashMap<>();

  // Extension functions that are implemented in BasisLibrary
  private static final Map<String, String> _extensionFunctionTable = new HashMap<>();

  /**
   * inner class to used in internal2Java mappings, contains the Java type and
   * the distance between the internal type and the Java type.
   */
  static class JavaType {
    public Class<?> type;
    public int distance;

    public JavaType(Class<?> type, int distance) {
      this.type = type;
      this.distance = distance;
    }

    @Override
    public boolean equals(Object query) {
      return query.equals(type);
    }
  }

  /**
   * Defines 2 conversion tables: 1. From internal types to Java types and 2.
   * From Java types to internal types. These two tables are used when calling
   * external (Java) functions.
   */
  static {
    try {
      final Class<?> nodeClass = Class.forName("org.w3c.dom.Node");
      final Class<?> nodeListClass = Class.forName("org.w3c.dom.NodeList");

      // -- Internal to Java --------------------------------------------

      // Type.Boolean -> { boolean(0), Boolean(1), Object(2) }
      _internal2Java.put(Type.Boolean, new HashSet<>(Arrays.asList(new JavaType[] { new JavaType(Boolean.TYPE, 0),
          new JavaType(Boolean.class, 1), new JavaType(Object.class, 3) })));

      // Type.Real -> { double(0), Double(1), float(2), long(3), int(4),
      // short(5), byte(6), char(7), Object(8) }
      _internal2Java
          .put(Type.Real,
              new HashSet<>(Arrays.asList(new JavaType[] { new JavaType(Double.TYPE, 0), new JavaType(Double.class, 1),
                  new JavaType(Float.TYPE, 2), new JavaType(Long.TYPE, 3), new JavaType(Integer.TYPE, 4),
                  new JavaType(Short.TYPE, 5), new JavaType(Byte.TYPE, 6), new JavaType(Character.TYPE, 7),
                  new JavaType(Object.class, 8) })));

      // Type.Int must be the same as Type.Real
      _internal2Java
          .put(Type.Int,
              new HashSet<>(Arrays.asList(new JavaType[] { new JavaType(Double.TYPE, 0), new JavaType(Double.class, 1),
                  new JavaType(Float.TYPE, 2), new JavaType(Long.TYPE, 3), new JavaType(Integer.TYPE, 4),
                  new JavaType(Short.TYPE, 5), new JavaType(Byte.TYPE, 6), new JavaType(Character.TYPE, 7),
                  new JavaType(Object.class, 8) })));

      // Type.String -> { String(0), Object(1) }
      _internal2Java.put(Type.String, new HashSet<>(
          Arrays.asList(new JavaType[] { new JavaType(String.class, 0), new JavaType(Object.class, 1) })));

      // Type.NodeSet -> { NodeList(0), Node(1), Object(2), String(3) }
      _internal2Java.put(Type.NodeSet, new HashSet<>(Arrays.asList(new JavaType[] { new JavaType(nodeListClass, 0),
          new JavaType(nodeClass, 1), new JavaType(Object.class, 2), new JavaType(String.class, 3) })));

      // Type.Node -> { Node(0), NodeList(1), Object(2), String(3) } TODO
      // switched nodeClass and nodeListClass
      _internal2Java.put(Type.Node, new HashSet<>(Arrays.asList(new JavaType[] { new JavaType(nodeClass, 0),
          new JavaType(nodeListClass, 1), new JavaType(Object.class, 2), new JavaType(String.class, 3) })));

      // Type.ResultTree -> { NodeList(0), Node(1), Object(2), String(3) }
      _internal2Java.put(Type.ResultTree, new HashSet<>(Arrays.asList(new JavaType[] { new JavaType(nodeListClass, 0),
          new JavaType(nodeClass, 1), new JavaType(Object.class, 2), new JavaType(String.class, 3) })));

      _internal2Java.put(Type.Reference,
          new HashSet<>(Arrays.asList(new JavaType[] { new JavaType(Object.class, 0) })));

      // Possible conversions between Java and internal types
      _java2Internal.put(Boolean.TYPE, Type.Boolean);
      _java2Internal.put(Void.TYPE, Type.Void);
      _java2Internal.put(Character.TYPE, Type.Real);
      _java2Internal.put(Byte.TYPE, Type.Real);
      _java2Internal.put(Short.TYPE, Type.Real);
      _java2Internal.put(Integer.TYPE, Type.Real);
      _java2Internal.put(Long.TYPE, Type.Real);
      _java2Internal.put(Float.TYPE, Type.Real);
      _java2Internal.put(Double.TYPE, Type.Real);

      _java2Internal.put(String.class, Type.String);

      _java2Internal.put(Object.class, Type.Reference);

      // Conversions from org.w3c.dom.Node/NodeList to internal NodeSet
      _java2Internal.put(nodeListClass, Type.NodeSet);
      _java2Internal.put(nodeClass, Type.NodeSet);

      // Initialize the extension namespace table
      _extensionNamespaceTable.put(EXT_XALAN, "de.lyca.xalan.lib.Extensions");
      _extensionNamespaceTable.put(EXSLT_COMMON, "de.lyca.xalan.lib.ExsltCommon");
      _extensionNamespaceTable.put(EXSLT_MATH, "de.lyca.xalan.lib.ExsltMath");
      _extensionNamespaceTable.put(EXSLT_SETS, "de.lyca.xalan.lib.ExsltSets");
      _extensionNamespaceTable.put(EXSLT_DATETIME, "de.lyca.xalan.lib.ExsltDatetime");
      _extensionNamespaceTable.put(EXSLT_STRINGS, "de.lyca.xalan.lib.ExsltStrings");

      // Initialize the extension function table
      _extensionFunctionTable.put(EXSLT_COMMON + ":nodeSet", "nodeset");
      _extensionFunctionTable.put(EXSLT_COMMON + ":objectType", "objectType");
      _extensionFunctionTable.put(EXT_XALAN + ":nodeset", "nodeset");
    } catch (final ClassNotFoundException e) {
      System.err.println(e);
    }
  }

  private static JavaType maps(Type from, Class<?> to) {
    if (from == null)
      return null;
    final Collection<JavaType> collection = _internal2Java.get(from);
    if (collection != null) {
      for (final JavaType javaType : collection) {
        if (javaType.equals(to))
          return javaType;
      }
    }
    return null;
  }

  public FunctionCall(QName fname, List<Expression> arguments) {
    _fname = fname;
    _arguments = arguments;
    _type = null;
  }

  public FunctionCall(QName fname) {
    this(fname, EMPTY_ARG_LIST);
  }

  public String getName() {
    return _fname.toString();
  }

  @Override
  public void setParser(Parser parser) {
    super.setParser(parser);
    if (_arguments != null) {
      for (Expression exp : _arguments) {
        exp.setParser(parser);
        exp.setParent(this);
      }
    }
  }

  public String getClassNameFromUri(String uri) {
    final String className = _extensionNamespaceTable.get(uri);

    if (className != null)
      return className;
    else {
      if (uri.startsWith(JAVA_EXT_XSLTC)) {
        final int length = JAVA_EXT_XSLTC.length() + 1;
        return uri.length() > length ? uri.substring(length) : "";
      } else if (uri.startsWith(JAVA_EXT_XALAN)) {
        final int length = JAVA_EXT_XALAN.length() + 1;
        return uri.length() > length ? uri.substring(length) : "";
      } else if (uri.startsWith(JAVA_EXT_XALAN_OLD)) {
        final int length = JAVA_EXT_XALAN_OLD.length() + 1;
        return uri.length() > length ? uri.substring(length) : "";
      } else {
        final int index = uri.lastIndexOf('/');
        return index > 0 ? uri.substring(index + 1) : uri;
      }
    }
  }

  /**
   * Type check a function call. Since different type conversions apply, type
   * checking is different for standard and external (Java) functions.
   */
  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {
    if (_type != null)
      return _type;

    final String namespace = _fname.getNamespace();
    String local = _fname.getLocalPart();

    if (isExtension()) {
      _fname = new QName(null, null, local);
      return typeCheckStandard(stable);
    } else if (isStandard())
      return typeCheckStandard(stable);
    else {
      try {
        _className = getClassNameFromUri(namespace);

        final int pos = local.lastIndexOf('.');
        if (pos > 0) {
          _isStatic = true;
          if (_className != null && _className.length() > 0) {
            _namespace_format = NAMESPACE_FORMAT_PACKAGE;
            _className = _className + "." + local.substring(0, pos);
          } else {
            _namespace_format = NAMESPACE_FORMAT_JAVA;
            _className = local.substring(0, pos);
          }

          _fname = new QName(namespace, null, local.substring(pos + 1));
        } else {
          if (_className != null && _className.length() > 0) {
            try {
              _clazz = ObjectFactory.findProviderClass(_className, ObjectFactory.findClassLoader(), true);
              _namespace_format = NAMESPACE_FORMAT_CLASS;
            } catch (final ClassNotFoundException e) {
              _namespace_format = NAMESPACE_FORMAT_PACKAGE;
            }
          } else {
            _namespace_format = NAMESPACE_FORMAT_JAVA;
          }

          if (local.indexOf('-') > 0) {
            local = replaceDash(local);
          }

          final String extFunction = _extensionFunctionTable.get(namespace + ":" + local);
          if (extFunction != null) {
            _fname = new QName(null, null, extFunction);
            return typeCheckStandard(stable);
          } else {
            _fname = new QName(namespace, null, local);
          }
        }

        return typeCheckExternal(stable);
      } catch (final TypeCheckError e) {
        ErrorMsg errorMsg = e.getErrorMsg();
        if (errorMsg == null) {
          final String name = _fname.getLocalPart();
          errorMsg = new ErrorMsg(this, Messages.get().methodNotFoundErr(name));
        }
        getParser().reportError(ERROR, errorMsg);
        return _type = Type.Void;
      }
    }
  }

  /**
   * Type check a call to a standard function. Insert CastExprs when needed. If
   * as a result of the insertion of a CastExpr a type check error is thrown,
   * then catch it and re-throw it with a new "this".
   */
  public Type typeCheckStandard(SymbolTable stable) throws TypeCheckError {
    _fname.clearNamespace(); // HACK!!!

    final int n = _arguments.size();
    final List<Type> argsType = typeCheckArgs(stable);
    final MethodType args = new MethodType(Type.Void, argsType);
    final MethodType ptype = lookupPrimop(stable, _fname.getLocalPart(), args);

    if (ptype != null) {
      for (int i = 0; i < n; i++) {
        final Type argType = ptype.argsType().get(i);
        final Expression exp = _arguments.get(i);
        if (!argType.identicalTo(exp.getType())) {
          try {
            _arguments.set(i, new CastExpr(exp, argType));
          } catch (final TypeCheckError e) {
            throw new TypeCheckError(this); // invalid conversion
          }
        }
      }
      _chosenMethodType = ptype;
      return _type = ptype.resultType();
    }
    throw new TypeCheckError(this);
  }

  public Type typeCheckConstructor(SymbolTable stable) throws TypeCheckError {
    final List<Constructor<?>> constructors = findConstructors();
    if (constructors == null)
      // Constructor not found in this class
      throw new TypeCheckError(new ErrorMsg(Messages.get().constructorNotFound(_className)));

    final int nConstructors = constructors.size();
    final int nArgs = _arguments.size();
    final List<Type> argsType = typeCheckArgs(stable);

    // Try all constructors
    int bestConstrDistance = Integer.MAX_VALUE;
    _type = null; // reset
    for (int j, i = 0; i < nConstructors; i++) {
      // Check if all parameters to this constructor can be converted
      final Constructor<?> constructor = constructors.get(i);
      final Class<?>[] paramTypes = constructor.getParameterTypes();

      Class<?> extType = null;
      int currConstrDistance = 0;
      for (j = 0; j < nArgs; j++) {
        // Convert from internal (translet) type to external (Java) type
        extType = paramTypes[j];
        final Type intType = argsType.get(j);
        final JavaType match = maps(intType, extType);
        if (match != null) {
          currConstrDistance += match.distance;
        } else if (intType instanceof ObjectType) {
          final ObjectType objectType = (ObjectType) intType;
          if (objectType.getJavaClass() == extType) {
            continue;
          } else if (extType.isAssignableFrom(objectType.getJavaClass())) {
            currConstrDistance += 1;
          } else {
            currConstrDistance = Integer.MAX_VALUE;
            break;
          }
        } else {
          // no mapping available
          currConstrDistance = Integer.MAX_VALUE;
          break;
        }
      }

      if (j == nArgs && currConstrDistance < bestConstrDistance) {
        _chosenConstructor = constructor;
        _isExtConstructor = true;
        bestConstrDistance = currConstrDistance;

        _type = _clazz != null ? Type.newObjectType(_clazz) : Type.newObjectType(_className);
      }
    }

    if (_type != null)
      return _type;

    throw new TypeCheckError(new ErrorMsg(this, Messages.get().argumentConversionErr(getMethodSignature(argsType))));
  }

  /**
   * Type check a call to an external (Java) method. The method must be static
   * an public, and a legal type conversion must exist for all its arguments and
   * its return type. Every method of name <code>_fname</code> is inspected as a
   * possible candidate.
   */
  public Type typeCheckExternal(SymbolTable stable) throws TypeCheckError {
    int nArgs = _arguments.size();
    final String name = _fname.getLocalPart();

    // check if function is a contructor 'new'
    if (_fname.getLocalPart().equals("new"))
      return typeCheckConstructor(stable);
    else {
      boolean hasThisArgument = false;

      if (nArgs == 0) {
        _isStatic = true;
      }

      if (!_isStatic) {
        if (_namespace_format == NAMESPACE_FORMAT_JAVA || _namespace_format == NAMESPACE_FORMAT_PACKAGE) {
          hasThisArgument = true;
        }

        final Expression firstArg = _arguments.get(0);
        final Type firstArgType = firstArg.typeCheck(stable);

        if (_namespace_format == NAMESPACE_FORMAT_CLASS && firstArgType instanceof ObjectType && _clazz != null
            && _clazz.isAssignableFrom(((ObjectType) firstArgType).getJavaClass())) {
          hasThisArgument = true;
        }

        if (hasThisArgument) {
          _thisArgument = _arguments.get(0);
          _arguments.remove(0);
          nArgs--;
          if (firstArgType instanceof ObjectType) {
            _className = ((ObjectType) firstArgType).getJavaClassName();
          } else
            throw new TypeCheckError(new ErrorMsg(Messages.get().noJavaFunctThisRef(name)));
        }
      } else if (_className.length() == 0) {
        /*
         * Warn user if external function could not be resolved. Warning will
         * _NOT_ be issued is the call is properly wrapped in an <xsl:if> or
         * <xsl:when> element. For details see If.parserContents() and
         * When.parserContents()
         */
        final Parser parser = getParser();
        if (parser != null) {
          reportWarning(this, parser, Messages.get().functionResolveErr(_fname));
        }
        unresolvedExternal = true;
        return _type = Type.Int; // use "Int" as "unknown"
      }
    }

    final List<Method> methods = findMethods();

    if (methods == null)
      // Method not found in this class
      throw new TypeCheckError(new ErrorMsg(this, Messages.get().methodNotFoundErr(_className + "." + name)));

    Class<?> extType = null;
    final int nMethods = methods.size();
    final List<Type> argsType = typeCheckArgs(stable);

    // Try all methods to identify the best fit
    int bestMethodDistance = Integer.MAX_VALUE;
    _type = null; // reset internal type
    for (int j, i = 0; i < nMethods; i++) {
      // Check if all paramteters to this method can be converted
      final Method method = methods.get(i);
      final Class<?>[] paramTypes = method.getParameterTypes();

      int currMethodDistance = 0;
      for (j = 0; j < nArgs; j++) {
        // Convert from internal (translet) type to external (Java) type
        extType = paramTypes[j];
        final Type intType = argsType.get(j);
        final JavaType match = maps(intType, extType);
        if (match != null) {
          currMethodDistance += match.distance;
        } else {
          // no mapping available
          //
          // Allow a Reference type to match any external (Java) type at
          // the moment. The real type checking is performed at runtime.
          if (intType instanceof ReferenceType) {
            currMethodDistance += 1;
          } else if (intType instanceof ObjectType) {
            final ObjectType object = (ObjectType) intType;
            if (extType.getName().equals(object.getJavaClassName())) {
              currMethodDistance += 0;
            } else if (extType.isAssignableFrom(object.getJavaClass())) {
              currMethodDistance += 1;
            } else {
              currMethodDistance = Integer.MAX_VALUE;
              break;
            }
          } else {
            currMethodDistance = Integer.MAX_VALUE;
            break;
          }
        }
      }

      if (j == nArgs) {
        // Check if the return type can be converted
        extType = method.getReturnType();

        _type = _java2Internal.get(extType);
        if (_type == null) {
          _type = Type.newObjectType(extType);
        }

        // Use this method if all parameters & return type match
        if (_type != null && currMethodDistance < bestMethodDistance) {
          _chosenMethod = method;
          bestMethodDistance = currMethodDistance;
        }
      }
    }

    // It is an error if the chosen method is an instance menthod but we don't
    // have a this argument.
    if (_chosenMethod != null && _thisArgument == null && !Modifier.isStatic(_chosenMethod.getModifiers()))
      throw new TypeCheckError(new ErrorMsg(Messages.get().noJavaFunctThisRef(getMethodSignature(argsType))));

    if (_type != null) {
      if (_type == Type.NodeSet) {
        getXSLTC().setMultiDocument(true);
      }
      return _type;
    }

    throw new TypeCheckError(new ErrorMsg(this, Messages.get().argumentConversionErr(getMethodSignature(argsType))));
  }

  /**
   * Type check the actual arguments of this function call.
   */
  public List<Type> typeCheckArgs(SymbolTable stable) throws TypeCheckError {
    final List<Type> result = new ArrayList<>();
    for (final Expression exp : _arguments) {
      result.add(exp.typeCheck(stable));
    }
    return result;
  }

  protected final Expression argument(int i) {
    return _arguments.get(i);
  }

  protected final Expression argument() {
    return argument(0);
  }

  protected final int argumentCount() {
    return _arguments.size();
  }

  protected final void setArgument(int i, Expression exp) {
    _arguments.set(i, exp);
  }

  protected final List<Expression> getArguments() {
    return _arguments;
  }

  @Override
  public JExpression toJExpression(CompilerContext ctx) {
    final int n = argumentCount();
    final boolean isSecureProcessing = getParser().getXSLTC().isSecureProcessing();

    // Translate calls to methods in the BasisLibrary
    if (isStandard() || isExtension()) {
      List<JExpression> compiledExps = new ArrayList<>();
      for (int i = 0, size = _arguments.size(); i < size; i++) {
        Expression exp = _arguments.get(i);
        compiledExps.add(exp.startIterator(ctx, exp.toJExpression(ctx)));
      }

      // append "F" to the function's name
      final String name = _fname.toString().replace('-', '_') + "F";

      // Special precautions for some method calls
      if (name.equals("sumF")) {
        compiledExps.add(ctx.currentDom());
      } else if (name.equals("normalize_spaceF")) {
        if (_chosenMethodType.argsCount() == 0 && _chosenMethodType.resultType().identicalTo(Type.String)) {
          compiledExps.add(ctx.currentNode());
          compiledExps.add(ctx.currentDom());
        }
      }

      // Invoke the method in the basis library
      JInvocation result = ctx.ref(BasisLibrary.class).staticInvoke(name);
      for (JExpression exp : compiledExps) {
        result = result.arg(exp);
      }
      return result;
    }
    // Add call to BasisLibrary.unresolved_externalF() to generate
    // run-time error message for unsupported external functions
    else if (unresolvedExternal) {
      return ctx.ref(BasisLibrary.class).staticInvoke("unresolved_externalF").arg(_fname.toString());
    } else if (_isExtConstructor) {
      if (isSecureProcessing) {
        translateUnallowedExtension(ctx);
      }

      Class<?> declaringClass = _chosenConstructor.getDeclaringClass();
      JInvocation invocation = _new(ctx.ref(declaringClass));
      final Class<?>[] paramTypes = _chosenConstructor.getParameterTypes();

      for (int i = 0; i < n; i++) {
        final Expression exp = argument(i);
        // Convert the argument to its Java type
        JExpression expr = exp.getType().compileTo(ctx, exp.startIterator(ctx, exp.toJExpression(ctx)), paramTypes[i]);
        invocation.arg(expr);
      }
      // Convert the return type back to our internal type
      return _type.getClassName().equals(declaringClass.getName()) ? invocation
          : _type.compileFrom(ctx, invocation, declaringClass);
    }
    // Invoke function calls that are handled in separate classes
    else {
      if (isSecureProcessing) {
        translateUnallowedExtension(ctx);
      }

      final Class<?>[] paramTypes = _chosenMethod.getParameterTypes();

      // Push "this" if it is an instance method
      JExpression thisArgument = null;
      if (_thisArgument != null) {
        thisArgument = _thisArgument.toJExpression(ctx);
      }

      JInvocation invocation = thisArgument == null
          ? ctx.ref(_chosenMethod.getDeclaringClass()).staticInvoke(_chosenMethod.getName())
          : thisArgument.invoke(_chosenMethod.getName());

      JExpression[] expressions = new JExpression[n];
      for (int i = 0; i < n; i++) {
        final Expression exp = argument(i);
        // Convert the argument to its Java type
        expressions[i] = exp.getType().compileTo(ctx, exp.startIterator(ctx, exp.toJExpression(ctx)), paramTypes[i]);
        invocation.arg(expressions[i]);
      }

      // Convert the return type back to our internal type
      return _type.getClassName().equals(_chosenMethod.getReturnType().getName()) ? invocation
          : _type.compileFrom(ctx, invocation, _chosenMethod.getReturnType());
    }
  }

  @Override
  public String toString() {
    return "funcall(" + _fname + ", " + _arguments + ')';
  }

  public boolean isStandard() {
    final String namespace = _fname.getNamespace();
    return namespace == null || namespace.isEmpty();
  }

  public boolean isExtension() {
    final String namespace = _fname.getNamespace();
    return namespace != null && namespace.equals(EXT_XSLTC);
  }

  /**
   * Returns a list with all methods named <code>_fname</code> after stripping
   * its namespace or <code>null</code> if no such methods exist.
   */
  private List<Method> findMethods() {

    List<Method> result = null;

    if (_className != null && _className.length() > 0) {
      final int nArgs = _arguments.size();
      try {
        if (_clazz == null) {
          _clazz = ObjectFactory.findProviderClass(_className, ObjectFactory.findClassLoader(), true);

          if (_clazz == null) {
            final ErrorMsg msg = new ErrorMsg(this, Messages.get().classNotFoundErr(_className));
            getParser().reportError(Constants.ERROR, msg);
          }
        }

        final String methodName = _fname.getLocalPart();
        final Method[] methods = _clazz.getMethods();

        for (int i = 0; i < methods.length; i++) {
          final int mods = methods[i].getModifiers();
          // Is it public and same number of args ?
          if (Modifier.isPublic(mods) && methods[i].getName().equals(methodName)
              && methods[i].getParameterTypes().length == nArgs) {
            if (result == null) {
              result = new ArrayList<>();
            }
            result.add(methods[i]);
          }
        }
      } catch (final ClassNotFoundException e) {
        final ErrorMsg msg = new ErrorMsg(this, Messages.get().classNotFoundErr(_className));
        getParser().reportError(Constants.ERROR, msg);
      }
    }
    return result;
  }

  /**
   * Returns a list with all constructors named <code>_fname</code> after
   * stripping its namespace or <code>null</code> if no such methods exist.
   */
  private List<Constructor<?>> findConstructors() {
    List<Constructor<?>> result = null;

    final int nArgs = _arguments.size();
    try {
      if (_clazz == null) {
        _clazz = ObjectFactory.findProviderClass(_className, ObjectFactory.findClassLoader(), true);

        if (_clazz == null) {
          final ErrorMsg msg = new ErrorMsg(this, Messages.get().classNotFoundErr(_className));
          getParser().reportError(Constants.ERROR, msg);
        }
      }

      final Constructor<?>[] constructors = _clazz.getConstructors();

      for (int i = 0; i < constructors.length; i++) {
        final int mods = constructors[i].getModifiers();
        // Is it public, static and same number of args ?
        if (Modifier.isPublic(mods) && constructors[i].getParameterTypes().length == nArgs) {
          if (result == null) {
            result = new ArrayList<>();
          }
          result.add(constructors[i]);
        }
      }
    } catch (final ClassNotFoundException e) {
      final ErrorMsg msg = new ErrorMsg(this, Messages.get().classNotFoundErr(_className));
      getParser().reportError(Constants.ERROR, msg);
    }

    return result;
  }

  /**
   * Compute the JVM signature for the class.
   */
  static final String getSignature(Class<?> clazz) {
    if (clazz.isArray()) {
      final StringBuilder sb = new StringBuilder();
      Class<?> cl = clazz;
      while (cl.isArray()) {
        sb.append("[");
        cl = cl.getComponentType();
      }
      sb.append(getSignature(cl));
      return sb.toString();
    } else if (clazz.isPrimitive()) {
      if (clazz == Integer.TYPE)
        return "I";
      else if (clazz == Byte.TYPE)
        return "B";
      else if (clazz == Long.TYPE)
        return "J";
      else if (clazz == Float.TYPE)
        return "F";
      else if (clazz == Double.TYPE)
        return "D";
      else if (clazz == Short.TYPE)
        return "S";
      else if (clazz == Character.TYPE)
        return "C";
      else if (clazz == Boolean.TYPE)
        return "Z";
      else if (clazz == Void.TYPE)
        return "V";
      else {
        final String name = clazz.toString();
        final ErrorMsg err = new ErrorMsg(Messages.get().unknownSigTypeErr(name), -1);
        throw new Error(err.toString());
      }
    } else
      return "L" + clazz.getName().replace('.', '/') + ';';
  }

  /**
   * Compute the JVM method descriptor for the method.
   */
  static final String getSignature(Method meth) {
    final StringBuilder sb = new StringBuilder();
    sb.append('(');
    final Class<?>[] params = meth.getParameterTypes(); // avoid clone
    for (int j = 0; j < params.length; j++) {
      sb.append(getSignature(params[j]));
    }
    return sb.append(')').append(getSignature(meth.getReturnType())).toString();
  }

  /**
   * Compute the JVM constructor descriptor for the constructor.
   */
  static final String getSignature(Constructor<?> cons) {
    final StringBuilder sb = new StringBuilder();
    sb.append('(');
    final Class<?>[] params = cons.getParameterTypes(); // avoid clone
    for (int j = 0; j < params.length; j++) {
      sb.append(getSignature(params[j]));
    }
    return sb.append(")V").toString();
  }

  /**
   * Return the signature of the current method
   */
  private String getMethodSignature(List<Type> argsType) {
    final StringBuilder buf = new StringBuilder(_className);
    buf.append('.').append(_fname.getLocalPart()).append('(');

    final int nArgs = argsType.size();
    for (int i = 0; i < nArgs; i++) {
      final Type intType = argsType.get(i);
      buf.append(intType.toString());
      if (i < nArgs - 1) {
        buf.append(", ");
      }
    }

    buf.append(')');
    return buf.toString();
  }

  /**
   * To support EXSLT extensions, convert names with dash to allowable Java
   * names: e.g., convert abc-xyz to abcXyz. Note: dashes only appear in middle
   * of an EXSLT function or element name.
   */
  protected static String replaceDash(String name) {
    final char dash = '-';
    final StringBuilder buff = new StringBuilder("");
    for (int i = 0; i < name.length(); i++) {
      if (i > 0 && name.charAt(i - 1) == dash) {
        buff.append(Character.toUpperCase(name.charAt(i)));
      } else if (name.charAt(i) != dash) {
        buff.append(name.charAt(i));
      }
    }
    return buff.toString();
  }

  /**
   * Add code to call the BasisLibrary.unallowed_extensionF(String) method.
   */
  private void translateUnallowedExtension(CompilerContext ctx) {
    ctx.currentBlock().staticInvoke(ctx.ref(BasisLibrary.class), "unallowed_extension_functionF")
        .arg(_fname.toString());
  }

}
