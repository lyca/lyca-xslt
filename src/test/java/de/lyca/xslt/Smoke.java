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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class Smoke {

  private static final int NUMBER = 3;

  private static TransformerFactory TF;
  private static Templates T;
  private static final StringBuffer sb = new StringBuffer();

  @BeforeClass
  public static void init() throws Exception {
    TF = TransformerFactory.newInstance();
    final Source source = new StreamSource("src/test/java/de/lyca/xslt/NewStylesheet.xsl");
    T = TF.newTemplates(source);
    System.out.println(TF.getClass().getName());
  }

  @Test
  @Ignore
  public void testCreateFastTransformers() throws Exception {
    final ExecutorService es = Executors.newFixedThreadPool(NUMBER);
    final List<CreateTransformers> addTransformers = new ArrayList<>(NUMBER);
    for (int i = 0; i < NUMBER; i++) {
      addTransformers.add(new CreateTransformers());
    }
    final List<Future<List<Transformer>>> createdTransformers = es.invokeAll(addTransformers);
    System.out.println(sb);
    printMemoryUsage();
    System.out.println(createdTransformers.size());
    for (final Future<List<Transformer>> future : createdTransformers) {
      System.out.println(future.get().size());
    }
  }

  private static class CreateTransformers implements Callable<List<Transformer>> {
    @Override
    public List<Transformer> call() throws Exception {
      final List<Transformer> transformers = new ArrayList<>(500_000);
      sb.append("Start: " + Thread.currentThread() + "\n");
      final long start = System.currentTimeMillis();
      for (int i = 1; i < 400_001; i++) {
        if (i % 100_000 == 0) {
          sb.append(Thread.currentThread().toString() + ' ' + i + '\n');
        }
        try {
          transformers.add(T.newTransformer());
        } catch (final TransformerConfigurationException e) {
          e.printStackTrace();
        }
      }
      sb.append("End : " + Thread.currentThread() + " took " + (System.currentTimeMillis() - start) + "ms.\n");
      return transformers;
    }
  }

  private static void printMemoryUsage() {
    System.gc();
    final int mb = 1024 * 1024;
    // Getting the runtime reference from system
    final Runtime runtime = Runtime.getRuntime();
    System.out.println("##### Heap utilization statistics [MB] #####");
    // Print used memory
    System.out.println("Used Memory:" + (runtime.totalMemory() - runtime.freeMemory()) / mb);
    // Print free memory
    System.out.println("Free Memory:" + runtime.freeMemory() / mb);
    // Print total available memory
    System.out.println("Total Memory:" + runtime.totalMemory() / mb);
    // Print Maximum available memory
    System.out.println("Max Memory:" + runtime.maxMemory() / mb);
  }

}
