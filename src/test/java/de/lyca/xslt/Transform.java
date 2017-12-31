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

package de.lyca.xslt;

import java.io.StringWriter;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

public class Transform {
  private static final TransformerFactory TF = TransformerFactory.newInstance();

//  private static final TransformerFactory TF = TransformerFactory.newInstance(
//      "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl", Transform.class.getClassLoader());

  private final Source inputSource;
  private Transformer transformer;

  public Transform(Source inputSource, Source stylesheetSource) throws TransformerConfigurationException {
    this.inputSource = inputSource;
    transformer = stylesheetSource == null ? TF.newTransformer() : TF.newTransformer(stylesheetSource);
  }

  public String getResultString() throws TransformerException {
    StringWriter outputWriter = new StringWriter();
    transformer.transform(inputSource, new StreamResult(outputWriter));
    return outputWriter.toString();
  }

  public void setErrorListener(ErrorListener errorListener) {
    transformer.setErrorListener(errorListener);
  }

  public void setParameter(String name, Object value) {
    transformer.setParameter(name, value);
}

}
