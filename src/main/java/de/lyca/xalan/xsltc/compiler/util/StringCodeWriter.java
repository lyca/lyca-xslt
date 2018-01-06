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

package de.lyca.xalan.xsltc.compiler.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.tools.JavaFileObject;

import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JPackage;

public class StringCodeWriter extends CodeWriter {

  private final Map<String, ByteArrayOutputStream> sourceHolder = new LinkedHashMap<>();

  public StringCodeWriter() {
  }

  @Override
  public OutputStream openBinary(JPackage pkg, String className) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream(8192);
    sourceHolder.put(fullClassName(pkg, className), baos);
    return baos;
  }

  @Override
  public void close() {
    // Cannot close ByteArrayOutputStream
  }

  public Iterable<? extends JavaFileObject> getJavaFileObjects() {
    List<JavaFileObject> javaFileObjects = new ArrayList<>();
    for (Entry<String, ByteArrayOutputStream> e : sourceHolder.entrySet()) {
      try {
        javaFileObjects
            .add(new StringJavaFileObject(e.getKey(), e.getValue().toString(StandardCharsets.UTF_8.toString())));
      } catch (UnsupportedEncodingException ex) {
        throw new RuntimeException("Should never happen!", ex);
      }
    }
    return javaFileObjects;
  }

  private String fullClassName(JPackage pkg, String className) throws IOException {
    return pkg.isUnnamed() ? className : pkg.name() + '.' + className;
  }

}
