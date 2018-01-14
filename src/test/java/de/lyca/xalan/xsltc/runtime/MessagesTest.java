package de.lyca.xalan.xsltc.runtime;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

public class MessagesTest {

  // Just as example - Better scan your classpath for message_*.properties
  private static String[] propertyFiles = new String[] { //
      "de/lyca/xalan/xsltc/runtime/ErrorMessages_ca.properties", //
      "de/lyca/xalan/xsltc/runtime/ErrorMessages_cs.properties", //
      "de/lyca/xalan/xsltc/runtime/ErrorMessages_de.properties", //
      "de/lyca/xalan/xsltc/runtime/ErrorMessages_es.properties", //
      "de/lyca/xalan/xsltc/runtime/ErrorMessages_fr.properties", //
      "de/lyca/xalan/xsltc/runtime/ErrorMessages_hu.properties", //
      "de/lyca/xalan/xsltc/runtime/ErrorMessages_it.properties", //
      "de/lyca/xalan/xsltc/runtime/ErrorMessages_ja.properties", //
      "de/lyca/xalan/xsltc/runtime/ErrorMessages_ko.properties", //
      "de/lyca/xalan/xsltc/runtime/ErrorMessages_no.properties", //
      "de/lyca/xalan/xsltc/runtime/ErrorMessages_pl.properties", //
      "de/lyca/xalan/xsltc/runtime/ErrorMessages_pt_BR.properties", //
      "de/lyca/xalan/xsltc/runtime/ErrorMessages_ru.properties", //
      "de/lyca/xalan/xsltc/runtime/ErrorMessages_sk.properties", //
      "de/lyca/xalan/xsltc/runtime/ErrorMessages_sl.properties", //
      "de/lyca/xalan/xsltc/runtime/ErrorMessages_tr.properties", //
      "de/lyca/xalan/xsltc/runtime/ErrorMessages_zh_TW.properties", //
      "de/lyca/xalan/xsltc/runtime/ErrorMessages_zh.properties", //
      "de/lyca/xalan/xsltc/runtime/ErrorMessages.properties" //
  };

  private static List<String> methodNames = new ArrayList<String>();
  private static Map<String, Properties> bundles = new HashMap<String, Properties>();

  @BeforeClass
  public static void prepare() throws Exception {
    // Get Method-Names
    Method[] methods = BasicLibraryErrorMessages.class.getDeclaredMethods();
    for (Method method : methods) {
      methodNames.add(method.getName());
    }

    // Get all messages
    for (String propertyFile : propertyFiles) {
      Properties properties = new Properties();
      URL url = ClassLoader.getSystemResource(propertyFile);
      properties.load(url.openStream());
      bundles.put(propertyFile, properties);
    }
  }

  /**
   * Is there an interface-method for every entry in our properties?
   * 
   * @throws IOException
   *           ignore
   */
  @Test
  public void shouldHaveMessagesForAllInterafaceMethods() throws IOException {
    Set<String> error = new HashSet<String>();

    for (String methodName : methodNames) {
      for (String propertyFile : propertyFiles) {
        if (!bundles.get(propertyFile).containsKey(methodName)) {
          error.add(propertyFile + "#" + methodName);
        }
      }
    }

    if (!error.isEmpty()) {
      fail("No translations for " + error);
    }

  }

  /**
   * Is there an entry in each message.properties for every method in the
   * interface?
   * 
   * @throws IOException
   *           ignore
   */
  @Test
  public void shouldHaveInterfaceMethodForAllMessages() throws IOException {
    Set<String> error = new HashSet<String>();

    for (String propertyFile : propertyFiles) {
      Properties bundle = bundles.get(propertyFile);

      for (Object messageObj : bundle.keySet()) {
        String message = messageObj.toString();
        if (!methodNames.contains(message)) {
          error.add(propertyFile + "#" + message);
        }
      }
    }

    if (!error.isEmpty()) {
      fail("No interface method for : " + error);
    }
  }

}
