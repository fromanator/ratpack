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

package org.ratpackframework.guice.internal;

import com.google.inject.Injector;
import org.ratpackframework.handling.Context;
import org.ratpackframework.handling.Handler;

public class InjectingHandler implements Handler {

  private final Class<? extends Handler> handlerType;

  public InjectingHandler(final Class<? extends Handler> handlerType) {
    this.handlerType = handlerType;
  }

  public void handle(Context context) {
    Injector injector = context.get(Injector.class);
    Handler instance = injector.getInstance(handlerType);
    instance.handle(context);
  }

}
