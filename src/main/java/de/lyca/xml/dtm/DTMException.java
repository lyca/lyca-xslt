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
package de.lyca.xml.dtm;

import java.io.PrintStream;
import java.io.PrintWriter;

import javax.xml.transform.SourceLocator;

import de.lyca.xml.res.Messages;

/**
 * This class specifies an exceptional condition that occured in the DTM module.
 */
public class DTMException extends RuntimeException {
  static final long serialVersionUID = -775576419181334734L;

  /**
   * Field locator specifies where the error occured.
   * 
   * @serial
   */
  SourceLocator locator;

  /**
   * Method getLocator retrieves an instance of a SourceLocator object that
   * specifies where an error occured.
   * 
   * @return A SourceLocator object, or null if none was specified.
   */
  public SourceLocator getLocator() {
    return locator;
  }

  /**
   * Method setLocator sets an instance of a SourceLocator object that specifies
   * where an error occured.
   * 
   * @param location
   *          A SourceLocator object, or null to clear the location.
   */
  public void setLocator(SourceLocator location) {
    locator = location;
  }

  /**
   * Field containedException specifies a wrapped exception. May be null.
   * 
   * @serial
   */
  Throwable containedException;

  /**
   * This method retrieves an exception that this exception wraps.
   * 
   * @return An Throwable object, or null.
   * @see #getCause
   */
  public Throwable getException() {
    return containedException;
  }

  /**
   * Returns the cause of this throwable or <code>null</code> if the cause is
   * nonexistent or unknown. (The cause is the throwable that caused this
   * throwable to get thrown.)
   */
  @Override
  public Throwable getCause() {

    return containedException == this ? null : containedException;
  }

  /**
   * Initializes the <i>cause</i> of this throwable to the specified value. (The
   * cause is the throwable that caused this throwable to get thrown.)
   * 
   * <p>
   * This method can be called at most once. It is generally called from within
   * the constructor, or immediately after creating the throwable. If this
   * throwable was created with {@link #DTMException(Throwable)} or
   * {@link #DTMException(String,Throwable)}, this method cannot be called even
   * once.
   * 
   * @param cause
   *          the cause (which is saved for later retrieval by the
   *          {@link #getCause()} method). (A <tt>null</tt> value is permitted,
   *          and indicates that the cause is nonexistent or unknown.)
   * @return a reference to this <code>Throwable</code> instance.
   * @throws IllegalArgumentException
   *           if <code>cause</code> is this throwable. (A throwable cannot be
   *           its own cause.)
   * @throws IllegalStateException
   *           if this throwable was created with
   *           {@link #DTMException(Throwable)} or
   *           {@link #DTMException(String,Throwable)}, or this method has
   *           already been called on this throwable.
   */
  @Override
  public synchronized Throwable initCause(Throwable cause) {

    if (containedException == null && cause != null)
      throw new IllegalStateException(Messages.get().cannotOverwriteCause()); // "Can't overwrite cause");

    if (cause == this)
      throw new IllegalArgumentException(Messages.get().selfCausationNotPermitted()); // "Self-causation not permitted");

    containedException = cause;

    return this;
  }

  /**
   * Create a new DTMException.
   * 
   * @param message
   *          The error or warning message.
   */
  public DTMException(String message) {

    super(message);

    containedException = null;
    locator = null;
  }

  /**
   * Create a new DTMException wrapping an existing exception.
   * 
   * @param e
   *          The exception to be wrapped.
   */
  public DTMException(Throwable e) {

    super(e.getMessage());

    containedException = e;
    locator = null;
  }

  /**
   * Wrap an existing exception in a DTMException.
   * 
   * <p>
   * This is used for throwing processor exceptions before the processing has
   * started.
   * </p>
   * 
   * @param message
   *          The error or warning message, or null to use the message from the
   *          embedded exception.
   * @param e
   *          Any exception
   */
  public DTMException(String message, Throwable e) {

    super(message == null || message.length() == 0 ? e.getMessage() : message);

    containedException = e;
    locator = null;
  }

  /**
   * Create a new DTMException from a message and a Locator.
   * 
   * <p>
   * This constructor is especially useful when an application is creating its
   * own exception from within a DocumentHandler callback.
   * </p>
   * 
   * @param message
   *          The error or warning message.
   * @param locator
   *          The locator object for the error or warning.
   */
  public DTMException(String message, SourceLocator locator) {

    super(message);

    containedException = null;
    this.locator = locator;
  }

  /**
   * Wrap an existing exception in a DTMException.
   * 
   * @param message
   *          The error or warning message, or null to use the message from the
   *          embedded exception.
   * @param locator
   *          The locator object for the error or warning.
   * @param e
   *          Any exception
   */
  public DTMException(String message, SourceLocator locator, Throwable e) {

    super(message);

    containedException = e;
    this.locator = locator;
  }

  /**
   * Get the error message with location information appended.
   * @return TODO
   */
  public String getMessageAndLocation() {

    final StringBuilder sbuffer = new StringBuilder();
    final String message = super.getMessage();

    if (null != message) {
      sbuffer.append(message);
    }

    if (null != locator) {
      final String systemID = locator.getSystemId();
      final int line = locator.getLineNumber();
      final int column = locator.getColumnNumber();

      if (null != systemID) {
        sbuffer.append("; SystemID: ");
        sbuffer.append(systemID);
      }

      if (0 != line) {
        sbuffer.append("; Line#: ");
        sbuffer.append(line);
      }

      if (0 != column) {
        sbuffer.append("; Column#: ");
        sbuffer.append(column);
      }
    }

    return sbuffer.toString();
  }

  /**
   * Get the location information as a string.
   * 
   * @return A string with location info, or null if there is no location
   *         information.
   */
  public String getLocationAsString() {

    if (null != locator) {
      final StringBuilder sbuffer = new StringBuilder();
      final String systemID = locator.getSystemId();
      final int line = locator.getLineNumber();
      final int column = locator.getColumnNumber();

      if (null != systemID) {
        sbuffer.append("; SystemID: ");
        sbuffer.append(systemID);
      }

      if (0 != line) {
        sbuffer.append("; Line#: ");
        sbuffer.append(line);
      }

      if (0 != column) {
        sbuffer.append("; Column#: ");
        sbuffer.append(column);
      }

      return sbuffer.toString();
    } else
      return null;
  }

  /**
   * Print the the trace of methods from where the error originated. This will
   * trace all nested exception objects, as well as this object.
   */
  @Override
  public void printStackTrace() {
    printStackTrace(new PrintWriter(System.err, true));
  }

  /**
   * Print the the trace of methods from where the error originated. This will
   * trace all nested exception objects, as well as this object.
   * 
   * @param s
   *          The stream where the dump will be sent to.
   */
  @Override
  public void printStackTrace(PrintStream s) {
    printStackTrace(new PrintWriter(s));
  }

  /**
   * Print the the trace of methods from where the error originated. This will
   * trace all nested exception objects, as well as this object.
   * 
   * @param s
   *          The writer where the dump will be sent to.
   */
  @Override
  public void printStackTrace(PrintWriter s) {
    if (s == null) {
      s = new PrintWriter(System.err, true);
    }
    try {
      final String locInfo = getLocationAsString();
      if (null != locInfo) {
        s.println(locInfo);
      }
      super.printStackTrace(s);
    } catch (final Throwable e) {
    }
  }

}
