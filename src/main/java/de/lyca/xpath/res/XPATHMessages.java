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
package de.lyca.xpath.res;

import java.text.MessageFormat;
import java.util.ListResourceBundle;

import de.lyca.xml.res.XMLMessages;

/**
 * A utility class for issuing XPath error messages.
 */
public class XPATHMessages extends XMLMessages {
  /** The language specific resource object for XPath messages. */
  private static ListResourceBundle XPATHBundle = null;

  /** The class name of the XPath error message string table. */
  private static final String XPATH_ERROR_RESOURCES = "de.lyca.xpath.res.XPATHErrorResources";

  /**
   * Creates a message from the specified key and replacement arguments, localized to the given locale.
   * 
   * @param msgKey The key for the message text.
   * @param args The arguments to be used as replacement text in the message created.
   * 
   * @return The formatted message string.
   */
  public static final String createXPATHMessage(String msgKey, Object args[]) {
    if (XPATHBundle == null) {
      XPATHBundle = loadResourceBundle(XPATH_ERROR_RESOURCES);
    }

    if (XPATHBundle != null)
      return createXPATHMsg(XPATHBundle, msgKey, args);
    else
      return "Could not load any resource bundles.";
  }

  /**
   * Creates a message from the specified key and replacement arguments, localized to the given locale.
   * 
   * @param msgKey The key for the message text.
   * @param args The arguments to be used as replacement text in the message created.
   * 
   * @return The formatted warning string.
   */
  public static final String createXPATHWarning(String msgKey, Object args[]) {
    if (XPATHBundle == null) {
      XPATHBundle = loadResourceBundle(XPATH_ERROR_RESOURCES);
    }

    if (XPATHBundle != null)
      return createXPATHMsg(XPATHBundle, msgKey, args);
    else
      return "Could not load any resource bundles.";
  }

  /**
   * Creates a message from the specified key and replacement arguments, localized to the given locale.
   * 
   * @param fResourceBundle The resource bundle to use.
   * @param msgKey The message key to use.
   * @param args The arguments to be used as replacement text in the message created.
   * 
   * @return The formatted message string.
   */
  public static final String createXPATHMsg(ListResourceBundle fResourceBundle, String msgKey, Object args[]) {

    String fmsg = null;
    boolean throwex = false;
    String msg = null;

    if (msgKey != null) {
      msg = fResourceBundle.getString(msgKey);
    }

    if (msg == null) {
      msg = fResourceBundle.getString("BAD_CODE");
      throwex = true;
    }

    if (args != null) {
      try {

        // Do this to keep format from crying.
        // This is better than making a bunch of conditional
        // code all over the place.
        final int n = args.length;

        for (int i = 0; i < n; i++) {
          if (null == args[i]) {
            args[i] = "";
          }
        }

        fmsg = MessageFormat.format(msg, args);
      } catch (final Exception e) {
        fmsg = fResourceBundle.getString("FORMAT_FAILED");
        fmsg += " " + msg;
      }
    } else {
      fmsg = msg;
    }

    if (throwex)
      throw new RuntimeException(fmsg);

    return fmsg;
  }

}
