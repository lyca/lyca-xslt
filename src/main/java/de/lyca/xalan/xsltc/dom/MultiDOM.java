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

package de.lyca.xalan.xsltc.dom;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.lyca.xalan.xsltc.DOM;
import de.lyca.xalan.xsltc.StripFilter;
import de.lyca.xalan.xsltc.TransletException;
import de.lyca.xalan.xsltc.runtime.BasisLibrary;
import de.lyca.xalan.xsltc.runtime.Hashtable;
import de.lyca.xml.dtm.Axis;
import de.lyca.xml.dtm.DTM;
import de.lyca.xml.dtm.DTMAxisIterator;
import de.lyca.xml.dtm.DTMManager;
import de.lyca.xml.dtm.ref.DTMAxisIteratorBase;
import de.lyca.xml.dtm.ref.DTMDefaultBase;
import de.lyca.xml.serializer.SerializationHandler;
import de.lyca.xml.utils.SuballocatedIntVector;

/**
 * @author Jacek Ambroziak
 * @author Morten Jorgensen
 * @author Erwin Bolwidt <ejb@klomp.org>
 */
public final class MultiDOM implements DOM {

  private static final int NO_TYPE = DOM.FIRST_TYPE - 2;
  private static final int INITIAL_SIZE = 4;

  private DOM[] _adapters;
  private final DOMAdapter _main;
  private DTMManager _dtmManager;
  private int _free;
  private int _size;

  private final Hashtable _documents = new Hashtable();

  private final class AxisIterator extends DTMAxisIteratorBase {
    // constitutive data
    private final int _axis;
    private final int _type;
    // implementation mechanism
    private DTMAxisIterator _source;
    private int _dtmId = -1;

    public AxisIterator(final int axis, final int type) {
      _axis = axis;
      _type = type;
    }

    @Override
    public int next() {
      if (_source == null)
        return END;
      return _source.next();
    }

    @Override
    public void setRestartable(boolean flag) {
      if (_source != null) {
        _source.setRestartable(flag);
      }
    }

    @Override
    public DTMAxisIterator setStartNode(final int node) {
      if (node == DTM.NULL)
        return this;

      final int dom = node >>> DTMManager.IDENT_DTM_NODE_BITS;

      // Get a new source first time and when mask changes
      if (_source == null || _dtmId != dom) {
        if (_type == NO_TYPE) {
          _source = _adapters[dom].getAxisIterator(_axis);
        } else if (_axis == Axis.CHILD) {
          _source = _adapters[dom].getTypedChildren(_type);
        } else {
          _source = _adapters[dom].getTypedAxisIterator(_axis, _type);
        }
      }

      _dtmId = dom;
      _source.setStartNode(node);
      return this;
    }

    @Override
    public DTMAxisIterator reset() {
      if (_source != null) {
        _source.reset();
      }
      return this;
    }

    @Override
    public int getLast() {
      if (_source != null)
        return _source.getLast();
      else
        return END;
    }

    @Override
    public int getPosition() {
      if (_source != null)
        return _source.getPosition();
      else
        return END;
    }

    @Override
    public boolean isReverse() {
      return Axis.isReverse(_axis);
    }

    @Override
    public void setMark() {
      if (_source != null) {
        _source.setMark();
      }
    }

    @Override
    public void gotoMark() {
      if (_source != null) {
        _source.gotoMark();
      }
    }

    @Override
    public DTMAxisIterator cloneIterator() {
      final AxisIterator clone = new AxisIterator(_axis, _type);
      if (_source != null) {
        clone._source = _source.cloneIterator();
      }
      clone._dtmId = _dtmId;
      return clone;
    }
  } // end of AxisIterator

  /**************************************************************
   * This is a specialised iterator for predicates comparing node or attribute
   * values to variable or parameter values.
   */
  private final class NodeValueIterator extends DTMAxisIteratorBase {

    private DTMAxisIterator _source;
    private final String _value;
    private final boolean _op;
    private final boolean _isReverse;
    private int _returnType = RETURN_PARENT;

    public NodeValueIterator(DTMAxisIterator source, int returnType, String value, boolean op) {
      _source = source;
      _returnType = returnType;
      _value = value;
      _op = op;
      _isReverse = source.isReverse();
    }

    @Override
    public boolean isReverse() {
      return _isReverse;
    }

    @Override
    public DTMAxisIterator cloneIterator() {
      try {
        final NodeValueIterator clone = (NodeValueIterator) super.clone();
        clone._source = _source.cloneIterator();
        clone.setRestartable(false);
        return clone.reset();
      } catch (final CloneNotSupportedException e) {
        BasisLibrary.runTimeError(BasisLibrary.ITERATOR_CLONE_ERR, e.toString());
        return null;
      }
    }

    @Override
    public void setRestartable(boolean isRestartable) {
      _isRestartable = isRestartable;
      _source.setRestartable(isRestartable);
    }

    @Override
    public DTMAxisIterator reset() {
      _source.reset();
      return resetPosition();
    }

    @Override
    public int next() {

      int node;
      while ((node = _source.next()) != END) {
        final String val = getStringValueX(node);
        if (_value.equals(val) == _op) {
          if (_returnType == RETURN_CURRENT)
            return returnNode(node);
          else
            return returnNode(getParent(node));
        }
      }
      return END;
    }

    @Override
    public DTMAxisIterator setStartNode(int node) {
      if (_isRestartable) {
        _source.setStartNode(_startNode = node);
        return resetPosition();
      }
      return this;
    }

    @Override
    public void setMark() {
      _source.setMark();
    }

    @Override
    public void gotoMark() {
      _source.gotoMark();
    }
  }

  public MultiDOM(DOM main) {
    _size = INITIAL_SIZE;
    _free = 1;
    _adapters = new DOM[INITIAL_SIZE];
    final DOMAdapter adapter = (DOMAdapter) main;
    _adapters[0] = adapter;
    _main = adapter;
    final DOM dom = adapter.getDOMImpl();
    if (dom instanceof DTMDefaultBase) {
      _dtmManager = ((DTMDefaultBase) dom).getManager();
    }

    // %HZ% %REVISIT% Is this the right thing to do here? In the old
    // %HZ% %REVISIT% version, the main document did not get added through
    // %HZ% %REVISIT% a call to addDOMAdapter, which meant it couldn't be
    // %HZ% %REVISIT% found by a call to getDocumentMask. The problem is
    // %HZ% %REVISIT% TransformerHandler is typically constructed with a
    // %HZ% %REVISIT% system ID equal to the stylesheet's URI; with SAX
    // %HZ% %REVISIT% input, it ends up giving that URI to the document.
    // %HZ% %REVISIT% Then, any references to document('') are resolved
    // %HZ% %REVISIT% using the stylesheet's URI.
    // %HZ% %REVISIT% MultiDOM.getDocumentMask is called to verify that
    // %HZ% %REVISIT% a document associated with that URI has not been
    // %HZ% %REVISIT% encountered, and that method ends up returning the
    // %HZ% %REVISIT% mask of the main document, when what we really what
    // %HZ% %REVISIT% is to read the stylesheet itself!
    addDOMAdapter(adapter, false);
  }

  public int nextMask() {
    return _free;
  }

  @Override
  public void setupMapping(String[] names, String[] uris, int[] types, String[] namespaces) {
    // This method only has a function in DOM adapters
  }

  public int addDOMAdapter(DOMAdapter adapter) {
    return addDOMAdapter(adapter, true);
  }

  private int addDOMAdapter(DOMAdapter adapter, boolean indexByURI) {
    // Add the DOM adapter to the array of DOMs
    final DOM dom = adapter.getDOMImpl();

    int domNo = 1;
    int dtmSize = 1;
    SuballocatedIntVector dtmIds = null;
    if (dom instanceof DTMDefaultBase) {
      final DTMDefaultBase dtmdb = (DTMDefaultBase) dom;
      dtmIds = dtmdb.getDTMIDs();
      dtmSize = dtmIds.size();
      domNo = dtmIds.elementAt(dtmSize - 1) >>> DTMManager.IDENT_DTM_NODE_BITS;
    } else if (dom instanceof SimpleResultTreeImpl) {
      final SimpleResultTreeImpl simpleRTF = (SimpleResultTreeImpl) dom;
      domNo = simpleRTF.getDocument() >>> DTMManager.IDENT_DTM_NODE_BITS;
    }

    if (domNo >= _size) {
      final int oldSize = _size;
      do {
        _size *= 2;
      } while (_size <= domNo);

      final DOMAdapter[] newArray = new DOMAdapter[_size];
      System.arraycopy(_adapters, 0, newArray, 0, oldSize);
      _adapters = newArray;
    }

    _free = domNo + 1;

    if (dtmSize == 1) {
      _adapters[domNo] = adapter;
    } else if (dtmIds != null) {
      int domPos = 0;
      for (int i = dtmSize - 1; i >= 0; i--) {
        domPos = dtmIds.elementAt(i) >>> DTMManager.IDENT_DTM_NODE_BITS;
        _adapters[domPos] = adapter;
      }
      domNo = domPos;
    }

    // Store reference to document (URI) in hashtable
    if (indexByURI) {
      final String uri = adapter.getDocumentURI(0);
      _documents.put(uri, new Integer(domNo));
    }

    // If the dom is an AdaptiveResultTreeImpl, we need to create a
    // DOMAdapter around its nested dom object (if it is non-null) and
    // add the DOMAdapter to the list.
    if (dom instanceof AdaptiveResultTreeImpl) {
      final AdaptiveResultTreeImpl adaptiveRTF = (AdaptiveResultTreeImpl) dom;
      final DOM nestedDom = adaptiveRTF.getNestedDOM();
      if (nestedDom != null) {
        final DOMAdapter newAdapter = new DOMAdapter(nestedDom, adapter.getNamesArray(), adapter.getUrisArray(),
                adapter.getTypesArray(), adapter.getNamespaceArray());
        addDOMAdapter(newAdapter);
      }
    }

    return domNo;
  }

  public int getDocumentMask(String uri) {
    final Integer domIdx = (Integer) _documents.get(uri);
    if (domIdx == null)
      return -1;
    else
      return domIdx.intValue();
  }

  public DOM getDOMAdapter(String uri) {
    final Integer domIdx = (Integer) _documents.get(uri);
    if (domIdx == null)
      return null;
    else
      return _adapters[domIdx.intValue()];
  }

  @Override
  public int getDocument() {
    return _main.getDocument();
  }

  public DTMManager getDTMManager() {
    return _dtmManager;
  }

  /**
   * Returns singleton iterator containing the document root
   */
  @Override
  public DTMAxisIterator getIterator() {
    // main source document @ 0
    return _main.getIterator();
  }

  @Override
  public String getStringValue() {
    return _main.getStringValue();
  }

  @Override
  public DTMAxisIterator getChildren(final int node) {
    return _adapters[getDTMId(node)].getChildren(node);
  }

  @Override
  public DTMAxisIterator getTypedChildren(final int type) {
    return new AxisIterator(Axis.CHILD, type);
  }

  @Override
  public DTMAxisIterator getAxisIterator(final int axis) {
    return new AxisIterator(axis, NO_TYPE);
  }

  @Override
  public DTMAxisIterator getTypedAxisIterator(final int axis, final int type) {
    return new AxisIterator(axis, type);
  }

  @Override
  public DTMAxisIterator getNthDescendant(int node, int n, boolean includeself) {
    return _adapters[getDTMId(node)].getNthDescendant(node, n, includeself);
  }

  @Override
  public DTMAxisIterator getNodeValueIterator(DTMAxisIterator iterator, int type, String value, boolean op) {
    return new NodeValueIterator(iterator, type, value, op);
  }

  @Override
  public DTMAxisIterator getNamespaceAxisIterator(final int axis, final int ns) {
    final DTMAxisIterator iterator = _main.getNamespaceAxisIterator(axis, ns);
    return iterator;
  }

  @Override
  public DTMAxisIterator orderNodes(DTMAxisIterator source, int node) {
    return _adapters[getDTMId(node)].orderNodes(source, node);
  }

  @Override
  public int getExpandedTypeID(final int node) {
    if (node != DTM.NULL)
      return _adapters[node >>> DTMManager.IDENT_DTM_NODE_BITS].getExpandedTypeID(node);
    else
      return DTM.NULL;
  }

  @Override
  public int getNamespaceType(final int node) {
    return _adapters[getDTMId(node)].getNamespaceType(node);
  }

  @Override
  public int getNSType(int node) {
    return _adapters[getDTMId(node)].getNSType(node);
  }

  @Override
  public int getParent(final int node) {
    if (node == DTM.NULL)
      return DTM.NULL;
    return _adapters[node >>> DTMManager.IDENT_DTM_NODE_BITS].getParent(node);
  }

  @Override
  public int getAttributeNode(final int type, final int el) {
    if (el == DTM.NULL)
      return DTM.NULL;
    return _adapters[el >>> DTMManager.IDENT_DTM_NODE_BITS].getAttributeNode(type, el);
  }

  @Override
  public String getNodeName(final int node) {
    if (node == DTM.NULL)
      return "";
    return _adapters[node >>> DTMManager.IDENT_DTM_NODE_BITS].getNodeName(node);
  }

  @Override
  public String getNodeNameX(final int node) {
    if (node == DTM.NULL)
      return "";
    return _adapters[node >>> DTMManager.IDENT_DTM_NODE_BITS].getNodeNameX(node);
  }

  @Override
  public String getNamespaceName(final int node) {
    if (node == DTM.NULL)
      return "";
    return _adapters[node >>> DTMManager.IDENT_DTM_NODE_BITS].getNamespaceName(node);
  }

  @Override
  public String getStringValueX(final int node) {
    if (node == DTM.NULL)
      return "";
    return _adapters[node >>> DTMManager.IDENT_DTM_NODE_BITS].getStringValueX(node);
  }

  @Override
  public void copy(final int node, SerializationHandler handler) throws TransletException {
    if (node != DTM.NULL) {
      _adapters[node >>> DTMManager.IDENT_DTM_NODE_BITS].copy(node, handler);
    }
  }

  @Override
  public void copy(DTMAxisIterator nodes, SerializationHandler handler) throws TransletException {
    int node;
    while ((node = nodes.next()) != DTM.NULL) {
      _adapters[node >>> DTMManager.IDENT_DTM_NODE_BITS].copy(node, handler);
    }
  }

  @Override
  public String shallowCopy(final int node, SerializationHandler handler) throws TransletException {
    if (node == DTM.NULL)
      return "";
    return _adapters[node >>> DTMManager.IDENT_DTM_NODE_BITS].shallowCopy(node, handler);
  }

  @Override
  public boolean lessThan(final int node1, final int node2) {
    if (node1 == DTM.NULL)
      return true;
    if (node2 == DTM.NULL)
      return false;
    final int dom1 = getDTMId(node1);
    final int dom2 = getDTMId(node2);
    return dom1 == dom2 ? _adapters[dom1].lessThan(node1, node2) : dom1 < dom2;
  }

  @Override
  public void characters(final int textNode, SerializationHandler handler) throws TransletException {
    if (textNode != DTM.NULL) {
      _adapters[textNode >>> DTMManager.IDENT_DTM_NODE_BITS].characters(textNode, handler);
    }
  }

  @Override
  public void setFilter(StripFilter filter) {
    for (int dom = 0; dom < _free; dom++) {
      if (_adapters[dom] != null) {
        _adapters[dom].setFilter(filter);
      }
    }
  }

  @Override
  public Node makeNode(int index) {
    if (index == DTM.NULL)
      return null;
    return _adapters[getDTMId(index)].makeNode(index);
  }

  @Override
  public Node makeNode(DTMAxisIterator iter) {
    // TODO: gather nodes from all DOMs ?
    return _main.makeNode(iter);
  }

  @Override
  public NodeList makeNodeList(int index) {
    if (index == DTM.NULL)
      return null;
    return _adapters[getDTMId(index)].makeNodeList(index);
  }

  @Override
  public NodeList makeNodeList(DTMAxisIterator iter) {
    // TODO: gather nodes from all DOMs ?
    return _main.makeNodeList(iter);
  }

  @Override
  public String getLanguage(int node) {
    return _adapters[getDTMId(node)].getLanguage(node);
  }

  @Override
  public int getSize() {
    int size = 0;
    for (int i = 0; i < _size; i++) {
      size += _adapters[i].getSize();
    }
    return size;
  }

  @Override
  public String getDocumentURI(int node) {
    if (node == DTM.NULL) {
      node = DOM.NULL;
    }
    return _adapters[node >>> DTMManager.IDENT_DTM_NODE_BITS].getDocumentURI(0);
  }

  @Override
  public boolean isElement(final int node) {
    if (node == DTM.NULL)
      return false;
    return _adapters[node >>> DTMManager.IDENT_DTM_NODE_BITS].isElement(node);
  }

  @Override
  public boolean isAttribute(final int node) {
    if (node == DTM.NULL)
      return false;
    return _adapters[node >>> DTMManager.IDENT_DTM_NODE_BITS].isAttribute(node);
  }

  public int getDTMId(int nodeHandle) {
    if (nodeHandle == DTM.NULL)
      return 0;

    int id = nodeHandle >>> DTMManager.IDENT_DTM_NODE_BITS;
    while (id >= 2 && _adapters[id] == _adapters[id - 1]) {
      id--;
    }
    return id;
  }

  @Override
  public int getNodeIdent(int nodeHandle) {
    return _adapters[nodeHandle >>> DTMManager.IDENT_DTM_NODE_BITS].getNodeIdent(nodeHandle);
  }

  @Override
  public int getNodeHandle(int nodeId) {
    return _main.getNodeHandle(nodeId);
  }

  @Override
  public DOM getResultTreeFrag(int initSize, int rtfType) {
    return _main.getResultTreeFrag(initSize, rtfType);
  }

  @Override
  public DOM getResultTreeFrag(int initSize, int rtfType, boolean addToManager) {
    return _main.getResultTreeFrag(initSize, rtfType, addToManager);
  }

  public DOM getMain() {
    return _main;
  }

  /**
   * Returns a DOMBuilder class wrapped in a SAX adapter.
   */
  @Override
  public SerializationHandler getOutputDomBuilder() {
    return _main.getOutputDomBuilder();
  }

  @Override
  public String lookupNamespace(int node, String prefix) throws TransletException {
    return _main.lookupNamespace(node, prefix);
  }

  // %HZ% Does this method make any sense here???
  @Override
  public String getUnparsedEntityURI(String entity) {
    return _main.getUnparsedEntityURI(entity);
  }

  // %HZ% Does this method make any sense here???
  @Override
  public Hashtable getElementsWithIDs() {
    return _main.getElementsWithIDs();
  }
}
