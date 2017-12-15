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

package de.lyca.xalan;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

/**
 * Security related methods that only work on J2SE 1.2 and newer.
 */
public final class SecuritySupport {

  public static ClassLoader getContextClassLoader() {
    return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
      @Override
      public ClassLoader run() {
        ClassLoader cl = null;
        try {
          cl = Thread.currentThread().getContextClassLoader();
        } catch (final SecurityException ex) {
        }
        return cl;
      }
    });
  }

  public static ClassLoader getSystemClassLoader() {
    return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
      @Override
      public ClassLoader run() {
        ClassLoader cl = null;
        try {
          cl = ClassLoader.getSystemClassLoader();
        } catch (final SecurityException ex) {
        }
        return cl;
      }
    });
  }

  public static ClassLoader getParentClassLoader(final ClassLoader cl) {
    return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
      @Override
      public ClassLoader run() {
        ClassLoader parent = null;
        try {
          parent = cl.getParent();
        } catch (final SecurityException ex) {
        }

        // eliminate loops in case of the boot
        // ClassLoader returning itself as a parent
        return parent == cl ? null : parent;
      }
    });
  }

  public static String getSystemProperty(final String propName) {
    return AccessController.doPrivileged((PrivilegedAction<String>) () -> System.getProperty(propName));
  }

  public static FileInputStream getFileInputStream(final File file) throws FileNotFoundException {
    try {
      return AccessController
          .doPrivileged((PrivilegedExceptionAction<FileInputStream>) () -> new FileInputStream(file));
    } catch (final PrivilegedActionException e) {
      throw (FileNotFoundException) e.getException();
    }
  }

  public static InputStream getResourceAsStream(final ClassLoader cl, final String name) {
    return AccessController.doPrivileged((PrivilegedAction<InputStream>) () -> cl == null
        ? ClassLoader.getSystemResourceAsStream(name) : cl.getResourceAsStream(name));
  }

  public static boolean getFileExists(final File f) {
    return AccessController.doPrivileged((PrivilegedAction<Boolean>) () -> f.exists());
  }

  public static long getLastModified(final File f) {
    return AccessController.doPrivileged((PrivilegedAction<Long>) () -> f.lastModified());
  }

  private SecuritySupport() {
  }

}
