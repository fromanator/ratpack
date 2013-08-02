/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ratpackframework.groovy.templating.internal;

import com.google.common.collect.ImmutableList;
import org.ratpackframework.file.FileSystemBinding;
import org.ratpackframework.groovy.templating.TemplateRenderer;
import org.ratpackframework.handling.Context;
import org.ratpackframework.handling.Handler;
import org.ratpackframework.handling.internal.ClientErrorHandler;
import org.ratpackframework.registry.Registry;
import org.ratpackframework.registry.internal.LazyHierarchicalRegistry;
import org.ratpackframework.util.internal.Factory;

import java.io.File;

import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;

public class TemplateRendererBindingHandler implements Handler {

  private final String templateDir;
  private final ImmutableList<Handler> delegate;

  public TemplateRendererBindingHandler(String templateDir, Handler delegate) {
    this.templateDir = templateDir;
    this.delegate = ImmutableList.of(delegate, new ClientErrorHandler(NOT_FOUND.code()));
  }

  public void handle(final Context context) {
    Registry registry = new LazyHierarchicalRegistry(context, TemplateRenderer.class, new Factory<TemplateRenderer>() {
      public TemplateRenderer create() {
        GroovyTemplateRenderingEngine engine = context.get(GroovyTemplateRenderingEngine.class);
        File templateDirFile = context.get(FileSystemBinding.class).file(templateDir);
        return new DefaultTemplateRenderer(templateDirFile, context, engine);
      }
    });
    context.insert(registry, delegate);
  }
}
