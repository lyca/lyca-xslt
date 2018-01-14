/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.lyca.xalan.xsltc.runtime;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public final class Messages {

  private static final String BUNDLE_NAME = "de.lyca.xalan.xsltc.runtime.ErrorMessages";

  private static BasicLibraryErrorMessages messages = (BasicLibraryErrorMessages) Proxy.newProxyInstance(//
      BasicLibraryErrorMessages.class.getClassLoader(), //
      new Class[] { BasicLibraryErrorMessages.class }, //
      new MessageResolver());

  private Messages() {
    // No instances
  }

  private static class MessageResolver implements InvocationHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
      return Messages.getString(method.getName(), args);
    }
  }

  /**
   * @return Proxy of BasicLibraryErrorMessages - Can be used to access all errors
   */
  public static BasicLibraryErrorMessages get() {
    return messages;
  }

  private static String getString(String key, Object... args) {
    Locale locale = Locale.getDefault();
    try {
      String message = ResourceBundle.getBundle(BUNDLE_NAME, locale).getString(key);
      if (args != null) {
        MessageFormat formatter = new MessageFormat(message, locale);
        message = formatter.format(args);
      }
      return message;
    } catch (MissingResourceException e) {
      return '!' + key + '!';
    }
  }
}
