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
import java.util.List;
import java.util.StringTokenizer;

import de.lyca.xalan.xsltc.compiler.util.CompilerContext;
import de.lyca.xalan.xsltc.compiler.util.ErrorMsg;
import de.lyca.xalan.xsltc.compiler.util.Type;
import de.lyca.xalan.xsltc.compiler.util.TypeCheckError;

/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 * @author Morten Jorgensen
 */
final class UseAttributeSets extends Instruction {

  // Only error that can occur:
  private final static String ATTR_SET_NOT_FOUND = "";

  // Contains the names of all references attribute sets
  private final List<QName> _sets = new ArrayList<>(2);

  /**
   * Constructur - define initial attribute sets to use
   */
  public UseAttributeSets(String setNames, Parser parser) {
    setParser(parser);
    addAttributeSets(setNames);
  }

  /**
   * This method is made public to enable an AttributeSet object to merge itself
   * with another AttributeSet (including any other AttributeSets the two may
   * inherit from).
   */
  public void addAttributeSets(String setNames) {
    if (setNames != null && !setNames.isEmpty()) {
      final StringTokenizer tokenizer = new StringTokenizer(setNames);
      while (tokenizer.hasMoreTokens()) {
        final QName qname = getParser().getQNameIgnoreDefaultNs(tokenizer.nextToken());
        _sets.add(qname);
      }
    }
  }

  /**
   * Do nada.
   */
  @Override
  public Type typeCheck(SymbolTable stable) throws TypeCheckError {
    return Type.Void;
  }

  /**
   * Generate a call to the method compiled for this attribute set
   */
  @Override
  public void translate(CompilerContext ctx) {
    // FIXME
    final SymbolTable symbolTable = getParser().getSymbolTable();

    // Go through each attribute set and generate a method call
    for (QName name : _sets) {
      // Get the AttributeSet reference from the symbol table
      final AttributeSet attrs = symbolTable.lookupAttributeSet(name);
      // Compile the call to the set's method if the set exists
      if (attrs != null) {
        ctx.currentBlock().add(attrs.compile(ctx));
      }
      // Generate an error if the attribute set does not exist
      else {
        final Parser parser = getParser();
        final String atrs = name.toString();
        reportError(this, parser, ErrorMsg.ATTRIBSET_UNDEF_ERR, atrs);
      }
    }
  }
}
