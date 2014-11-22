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
 * $Id: URIResolverTest.java 470101 2006-11-01 21:03:00Z minchau $
 */

/*
 *
 * URIResolverTest.java
 *
 */
package de.lyca.xslt.api.trax;

import static de.lyca.xslt.ResourceUtils.getSource;
import static de.lyca.xslt.ResourceUtils.readResource;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Assert;
import org.junit.Test;

import de.lyca.xml.utils.SystemIDResolver;

/**
 * Verify that URIResolvers are called properly.
 * 
 * @author Original: shane_curcuru@lotus.com
 * @author Refactored: l.michele@lyca.de
 */
public class URIResolverTest {

  private static final String PACKAGE = '/' + URIResolverTest.class.getPackage().getName().replace('.', '/') + "/";

  @Test
  public void testURIResolver() throws Exception {
    final String name = PACKAGE + "URIResolverTest";
    final TransformerFactory factory = TransformerFactory.newInstance();
    final TestURIResolver resolver = new TestURIResolver(name + ".xsl");
    factory.setURIResolver(resolver);
    final Templates templates = factory.newTemplates(getSource(name + ".xsl"));
    Assert.assertTrue(resolver.getResolvedURIs().contains("impincl/SystemIdImport.xsl"));
    Assert.assertTrue(resolver.getResolvedURIs().contains("impincl/SystemIdInclude.xsl"));
    resolver.reset();
    Assert.assertTrue(resolver.getResolvedURIs().isEmpty());
    final Transformer transformer = templates.newTransformer();
    Assert.assertEquals(TestURIResolver.class, transformer.getURIResolver().getClass());
    final Path outPath = Paths.get(System.getProperty("java.io.tmpdir"), "URIResolverTest.out");
    try {
      try (OutputStream outputStream = Files.newOutputStream(outPath)) {
        transformer.transform(getSource(name + ".xml"), new StreamResult(outputStream));
        final String expected = readResource(name + ".out", UTF_8);
        final byte[] encoded = Files.readAllBytes(outPath);
        final String generated = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(encoded)).toString();
        Assert.assertEquals(expected, generated);
      }
    } finally {
      Files.delete(outPath);
    }
    Assert.assertTrue(resolver.getResolvedURIs().contains("../impincl/SystemIdImport.xsl"));
    Assert.assertTrue(resolver.getResolvedURIs().contains("impincl/SystemIdImport.xsl"));
    Assert.assertTrue(resolver.getResolvedURIs().contains("systemid/impincl/SystemIdImport.xsl"));

  }

  private static class TestURIResolver implements URIResolver {

    private final String resource;
    private final Set<String> resolvedURIs = new HashSet<>();

    public TestURIResolver(String resource) {
      this.resource = resource;
    }

    public Set<String> getResolvedURIs() {
      return Collections.unmodifiableSet(resolvedURIs);
    }

    public void reset() {
      resolvedURIs.clear();
    }

    @Override
    public Source resolve(String href, String base) throws TransformerException {
      final String sysId = SystemIDResolver.getAbsoluteURI(href, base);
      if (base.endsWith(resource)) {
        resolvedURIs.add(href);
      }
      return new StreamSource(sysId);
    }
  }

}
