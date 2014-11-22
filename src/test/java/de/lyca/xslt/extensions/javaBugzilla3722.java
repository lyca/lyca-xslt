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

// TODO is this needed for the test? explicitly packageless
package de.lyca.xslt.extensions;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Extension for testing xml-xalan/samples/extensions.
 */
public class javaBugzilla3722 {
  static Map<String, String> counters = new HashMap<>();

  /** Plain counter of number of times called. */
  private static int counter = 0;

  /** Extension method from Bugzilla3722. */
  public static String dumpConfig(NodeList conf) {
    counter++;
    if (conf != null) {
      for (int i = 0; i < conf.getLength(); i++) {
        final Node node = conf.item(i);
        if (node != null) {
          if (node.hasChildNodes()) {
            // Below line throws DTMDOMException on CVS code 21-Sep-01
            final NodeList subList = node.getChildNodes();
            dumpConfig(subList);
          } else {
            // Output info about the node for later debugging
            counters.put(node.getNodeName(), node.getNodeValue());
          }
        }
      }
    }
    return "dumpConfig.count=" + counter;
  }

  /**
   * Description of what this extension does.
   * 
   * @return String description of extension
   */
  public static String getDescription() {
    return "Reproduce Bugzilla # 3722";
  }
}
