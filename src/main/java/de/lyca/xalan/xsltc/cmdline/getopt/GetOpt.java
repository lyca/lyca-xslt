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
package de.lyca.xalan.xsltc.cmdline.getopt;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import de.lyca.xalan.xsltc.compiler.util.Messages;

/**
 * GetOpt is a Java equivalent to the C getopt() library function discussed in man page getopt(3C). It provides command
 * line parsing for Java applications. It supports the most rules of the command line standard (see man page intro(1))
 * including stacked options such as '-sxm' (which is equivalent to -s -x -m); it handles special '--' option that
 * signifies the end of options. Additionally this implementation of getopt will check for mandatory arguments to
 * options such as in the case of '-d {@literal <file>}' it will throw a MissingOptArgException if the option argument
 * '{@literal <file>}' is not included on the commandline. getopt(3C) does not check for this.
 * 
 * @author G Todd Miller
 */
public class GetOpt {
  public GetOpt(String[] args, String optString) {
    theOptions = new ArrayList<>();
    int currOptIndex = 0;
    theCmdArgs = new ArrayList<>();
    theOptionMatcher = new OptionMatcher(optString);
    // fill in the options list
    for (int i = 0; i < args.length; i++) {
      final String token = args[i];
      final int tokenLength = token.length();
      if (token.equals("--")) { // end of opts
        currOptIndex = i + 1; // set index of first operand
        break; // end of options
      } else if (token.startsWith("-") && tokenLength == 2) {
        // simple option token such as '-s' found
        theOptions.add(new Option(token.charAt(1)));
      } else if (token.startsWith("-") && tokenLength > 2) {
        // stacked options found, such as '-shm'
        // iterate thru the tokens after the dash and
        // add them to theOptions list
        for (int j = 1; j < tokenLength; j++) {
          theOptions.add(new Option(token.charAt(j)));
        }
      } else if (!token.startsWith("-")) {
        // case 1- there are not options stored yet therefore
        // this must be an command argument, not an option argument
        if (theOptions.size() == 0) {
          currOptIndex = i;
          break; // stop processing options
        } else {
          // case 2-
          // there are options stored, check to see if
          // this arg belong to the last arg stored
          int indexoflast = 0;
          indexoflast = theOptions.size() - 1;
          final Option op = theOptions.get(indexoflast);
          final char opLetter = op.getArgLetter();
          if (!op.hasArg() && theOptionMatcher.hasArg(opLetter)) {
            op.setArg(token);
          } else {
            // case 3 -
            // the last option stored does not take
            // an argument, so again, this argument
            // must be a command argument, not
            // an option argument
            currOptIndex = i;
            break; // end of options
          }
        }
      } // end option does not start with "-"
    } // end for args loop

    // attach an iterator to list of options
    theOptionsIterator = theOptions.listIterator();

    // options are done, now fill out cmd arg list with remaining args
    for (int i = currOptIndex; i < args.length; i++) {
      final String token = args[i];
      theCmdArgs.add(token);
    }
  }

  /**
   * debugging routine to print out all options collected
   */
  public void printOptions() {
    for (final ListIterator<Option> it = theOptions.listIterator(); it.hasNext();) {
      final Option opt = it.next();
      System.out.print("OPT =" + opt.getArgLetter());
      final String arg = opt.getArgument();
      if (arg != null) {
        System.out.print(" " + arg);
      }
      System.out.println();
    }
  }

  /**
   * gets the next option found in the commandline. Distinguishes between two bad cases, one case is when an illegal
   * option is found, and then other case is when an option takes an argument but no argument was found for that option.
   * If the option found was not declared in the optString, then an IllegalArgumentException will be thrown (case 1). If
   * the next option found has been declared to take an argument, and no such argument exists, then a
   * MissingOptArgException is thrown (case 2).
   * 
   * @return int - the next option found.
   * @throws IllegalArgumentException TODO
   * @throws MissingOptArgException TODO
   */
  public int getNextOption() throws IllegalArgumentException, MissingOptArgException {
    int retval = -1;
    if (theOptionsIterator.hasNext()) {
      theCurrentOption = theOptionsIterator.next();
      final char c = theCurrentOption.getArgLetter();
      final boolean shouldHaveArg = theOptionMatcher.hasArg(c);
      final String arg = theCurrentOption.getArgument();
      if (!theOptionMatcher.match(c)) {
        throw new IllegalArgumentException(Messages.get().illegalCmdlineOptionErr(new Character(c)));
      } else if (shouldHaveArg && arg == null) {
        throw new MissingOptArgException(Messages.get().cmdlineOptMissingArgErr(new Character(c)));
      }
      retval = c;
    }
    return retval;
  }

  /**
   * gets the argument for the current parsed option. For example, in case of '-d {@literal <file>}', if current option parsed is
   * 'd' then getOptionArg() would return '{@literal <file>}'.
   * 
   * @return String - argument for current parsed option.
   */
  public String getOptionArg() {
    String retval = null;
    final String tmp = theCurrentOption.getArgument();
    final char c = theCurrentOption.getArgLetter();
    if (theOptionMatcher.hasArg(c)) {
      retval = tmp;
    }
    return retval;
  }

  /**
   * gets list of the commandline arguments. For example, in command such as 'cmd -s -d file file2 file3 file4' with the
   * usage 'cmd [-s] [-d {@literal <file>}] {@literal <file>}...', getCmdArgs() would return the list {file2, file3, file4}.
   * 
   * @return String[] - list of command arguments that may appear after options and option arguments.
   */
  public String[] getCmdArgs() {
    return theCmdArgs.toArray(new String[theCmdArgs.size()]);
  }

  private Option theCurrentOption = null;
  private final ListIterator<Option> theOptionsIterator;
  private List<Option> theOptions = null;
  private List<String> theCmdArgs = null;
  private OptionMatcher theOptionMatcher = null;

  // /////////////////////////////////////////////////////////
  //
  // Inner Classes
  //
  // /////////////////////////////////////////////////////////

  // inner class to model an option
  static class Option {
    private final char theArgLetter;
    private String theArgument = null;

    public Option(char argLetter) {
      theArgLetter = argLetter;
    }

    public void setArg(String arg) {
      theArgument = arg;
    }

    public boolean hasArg() {
      return theArgument != null;
    }

    public char getArgLetter() {
      return theArgLetter;
    }

    public String getArgument() {
      return theArgument;
    }
  } // end class Option

  // inner class to query optString for a possible option match,
  // and whether or not a given legal option takes an argument.
  //
  static class OptionMatcher {
    public OptionMatcher(String optString) {
      theOptString = optString;
    }

    public boolean match(char c) {
      boolean retval = false;
      if (theOptString.indexOf(c) != -1) {
        retval = true;
      }
      return retval;
    }

    public boolean hasArg(char c) {
      boolean retval = false;
      final int index = theOptString.indexOf(c) + 1;
      if (index == theOptString.length()) {
        // reached end of theOptString
        retval = false;
      } else if (theOptString.charAt(index) == ':') {
        retval = true;
      }
      return retval;
    }

    private String theOptString = null;
  } // end class OptionMatcher
}// end class GetOpt
