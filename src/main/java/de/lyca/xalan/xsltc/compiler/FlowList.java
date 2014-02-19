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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.bcel.generic.BranchHandle;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 */
public final class FlowList {
  private List<InstructionHandle> _elements;

  public FlowList() {
    _elements = null;
  }

  public FlowList(InstructionHandle bh) {
    _elements = new ArrayList<>();
    _elements.add(bh);
  }

  public FlowList(FlowList list) {
    _elements = list._elements;
  }

  public FlowList add(InstructionHandle bh) {
    if (_elements == null) {
      _elements = new ArrayList<>();
    }
    _elements.add(bh);
    return this;
  }

  public FlowList append(FlowList right) {
    if (_elements == null) {
      _elements = right._elements;
    } else {
      final List<InstructionHandle> temp = right._elements;
      if (temp != null) {
        _elements.addAll(temp);
      }
    }
    return this;
  }

  /**
   * Back patch a flow list. All instruction handles must be branch handles.
   */
  public void backPatch(InstructionHandle target) {
    if (_elements != null) {
      for (InstructionHandle ih : _elements) {
        final BranchHandle bh = (BranchHandle) ih;
        bh.setTarget(target);
      }
      _elements.clear(); // avoid backpatching more than once
    }
  }

  /**
   * Redirect the handles from oldList to newList. "This" flow list is assumed
   * to be relative to oldList.
   */
  public FlowList copyAndRedirect(InstructionList oldList, InstructionList newList) {
    final FlowList result = new FlowList();
    if (_elements == null)
      return result;

    @SuppressWarnings("unchecked")
    final Iterator<InstructionHandle> oldIter = oldList.iterator();
    @SuppressWarnings("unchecked")
    final Iterator<InstructionHandle> newIter = newList.iterator();

    while (oldIter.hasNext()) {
      final InstructionHandle oldIh = oldIter.next();
      final InstructionHandle newIh = newIter.next();

      for (InstructionHandle ih : _elements) {
        if (ih == oldIh) {
          result.add(newIh);
        }
      }
    }
    return result;
  }
}
