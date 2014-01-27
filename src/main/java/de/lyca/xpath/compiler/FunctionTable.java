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
package de.lyca.xpath.compiler;

import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.TransformerException;

import de.lyca.xpath.functions.Function;

/**
 * The function table for XPath.
 */
public class FunctionTable {

  /** The 'current()' id. */
  public static final int FUNC_CURRENT = 0;

  /** The 'last()' id. */
  public static final int FUNC_LAST = 1;

  /** The 'position()' id. */
  public static final int FUNC_POSITION = 2;

  /** The 'count()' id. */
  public static final int FUNC_COUNT = 3;

  /** The 'id()' id. */
  public static final int FUNC_ID = 4;

  /** The 'key()' id (XSLT). */
  public static final int FUNC_KEY = 5;

  /** The 'local-name()' id. */
  public static final int FUNC_LOCAL_PART = 7;

  /** The 'namespace-uri()' id. */
  public static final int FUNC_NAMESPACE = 8;

  /** The 'name()' id. */
  public static final int FUNC_QNAME = 9;

  /** The 'generate-id()' id. */
  public static final int FUNC_GENERATE_ID = 10;

  /** The 'not()' id. */
  public static final int FUNC_NOT = 11;

  /** The 'true()' id. */
  public static final int FUNC_TRUE = 12;

  /** The 'false()' id. */
  public static final int FUNC_FALSE = 13;

  /** The 'boolean()' id. */
  public static final int FUNC_BOOLEAN = 14;

  /** The 'number()' id. */
  public static final int FUNC_NUMBER = 15;

  /** The 'floor()' id. */
  public static final int FUNC_FLOOR = 16;

  /** The 'ceiling()' id. */
  public static final int FUNC_CEILING = 17;

  /** The 'round()' id. */
  public static final int FUNC_ROUND = 18;

  /** The 'sum()' id. */
  public static final int FUNC_SUM = 19;

  /** The 'string()' id. */
  public static final int FUNC_STRING = 20;

  /** The 'starts-with()' id. */
  public static final int FUNC_STARTS_WITH = 21;

  /** The 'contains()' id. */
  public static final int FUNC_CONTAINS = 22;

  /** The 'substring-before()' id. */
  public static final int FUNC_SUBSTRING_BEFORE = 23;

  /** The 'substring-after()' id. */
  public static final int FUNC_SUBSTRING_AFTER = 24;

  /** The 'normalize-space()' id. */
  public static final int FUNC_NORMALIZE_SPACE = 25;

  /** The 'translate()' id. */
  public static final int FUNC_TRANSLATE = 26;

  /** The 'concat()' id. */
  public static final int FUNC_CONCAT = 27;

  /** The 'substring()' id. */
  public static final int FUNC_SUBSTRING = 29;

  /** The 'string-length()' id. */
  public static final int FUNC_STRING_LENGTH = 30;

  /** The 'system-property()' id. */
  public static final int FUNC_SYSTEM_PROPERTY = 31;

  /** The 'lang()' id. */
  public static final int FUNC_LANG = 32;

  /** The 'function-available()' id (XSLT). */
  public static final int FUNC_EXT_FUNCTION_AVAILABLE = 33;

  /** The 'element-available()' id (XSLT). */
  public static final int FUNC_EXT_ELEM_AVAILABLE = 34;

  /** The 'unparsed-entity-uri()' id (XSLT). */
  public static final int FUNC_UNPARSED_ENTITY_URI = 36;

  // Proprietary

  /** The 'document-location()' id (Proprietary). */
  public static final int FUNC_DOCLOCATION = 35;

  /**
   * The function table.
   */
  private static Class[] m_functions;

  /** Table of function name to function ID associations. */
  private static Map<String, Integer> m_functionID = new HashMap<String, Integer>();

  /**
   * The function table contains customized functions
   */
  private final Class[] m_functions_customer = new Class[NUM_ALLOWABLE_ADDINS];

  /**
   * Table of function name to function ID associations for customized functions
   */
  private final Map<String, Integer> m_functionID_customer = new HashMap<String, Integer>();

  /**
   * Number of built in functions. Be sure to update this as built-in functions
   * are added.
   */
  private static final int NUM_BUILT_IN_FUNCS = 37;

  /**
   * Number of built-in functions that may be added.
   */
  private static final int NUM_ALLOWABLE_ADDINS = 30;

  /**
   * The index to the next free function index.
   */
  private int m_funcNextFreeIndex = NUM_BUILT_IN_FUNCS;

  static {
    m_functions = new Class[NUM_BUILT_IN_FUNCS];
    m_functions[FUNC_CURRENT] = de.lyca.xpath.functions.FuncCurrent.class;
    m_functions[FUNC_LAST] = de.lyca.xpath.functions.FuncLast.class;
    m_functions[FUNC_POSITION] = de.lyca.xpath.functions.FuncPosition.class;
    m_functions[FUNC_COUNT] = de.lyca.xpath.functions.FuncCount.class;
    m_functions[FUNC_ID] = de.lyca.xpath.functions.FuncId.class;
    // m_functions[FUNC_KEY] = de.lyca.xalan.templates.FuncKey.class;
    m_functions[FUNC_LOCAL_PART] = de.lyca.xpath.functions.FuncLocalPart.class;
    m_functions[FUNC_NAMESPACE] = de.lyca.xpath.functions.FuncNamespace.class;
    m_functions[FUNC_QNAME] = de.lyca.xpath.functions.FuncQname.class;
    m_functions[FUNC_GENERATE_ID] = de.lyca.xpath.functions.FuncGenerateId.class;
    m_functions[FUNC_NOT] = de.lyca.xpath.functions.FuncNot.class;
    m_functions[FUNC_TRUE] = de.lyca.xpath.functions.FuncTrue.class;
    m_functions[FUNC_FALSE] = de.lyca.xpath.functions.FuncFalse.class;
    m_functions[FUNC_BOOLEAN] = de.lyca.xpath.functions.FuncBoolean.class;
    m_functions[FUNC_LANG] = de.lyca.xpath.functions.FuncLang.class;
    m_functions[FUNC_NUMBER] = de.lyca.xpath.functions.FuncNumber.class;
    m_functions[FUNC_FLOOR] = de.lyca.xpath.functions.FuncFloor.class;
    m_functions[FUNC_CEILING] = de.lyca.xpath.functions.FuncCeiling.class;
    m_functions[FUNC_ROUND] = de.lyca.xpath.functions.FuncRound.class;
    m_functions[FUNC_SUM] = de.lyca.xpath.functions.FuncSum.class;
    m_functions[FUNC_STRING] = de.lyca.xpath.functions.FuncString.class;
    m_functions[FUNC_STARTS_WITH] = de.lyca.xpath.functions.FuncStartsWith.class;
    m_functions[FUNC_CONTAINS] = de.lyca.xpath.functions.FuncContains.class;
    m_functions[FUNC_SUBSTRING_BEFORE] = de.lyca.xpath.functions.FuncSubstringBefore.class;
    m_functions[FUNC_SUBSTRING_AFTER] = de.lyca.xpath.functions.FuncSubstringAfter.class;
    m_functions[FUNC_NORMALIZE_SPACE] = de.lyca.xpath.functions.FuncNormalizeSpace.class;
    m_functions[FUNC_TRANSLATE] = de.lyca.xpath.functions.FuncTranslate.class;
    m_functions[FUNC_CONCAT] = de.lyca.xpath.functions.FuncConcat.class;
    m_functions[FUNC_SYSTEM_PROPERTY] = de.lyca.xpath.functions.FuncSystemProperty.class;
    m_functions[FUNC_EXT_FUNCTION_AVAILABLE] = de.lyca.xpath.functions.FuncExtFunctionAvailable.class;
    m_functions[FUNC_EXT_ELEM_AVAILABLE] = de.lyca.xpath.functions.FuncExtElementAvailable.class;
    m_functions[FUNC_SUBSTRING] = de.lyca.xpath.functions.FuncSubstring.class;
    m_functions[FUNC_STRING_LENGTH] = de.lyca.xpath.functions.FuncStringLength.class;
    m_functions[FUNC_DOCLOCATION] = de.lyca.xpath.functions.FuncDoclocation.class;
    m_functions[FUNC_UNPARSED_ENTITY_URI] = de.lyca.xpath.functions.FuncUnparsedEntityURI.class;
  }

  static {
    m_functionID.put(Keywords.FUNC_CURRENT_STRING, FUNC_CURRENT);
    m_functionID.put(Keywords.FUNC_LAST_STRING, FUNC_LAST);
    m_functionID.put(Keywords.FUNC_POSITION_STRING, FUNC_POSITION);
    m_functionID.put(Keywords.FUNC_COUNT_STRING, FUNC_COUNT);
    m_functionID.put(Keywords.FUNC_ID_STRING, FUNC_ID);
    m_functionID.put(Keywords.FUNC_KEY_STRING, FUNC_KEY);
    m_functionID.put(Keywords.FUNC_LOCAL_PART_STRING, FUNC_LOCAL_PART);
    m_functionID.put(Keywords.FUNC_NAMESPACE_STRING, FUNC_NAMESPACE);
    m_functionID.put(Keywords.FUNC_NAME_STRING, FUNC_QNAME);
    m_functionID.put(Keywords.FUNC_GENERATE_ID_STRING, FUNC_GENERATE_ID);
    m_functionID.put(Keywords.FUNC_NOT_STRING, FUNC_NOT);
    m_functionID.put(Keywords.FUNC_TRUE_STRING, FUNC_TRUE);
    m_functionID.put(Keywords.FUNC_FALSE_STRING, FUNC_FALSE);
    m_functionID.put(Keywords.FUNC_BOOLEAN_STRING, FUNC_BOOLEAN);
    m_functionID.put(Keywords.FUNC_LANG_STRING, FUNC_LANG);
    m_functionID.put(Keywords.FUNC_NUMBER_STRING, FUNC_NUMBER);
    m_functionID.put(Keywords.FUNC_FLOOR_STRING, FUNC_FLOOR);
    m_functionID.put(Keywords.FUNC_CEILING_STRING, FUNC_CEILING);
    m_functionID.put(Keywords.FUNC_ROUND_STRING, FUNC_ROUND);
    m_functionID.put(Keywords.FUNC_SUM_STRING, FUNC_SUM);
    m_functionID.put(Keywords.FUNC_STRING_STRING, FUNC_STRING);
    m_functionID.put(Keywords.FUNC_STARTS_WITH_STRING, FUNC_STARTS_WITH);
    m_functionID.put(Keywords.FUNC_CONTAINS_STRING, FUNC_CONTAINS);
    m_functionID.put(Keywords.FUNC_SUBSTRING_BEFORE_STRING, FUNC_SUBSTRING_BEFORE);
    m_functionID.put(Keywords.FUNC_SUBSTRING_AFTER_STRING, FUNC_SUBSTRING_AFTER);
    m_functionID.put(Keywords.FUNC_NORMALIZE_SPACE_STRING, FUNC_NORMALIZE_SPACE);
    m_functionID.put(Keywords.FUNC_TRANSLATE_STRING, FUNC_TRANSLATE);
    m_functionID.put(Keywords.FUNC_CONCAT_STRING, FUNC_CONCAT);
    m_functionID.put(Keywords.FUNC_SYSTEM_PROPERTY_STRING, FUNC_SYSTEM_PROPERTY);
    m_functionID.put(Keywords.FUNC_EXT_FUNCTION_AVAILABLE_STRING, FUNC_EXT_FUNCTION_AVAILABLE);
    m_functionID.put(Keywords.FUNC_EXT_ELEM_AVAILABLE_STRING, FUNC_EXT_ELEM_AVAILABLE);
    m_functionID.put(Keywords.FUNC_SUBSTRING_STRING, FUNC_SUBSTRING);
    m_functionID.put(Keywords.FUNC_STRING_LENGTH_STRING, FUNC_STRING_LENGTH);
    m_functionID.put(Keywords.FUNC_UNPARSED_ENTITY_URI_STRING, FUNC_UNPARSED_ENTITY_URI);
    m_functionID.put(Keywords.FUNC_DOCLOCATION_STRING, FUNC_DOCLOCATION);
  }

  public FunctionTable() {
  }

  /**
   * Return the name of the a function in the static table. Needed to avoid
   * making the table publicly available.
   */
  String getFunctionName(int funcID) {
    if (funcID < NUM_BUILT_IN_FUNCS)
      return m_functions[funcID].getName();
    else
      return m_functions_customer[funcID - NUM_BUILT_IN_FUNCS].getName();
  }

  /**
   * Obtain a new Function object from a function ID.
   * 
   * @param which
   *          The function ID, which may correspond to one of the FUNC_XXX
   *          values found in {@link de.lyca.xpath.compiler.FunctionTable}, but
   *          may be a value installed by an external module.
   * 
   * @return a a new Function instance.
   * 
   * @throws javax.xml.transform.TransformerException
   *           if ClassNotFoundException, IllegalAccessException, or
   *           InstantiationException is thrown.
   */
  Function getFunction(int which) throws javax.xml.transform.TransformerException {
    try {
      if (which < NUM_BUILT_IN_FUNCS)
        return (Function) m_functions[which].newInstance();
      else
        return (Function) m_functions_customer[which - NUM_BUILT_IN_FUNCS].newInstance();
    } catch (final IllegalAccessException ex) {
      throw new TransformerException(ex.getMessage());
    } catch (final InstantiationException ex) {
      throw new TransformerException(ex.getMessage());
    }
  }

  /**
   * Obtain a function ID from a given function name
   * 
   * @param key
   *          the function name in a java.lang.String format.
   * @return a function ID, which may correspond to one of the FUNC_XXX values
   *         found in {@link de.lyca.xpath.compiler.FunctionTable}, but may be a
   *         value installed by an external module.
   */
  Integer getFunctionID(String key) {
    Integer id = m_functionID_customer.get(key);
    if (null == id) {
      id = m_functionID.get(key);
    }
    return id;
  }

  /**
   * Install a built-in function.
   * 
   * @param name
   *          The unqualified name of the function, must not be null
   * @param func
   *          A Implementation of an XPath Function object.
   * @return the position of the function in the internal index.
   */
  public int installFunction(String name, Class func) {

    int funcIndex;
    final Integer funcIndexObj = getFunctionID(name);

    if (null != funcIndexObj) {
      funcIndex = funcIndexObj.intValue();

      if (funcIndex < NUM_BUILT_IN_FUNCS) {
        funcIndex = m_funcNextFreeIndex++;
        m_functionID_customer.put(name, new Integer(funcIndex));
      }
      m_functions_customer[funcIndex - NUM_BUILT_IN_FUNCS] = func;
    } else {
      funcIndex = m_funcNextFreeIndex++;

      m_functions_customer[funcIndex - NUM_BUILT_IN_FUNCS] = func;

      m_functionID_customer.put(name, funcIndex);
    }
    return funcIndex;
  }

  /**
   * Tell if a built-in, non-namespaced function is available.
   * 
   * @param functionName
   *          The local name of the function.
   * 
   * @return True if the function can be executed.
   */
  public boolean functionAvailable(String functionName) {
    return m_functionID.containsKey(functionName) || m_functionID_customer.containsKey(functionName);
  }

}
