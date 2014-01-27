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
package de.lyca.xpath.axes;

import de.lyca.xpath.Expression;
import de.lyca.xpath.ExpressionOwner;
import de.lyca.xpath.XPathVisitor;
import de.lyca.xpath.functions.FuncLast;
import de.lyca.xpath.functions.FuncPosition;
import de.lyca.xpath.functions.Function;
import de.lyca.xpath.objects.XNumber;
import de.lyca.xpath.operations.Div;
import de.lyca.xpath.operations.Minus;
import de.lyca.xpath.operations.Mod;
import de.lyca.xpath.operations.Mult;
import de.lyca.xpath.operations.Plus;
import de.lyca.xpath.operations.Quo;
import de.lyca.xpath.operations.Variable;

public class HasPositionalPredChecker extends XPathVisitor {
  private boolean m_hasPositionalPred = false;
  private int m_predDepth = 0;

  /**
   * Process the LocPathIterator to see if it contains variables or functions
   * that may make it context dependent.
   * 
   * @param path
   *          LocPathIterator that is assumed to be absolute, but needs
   *          checking.
   * @return true if the path is confirmed to be absolute, false if it may
   *         contain context dependencies.
   */
  public static boolean check(LocPathIterator path) {
    final HasPositionalPredChecker hppc = new HasPositionalPredChecker();
    path.callVisitors(null, hppc);
    return hppc.m_hasPositionalPred;
  }

  /**
   * Visit a function.
   * 
   * @param owner
   *          The owner of the expression, to which the expression can be reset
   *          if rewriting takes place.
   * @param func
   *          The function reference object.
   * @return true if the sub expressions should be traversed.
   */
  @Override
  public boolean visitFunction(ExpressionOwner owner, Function func) {
    if (func instanceof FuncPosition || func instanceof FuncLast) {
      m_hasPositionalPred = true;
    }
    return true;
  }

  // /**
  // * Visit a variable reference.
  // * @param owner The owner of the expression, to which the expression can
  // * be reset if rewriting takes place.
  // * @param var The variable reference object.
  // * @return true if the sub expressions should be traversed.
  // */
  // public boolean visitVariableRef(ExpressionOwner owner, Variable var)
  // {
  // m_hasPositionalPred = true;
  // return true;
  // }

  /**
   * Visit a predicate within a location path. Note that there isn't a proper
   * unique component for predicates, and that the expression will be called
   * also for whatever type Expression is.
   * 
   * @param owner
   *          The owner of the expression, to which the expression can be reset
   *          if rewriting takes place.
   * @param pred
   *          The predicate object.
   * @return true if the sub expressions should be traversed.
   */
  @Override
  public boolean visitPredicate(ExpressionOwner owner, Expression pred) {
    m_predDepth++;

    if (m_predDepth == 1) {
      if (pred instanceof Variable || pred instanceof XNumber || pred instanceof Div || pred instanceof Plus
              || pred instanceof Minus || pred instanceof Mod || pred instanceof Quo || pred instanceof Mult
              || pred instanceof de.lyca.xpath.operations.Number || pred instanceof Function) {
        m_hasPositionalPred = true;
      } else {
        pred.callVisitors(owner, this);
      }
    }

    m_predDepth--;

    // Don't go have the caller go any further down the subtree.
    return false;
  }

}
