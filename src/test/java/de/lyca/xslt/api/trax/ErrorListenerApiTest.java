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
 * $Id: ErrorListenerAPITest.java 470101 2006-11-01 21:03:00Z minchau $
 */

/*
 *
 * ErrorListenerAPITest.java
 *
 */
package de.lyca.xslt.api.trax;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

import org.junit.Test;

import de.lyca.xml.utils.DefaultErrorHandler;

/**
 * API Coverage test for ErrorListener; defaults to Xalan impl. Only very basic
 * API coverage.
 * 
 * @author shane_curcuru@lotus.com
 * 
 */
public class ErrorListenerApiTest {

  @Test
  public void test1() throws Exception {
    // TODO check System Error
    final ErrorListener errorListener = new DefaultErrorHandler();
    final TransformerException tex = new TransformerException("TransformerException-Warning");
    errorListener.warning(tex);
  }

  @Test(expected = TransformerException.class)
  public void test2() throws Exception {
    final ErrorListener errorListener = new DefaultErrorHandler();
    final Exception ex = new Exception("Exception-message-here");
    final TransformerException tex = new TransformerException("TransformerException-Error", ex);
    errorListener.error(tex);
  }

  @Test(expected = TransformerException.class)
  public void test3() throws Exception {
    final ErrorListener errorListener = new DefaultErrorHandler();
    final Exception ex = new Exception("Exception-message-here");
    final TransformerException tex = new TransformerException("TransformerException-Fatal-Error", ex);
    errorListener.fatalError(tex);
  }

}
