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
package de.lyca.xpath.axes;

import java.util.List;

import javax.xml.transform.TransformerException;

import de.lyca.xml.dtm.Axis;
import de.lyca.xml.utils.PrefixResolver;
import de.lyca.xml.utils.QName;
import de.lyca.xpath.compiler.Compiler;

/**
 * This class iterates over set of nodes that needs to be sorted.
 */
public class WalkingIteratorSorted extends WalkingIterator {
  static final long serialVersionUID = -4512512007542368213L;

  // /** True if the nodes will be found in document order */
  // protected boolean m_inNaturalOrder = false;

  /**
   * True if the nodes will be found in document order, and this can be
   * determined statically.
   */
  protected boolean m_inNaturalOrderStatic = false;

  /**
   * Create a WalkingIteratorSorted object.
   * 
   * @param nscontext
   *          The namespace context for this iterator, should be OK if null.
   */
  public WalkingIteratorSorted(PrefixResolver nscontext) {
    super(nscontext);
  }

  /**
   * Create a WalkingIterator iterator, including creation of step walkers from
   * the opcode list, and call back into the Compiler to create predicate
   * expressions.
   * 
   * @param compiler
   *          The Compiler which is creating this expression.
   * @param opPos
   *          The position of this iterator in the opcode list from the
   *          compiler.
   * @param shouldLoadWalkers
   *          True if walkers should be loaded, or false if this is a derived
   *          iterator and it doesn't wish to load child walkers.
   * 
   * @throws TransformerException TODO
   */
  WalkingIteratorSorted(Compiler compiler, int opPos, int analysis, boolean shouldLoadWalkers)
          throws TransformerException {
    super(compiler, opPos, analysis, shouldLoadWalkers);
  }

  /**
   * Returns true if all the nodes in the iteration well be returned in document
   * order.
   * 
   * @return true as a default.
   */
  @Override
  public boolean isDocOrdered() {
    return m_inNaturalOrderStatic;
  }

  /**
   * Tell if the nodeset can be walked in doc order, via static analysis.
   * 
   * 
   * @return true if the nodeset can be walked in doc order, without sorting.
   */
  boolean canBeWalkedInNaturalDocOrderStatic() {

    if (null != m_firstWalker) {
      AxesWalker walker = m_firstWalker;
      final int prevAxis = -1;
      final boolean prevIsSimpleDownAxis = true;

      for (int i = 0; null != walker; i++) {
        final Axis axis = walker.getAxis();

        if (walker.isDocOrdered()) {
          final boolean isSimpleDownAxis = axis == Axis.CHILD || axis == Axis.SELF || axis == Axis.ROOT;
          // Catching the filtered list here is only OK because
          // FilterExprWalker#isDocOrdered() did the right thing.
          if (isSimpleDownAxis || axis == null) {
            walker = walker.getNextWalker();
          } else {
            final boolean isLastWalker = null == walker.getNextWalker();
            if (isLastWalker) {
              if (walker.isDocOrdered()
                      && (axis == Axis.DESCENDANT || axis == Axis.DESCENDANTORSELF || axis == Axis.DESCENDANTSFROMROOT || axis == Axis.DESCENDANTSORSELFFROMROOT)
                      || axis == Axis.ATTRIBUTE)
                return true;
            }
            return false;
          }
        } else
          return false;
      }
      return true;
    }
    return false;
  }

  // /**
  // * NEEDSDOC Method canBeWalkedInNaturalDocOrder
  // *
  // *
  // * NEEDSDOC (canBeWalkedInNaturalDocOrder) @return
  // */
  // boolean canBeWalkedInNaturalDocOrder()
  // {
  //
  // if (null != m_firstWalker)
  // {
  // AxesWalker walker = m_firstWalker;
  // int prevAxis = -1;
  // boolean prevIsSimpleDownAxis = true;
  //
  // for(int i = 0; null != walker; i++)
  // {
  // int axis = walker.getAxis();
  //
  // if(walker.isDocOrdered())
  // {
  // boolean isSimpleDownAxis = ((axis == Axis.CHILD)
  // || (axis == Axis.SELF)
  // || (axis == Axis.ROOT));
  // // Catching the filtered list here is only OK because
  // // FilterExprWalker#isDocOrdered() did the right thing.
  // if(isSimpleDownAxis || (axis == -1))
  // walker = walker.getNextWalker();
  // else
  // {
  // boolean isLastWalker = (null == walker.getNextWalker());
  // if(isLastWalker)
  // {
  // if(walker.isDocOrdered() && (axis == Axis.DESCENDANT ||
  // axis == Axis.DESCENDANTORSELF || axis == Axis.DESCENDANTSFROMROOT
  // || axis == Axis.DESCENDANTSORSELFFROMROOT) || (axis == Axis.ATTRIBUTE))
  // return true;
  // }
  // return false;
  // }
  // }
  // else
  // return false;
  // }
  // return true;
  // }
  // return false;
  // }

  /**
   * This function is used to perform some extra analysis of the iterator.
   * 
   * @param vars
   *          List of QNames that correspond to variables. This list should be
   *          searched backwards for the first qualified name that corresponds
   *          to the variable reference qname. The position of the QName in the
   *          list from the start of the list will be its position in the stack
   *          frame (but variables above the globalsTop value will need to be
   *          offset to the current stack frame).
   */
  @Override
  public void fixupVariables(List<QName> vars, int globalsSize) {
    super.fixupVariables(vars, globalsSize);

    final int analysis = getAnalysisBits();
    if (WalkerFactory.isNaturalDocOrder(analysis)) {
      m_inNaturalOrderStatic = true;
    } else {
      m_inNaturalOrderStatic = false;
      // System.out.println("Setting natural doc order to false: "+
      // WalkerFactory.getAnalysisString(analysis));
    }

  }

}
