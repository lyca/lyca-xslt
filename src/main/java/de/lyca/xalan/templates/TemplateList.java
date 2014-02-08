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
package de.lyca.xalan.templates;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.transform.TransformerException;

import de.lyca.xalan.res.XSLTErrorResources;
import de.lyca.xml.dtm.DTM;
import de.lyca.xml.utils.QName;
import de.lyca.xpath.Expression;
import de.lyca.xpath.XPath;
import de.lyca.xpath.XPathContext;
import de.lyca.xpath.compiler.PseudoNames;
import de.lyca.xpath.patterns.NodeTest;
import de.lyca.xpath.patterns.StepPattern;
import de.lyca.xpath.patterns.UnionPattern;

/**
 * Encapsulates a template list, and helps locate individual templates.
 * 
 * @xsl.usage advanced
 */
public class TemplateList implements java.io.Serializable {
  static final long serialVersionUID = 5803675288911728791L;

  /**
   * Construct a TemplateList object. Needs to be public so it can be invoked
   * from the CompilingStylesheetHandler.
   */
  public TemplateList() {
    super();
  }

  /**
   * Add a template to the table of named templates and/or the table of
   * templates with match patterns. This routine should be called in decreasing
   * order of precedence but it checks nonetheless.
   * 
   * @param template
   */
  public void setTemplate(ElemTemplate template) {
    final XPath matchXPath = template.getMatch();

    if (null == template.getName() && null == matchXPath) {
      template.error(XSLTErrorResources.ER_NEED_NAME_OR_MATCH_ATTRIB, new Object[] { "xsl:template" });
    }

    if (null != template.getName()) {
      final ElemTemplate existingTemplate = m_namedTemplates.get(template.getName());
      if (null == existingTemplate) {
        m_namedTemplates.put(template.getName(), template);
      } else {
        final int existingPrecedence = existingTemplate.getStylesheetComposed().getImportCountComposed();
        final int newPrecedence = template.getStylesheetComposed().getImportCountComposed();
        if (newPrecedence > existingPrecedence) {
          // This should never happen
          m_namedTemplates.put(template.getName(), template);
        } else if (newPrecedence == existingPrecedence) {
          template.error(XSLTErrorResources.ER_DUPLICATE_NAMED_TEMPLATE, new Object[] { template.getName() });
        }
      }
    }

    if (null != matchXPath) {
      final Expression matchExpr = matchXPath.getExpression();

      if (matchExpr instanceof StepPattern) {
        insertPatternInTable((StepPattern) matchExpr, template);
      } else if (matchExpr instanceof UnionPattern) {
        final UnionPattern upat = (UnionPattern) matchExpr;
        final StepPattern[] pats = upat.getPatterns();
        final int n = pats.length;

        for (int i = 0; i < n; i++) {
          insertPatternInTable(pats[i], template);
        }
      } else {

        // TODO: assert error
      }
    }
  }

  /** Flag to indicate whether in DEBUG mode */
  final static boolean DEBUG = false;

  /**
   * Dump all patterns and elements that match those patterns
   * 
   */
  void dumpAssociationTables() {
    for (TemplateSubPatternAssociation head : m_patternTable.values()) {
      while (null != head) {
        System.out.print("(" + head.getTargetString() + ", " + head.getPattern() + ")");
        head = head.getNext();
      }
      System.out.println("\n.....");
    }

    TemplateSubPatternAssociation head = m_wildCardPatterns;
    System.out.print("wild card list: ");
    while (null != head) {
      System.out.print("(" + head.getTargetString() + ", " + head.getPattern() + ")");
      head = head.getNext();
    }
    System.out.println("\n.....");
  }

  /**
   * After all templates have been added, this function should be called.
   */
  public void compose(StylesheetRoot sroot) {

    if (DEBUG) {
      System.out.println("Before wildcard insert...");
      dumpAssociationTables();
    }

    if (null != m_wildCardPatterns) {
      for (TemplateSubPatternAssociation head : m_patternTable.values()) {
        TemplateSubPatternAssociation wild = m_wildCardPatterns;
        while (null != wild) {
          try {
            head = insertAssociationIntoList(head, (TemplateSubPatternAssociation) wild.clone(), true);
          } catch (final CloneNotSupportedException cnse) {
          }
          wild = wild.getNext();
        }
      }
    }

    if (DEBUG) {
      System.out.println("After wildcard insert...");
      dumpAssociationTables();
    }
  }

  /**
   * Insert the given TemplateSubPatternAssociation into the the linked list.
   * Sort by import precedence, then priority, then by document order.
   * 
   * @param head
   *          The first TemplateSubPatternAssociation in the linked list.
   * @param item
   *          The item that we want to insert into the proper place.
   * @param isWildCardInsert
   *          <code>true</code> if we are inserting a wild card template onto
   *          this list.
   * @return the new head of the list.
   */
  private TemplateSubPatternAssociation insertAssociationIntoList(TemplateSubPatternAssociation head,
          TemplateSubPatternAssociation item, boolean isWildCardInsert) {

    // Sort first by import level (higher level is at front),
    // then by priority (highest priority is at front),
    // then by document order (later in document is at front).

    final double priority = getPriorityOrScore(item);
    double workPriority;
    final int importLevel = item.getImportLevel();
    final int docOrder = item.getDocOrderPos();
    TemplateSubPatternAssociation insertPoint = head;
    TemplateSubPatternAssociation next;
    boolean insertBefore; // true means insert before insertPoint; otherwise
                          // after
                          // This can only be true if insertPoint is pointing to
                          // the first or last template.

    // Spin down so that insertPoint points to:
    // (a) the template immediately _before_ the first template on the chain
    // with
    // a precedence that is either (i) less than ours or (ii) the same as ours
    // but
    // the template document position is less than ours
    // -or-
    // (b) the last template on the chain if no such template described in (a)
    // exists.
    // If we are pointing to the first template or the last template (that is,
    // case b),
    // we need to determine whether to insert before or after the template.
    // Otherwise,
    // we always insert after the insertPoint.

    while (true) {
      next = insertPoint.getNext();
      if (null == next) {
        break;
      } else {
        workPriority = getPriorityOrScore(next);
        if (importLevel > next.getImportLevel()) {
          break;
        } else if (importLevel < next.getImportLevel()) {
          insertPoint = next;
        } else if (priority > workPriority) {
          break;
        } else if (priority < workPriority) {
          insertPoint = next;
        } else if (docOrder >= next.getDocOrderPos()) {
          break;
        } else {
          insertPoint = next;
        }
      }
    }

    if (null == next || insertPoint == head) // insert point is first or last
    {
      workPriority = getPriorityOrScore(insertPoint);
      if (importLevel > insertPoint.getImportLevel()) {
        insertBefore = true;
      } else if (importLevel < insertPoint.getImportLevel()) {
        insertBefore = false;
      } else if (priority > workPriority) {
        insertBefore = true;
      } else if (priority < workPriority) {
        insertBefore = false;
      } else if (docOrder >= insertPoint.getDocOrderPos()) {
        insertBefore = true;
      } else {
        insertBefore = false;
      }
    } else {
      insertBefore = false;
    }

    // System.out.println("appending: "+target+" to "+matchPat.getPattern());

    if (isWildCardInsert) {
      if (insertBefore) {
        item.setNext(insertPoint);

        final String key = insertPoint.getTargetString();

        item.setTargetString(key);
        putHead(key, item);
        return item;
      } else {
        item.setNext(next);
        insertPoint.setNext(item);
        return head;
      }
    } else {
      if (insertBefore) {
        item.setNext(insertPoint);

        if (insertPoint.isWild() || item.isWild()) {
          m_wildCardPatterns = item;
        } else {
          putHead(item.getTargetString(), item);
        }
        return item;
      } else {
        item.setNext(next);
        insertPoint.setNext(item);
        return head;
      }
    }
  }

  /**
   * Add a template to the template list.
   * 
   * @param pattern
   * @param template
   */
  private void insertPatternInTable(StepPattern pattern, ElemTemplate template) {

    final String target = pattern.getTargetString();

    if (null != target) {
      final String pstring = template.getMatch().getPatternString();
      final TemplateSubPatternAssociation association = new TemplateSubPatternAssociation(template, pattern, pstring);

      // See if there's already one there
      final boolean isWildCard = association.isWild();
      final TemplateSubPatternAssociation head = isWildCard ? m_wildCardPatterns : getHead(target);

      if (null == head) {
        if (isWildCard) {
          m_wildCardPatterns = association;
        } else {
          putHead(target, association);
        }
      } else {
        insertAssociationIntoList(head, association, false);
      }
    }
  }

  /**
   * Given a match pattern and template association, return the score of that
   * match. This score or priority can always be statically calculated.
   * 
   * @param matchPat
   *          The match pattern to template association.
   * 
   * @return {@link de.lyca.xpath.patterns.NodeTest#SCORE_NODETEST},
   *         {@link de.lyca.xpath.patterns.NodeTest#SCORE_NONE},
   *         {@link de.lyca.xpath.patterns.NodeTest#SCORE_NSWILD},
   *         {@link de.lyca.xpath.patterns.NodeTest#SCORE_QNAME}, or
   *         {@link de.lyca.xpath.patterns.NodeTest#SCORE_OTHER}, or the value
   *         defined by the priority attribute of the template.
   * 
   */
  private double getPriorityOrScore(TemplateSubPatternAssociation matchPat) {

    final double priority = matchPat.getTemplate().getPriority();

    if (priority == XPath.MATCH_SCORE_NONE) {
      final Expression ex = matchPat.getStepPattern();

      if (ex instanceof NodeTest)
        return ((NodeTest) ex).getDefaultScore();
    }

    return priority;
  }

  /**
   * Locate a named template.
   * 
   * @param qname
   *          Qualified name of the template.
   * 
   * @return Template argument with the requested name, or null if not found.
   */
  public ElemTemplate getTemplate(QName qname) {
    return m_namedTemplates.get(qname);
  }

  /**
   * Get the head of the most likely list of associations to check, based on the
   * name and type of the targetNode argument.
   * 
   * @param xctxt
   *          The XPath runtime context.
   * @param targetNode
   *          The target node that will be checked for a match.
   * @param dtm
   *          The dtm owner for the target node.
   * 
   * @return The head of a linked list that contains all possible match pattern
   *         to template associations.
   */
  public TemplateSubPatternAssociation getHead(XPathContext xctxt, int targetNode, DTM dtm) {
    final short targetNodeType = dtm.getNodeType(targetNode);
    TemplateSubPatternAssociation head;

    switch (targetNodeType) {
      case DTM.ELEMENT_NODE:
      case DTM.ATTRIBUTE_NODE:
        head = m_patternTable.get(dtm.getLocalName(targetNode));
        break;
      case DTM.TEXT_NODE:
      case DTM.CDATA_SECTION_NODE:
        head = m_textPatterns;
        break;
      case DTM.ENTITY_REFERENCE_NODE:
      case DTM.ENTITY_NODE:
        head = m_patternTable.get(dtm.getNodeName(targetNode)); // %REVIEW%
                                                                // I
                                                                // think
                                                                // this
                                                                // is
                                                                // right
        break;
      case DTM.PROCESSING_INSTRUCTION_NODE:
        head = m_patternTable.get(dtm.getLocalName(targetNode));
        break;
      case DTM.COMMENT_NODE:
        head = m_commentPatterns;
        break;
      case DTM.DOCUMENT_NODE:
      case DTM.DOCUMENT_FRAGMENT_NODE:
        head = m_docPatterns;
        break;
      case DTM.NOTATION_NODE:
      default:
        head = m_patternTable.get(dtm.getNodeName(targetNode)); // %REVIEW%
                                                                // I
                                                                // think
                                                                // this
                                                                // is
                                                                // right
    }

    return null == head ? m_wildCardPatterns : head;
  }

  /**
   * Given a target element, find the template that best matches in the given
   * XSL document, according to the rules specified in the xsl draft. This
   * variation of getTemplate assumes the current node and current expression
   * node have already been pushed.
   * 
   * @param xctxt
   * @param targetNode
   * @param mode
   *          A string indicating the display mode.
   * @param maxImportLevel
   *          The maximum importCountComposed that we should consider or -1 if
   *          we should consider all import levels. This is used by
   *          apply-imports to access templates that have been overridden.
   * @param quietConflictWarnings
   * @return Rule that best matches targetElem.
   * @throws XSLProcessorException
   *           thrown if the active ProblemListener and XPathContext decide the
   *           error condition is severe enough to halt processing.
   * 
   * @throws TransformerException
   */
  public ElemTemplate getTemplateFast(XPathContext xctxt, int targetNode, int expTypeID, QName mode,
          int maxImportLevel, boolean quietConflictWarnings, DTM dtm) throws TransformerException {

    TemplateSubPatternAssociation head;

    switch (dtm.getNodeType(targetNode)) {
      case DTM.ELEMENT_NODE:
      case DTM.ATTRIBUTE_NODE:
        head = m_patternTable.get(dtm.getLocalNameFromExpandedNameID(expTypeID));
        break;
      case DTM.TEXT_NODE:
      case DTM.CDATA_SECTION_NODE:
        head = m_textPatterns;
        break;
      case DTM.ENTITY_REFERENCE_NODE:
      case DTM.ENTITY_NODE:
        head = m_patternTable.get(dtm.getNodeName(targetNode)); // %REVIEW%
                                                                // I
                                                                // think
                                                                // this
                                                                // is
                                                                // right
        break;
      case DTM.PROCESSING_INSTRUCTION_NODE:
        head = m_patternTable.get(dtm.getLocalName(targetNode));
        break;
      case DTM.COMMENT_NODE:
        head = m_commentPatterns;
        break;
      case DTM.DOCUMENT_NODE:
      case DTM.DOCUMENT_FRAGMENT_NODE:
        head = m_docPatterns;
        break;
      case DTM.NOTATION_NODE:
      default:
        head = m_patternTable.get(dtm.getNodeName(targetNode)); // %REVIEW%
                                                                // I
                                                                // think
                                                                // this
                                                                // is
                                                                // right
    }

    if (null == head) {
      head = m_wildCardPatterns;
      if (null == head)
        return null;
    }

    // XSLT functions, such as xsl:key, need to be able to get to
    // current ElemTemplateElement via a cast to the prefix resolver.
    // Setting this fixes bug idkey03.
    xctxt.pushNamespaceContextNull();
    try {
      do {
        if (maxImportLevel > -1 && head.getImportLevel() > maxImportLevel) {
          continue;
        }
        final ElemTemplate template = head.getTemplate();
        xctxt.setNamespaceContext(template);

        if (head.m_stepPattern.execute(xctxt, targetNode, dtm, expTypeID) != NodeTest.SCORE_NONE
                && head.matchMode(mode)) {
          if (quietConflictWarnings) {
            checkConflicts(head, xctxt, targetNode, mode);
          }

          return template;
        }
      } while (null != (head = head.getNext()));
    } finally {
      xctxt.popNamespaceContext();
    }

    return null;
  } // end findTemplate

  /**
   * Given a target element, find the template that best matches in the given
   * XSL document, according to the rules specified in the xsl draft.
   * 
   * @param xctxt
   * @param targetNode
   * @param mode
   *          A string indicating the display mode.
   * @param quietConflictWarnings
   * @return Rule that best matches targetElem.
   * @throws XSLProcessorException
   *           thrown if the active ProblemListener and XPathContext decide the
   *           error condition is severe enough to halt processing.
   * 
   * @throws TransformerException
   */
  public ElemTemplate getTemplate(XPathContext xctxt, int targetNode, QName mode, boolean quietConflictWarnings, DTM dtm)
          throws TransformerException {

    TemplateSubPatternAssociation head = getHead(xctxt, targetNode, dtm);

    if (null != head) {
      // XSLT functions, such as xsl:key, need to be able to get to
      // current ElemTemplateElement via a cast to the prefix resolver.
      // Setting this fixes bug idkey03.
      xctxt.pushNamespaceContextNull();
      xctxt.pushCurrentNodeAndExpression(targetNode, targetNode);
      try {
        do {
          final ElemTemplate template = head.getTemplate();
          xctxt.setNamespaceContext(template);

          if (head.m_stepPattern.execute(xctxt, targetNode) != NodeTest.SCORE_NONE && head.matchMode(mode)) {
            if (quietConflictWarnings) {
              checkConflicts(head, xctxt, targetNode, mode);
            }

            return template;
          }
        } while (null != (head = head.getNext()));
      } finally {
        xctxt.popCurrentNodeAndExpression();
        xctxt.popNamespaceContext();
      }
    }

    return null;
  } // end findTemplate

  /**
   * Given a target element, find the template that best matches in the given
   * XSL document, according to the rules specified in the xsl draft.
   * 
   * @param xctxt
   * @param targetNode
   * @param mode
   *          A string indicating the display mode.
   * @param maxImportLevel
   *          The maximum importCountComposed that we should consider or -1 if
   *          we should consider all import levels. This is used by
   *          apply-imports to access templates that have been overridden.
   * @param endImportLevel
   *          The count of composed imports
   * @param quietConflictWarnings
   * @return Rule that best matches targetElem.
   * @throws XSLProcessorException
   *           thrown if the active ProblemListener and XPathContext decide the
   *           error condition is severe enough to halt processing.
   * 
   * @throws TransformerException
   */
  public ElemTemplate getTemplate(XPathContext xctxt, int targetNode, QName mode, int maxImportLevel,
          int endImportLevel, boolean quietConflictWarnings, DTM dtm) throws TransformerException {

    TemplateSubPatternAssociation head = getHead(xctxt, targetNode, dtm);

    if (null != head) {
      // XSLT functions, such as xsl:key, need to be able to get to
      // current ElemTemplateElement via a cast to the prefix resolver.
      // Setting this fixes bug idkey03.
      xctxt.pushNamespaceContextNull();
      xctxt.pushCurrentNodeAndExpression(targetNode, targetNode);
      try {
        do {
          if (maxImportLevel > -1 && head.getImportLevel() > maxImportLevel) {
            continue;
          }
          if (head.getImportLevel() <= maxImportLevel - endImportLevel)
            return null;
          final ElemTemplate template = head.getTemplate();
          xctxt.setNamespaceContext(template);

          if (head.m_stepPattern.execute(xctxt, targetNode) != NodeTest.SCORE_NONE && head.matchMode(mode)) {
            if (quietConflictWarnings) {
              checkConflicts(head, xctxt, targetNode, mode);
            }

            return template;
          }
        } while (null != (head = head.getNext()));
      } finally {
        xctxt.popCurrentNodeAndExpression();
        xctxt.popNamespaceContext();
      }
    }

    return null;
  } // end findTemplate

  /**
   * Get a TemplateWalker for use by a compiler. See the documentation for the
   * TreeWalker inner class for further details.
   */
  public TemplateWalker getWalker() {
    return new TemplateWalker();
  }

  /**
   * Check for match conflicts, and warn the stylesheet author.
   * 
   * @param head
   *          Template pattern
   * @param xctxt
   *          Current XPath context
   * @param targetNode
   *          Node matching the pattern
   * @param mode
   *          reference, which may be null, to the <a
   *          href="http://www.w3.org/TR/xslt#modes">current mode</a>.
   */
  private void checkConflicts(TemplateSubPatternAssociation head, XPathContext xctxt, int targetNode, QName mode) {

    // TODO: Check for conflicts.
  }

  /**
   * Keyed on string macro names, and holding values that are macro elements in
   * the XSL DOM tree. Initialized in initMacroLookupTable, and used in
   * findNamedTemplate.
   * 
   * @serial
   */
  private final Map<QName, ElemTemplate> m_namedTemplates = new HashMap<>(89);

  /**
   * This table is keyed on the target elements of patterns, and contains linked
   * lists of the actual patterns that match the target element to some degree
   * of specifity.
   * 
   * @serial
   */
  private final Map<String, TemplateSubPatternAssociation> m_patternTable = new HashMap<>(89);

  /**
   * Wildcard patterns.
   * 
   * @serial
   */
  private TemplateSubPatternAssociation m_wildCardPatterns = null;

  /**
   * Text Patterns.
   * 
   * @serial
   */
  private TemplateSubPatternAssociation m_textPatterns = null;

  /**
   * Root document Patterns.
   * 
   * @serial
   */
  private TemplateSubPatternAssociation m_docPatterns = null;

  /**
   * Comment Patterns.
   * 
   * @serial
   */
  private TemplateSubPatternAssociation m_commentPatterns = null;

  /**
   * Get the head of the assocation list that is keyed by target.
   * 
   * @param key
   *          The name of a node.
   * 
   * @return The head of a linked list that contains all possible match pattern
   *         to template associations for the given key.
   */
  private TemplateSubPatternAssociation getHead(String key) {
    return m_patternTable.get(key);
  }

  /**
   * Get the head of the assocation list that is keyed by target.
   * 
   * @param key
   * @param assoc
   */
  private void putHead(String key, TemplateSubPatternAssociation assoc) {

    if (key.equals(PseudoNames.PSEUDONAME_TEXT)) {
      m_textPatterns = assoc;
    } else if (key.equals(PseudoNames.PSEUDONAME_ROOT)) {
      m_docPatterns = assoc;
    } else if (key.equals(PseudoNames.PSEUDONAME_COMMENT)) {
      m_commentPatterns = assoc;
    }

    m_patternTable.put(key, assoc);
  }

  /**
   * An inner class used by a compiler to iterate over all of the ElemTemplates
   * stored in this TemplateList. The compiler can replace returned templates
   * with their compiled equivalent.
   */
  public class TemplateWalker {
    private final Iterator<TemplateSubPatternAssociation> tspaIterator;
    private Iterator<ElemTemplate> etIterator;
    private boolean inPatterns;
    private TemplateSubPatternAssociation curPattern;

    private final Map<Integer,ElemTemplate> m_compilerCache = new HashMap<>();

    private TemplateWalker() {
      tspaIterator = m_patternTable.values().iterator();
      inPatterns = true;
      curPattern = null;
    }

    public ElemTemplate next() {

      ElemTemplate retValue = null;
      ElemTemplate ct;

      while (true) {
        if (inPatterns) {
          if (null != curPattern) {
            curPattern = curPattern.getNext();
          }

          if (null != curPattern) {
            retValue = curPattern.getTemplate();
          } else {
            if (tspaIterator.hasNext()) {
              curPattern = tspaIterator.next();
              retValue = curPattern.getTemplate();
            } else {
              inPatterns = false;
              etIterator = m_namedTemplates.values().iterator();
            }
          }
        }

        if (!inPatterns) {
          if (etIterator.hasNext()) {
            retValue = etIterator.next();
          } else
            return null;
        }

        ct = m_compilerCache.get(retValue.getUid());
        if (null == ct) {
          m_compilerCache.put(retValue.getUid(), retValue);
          return retValue;
        }
      }
    }
  }

}