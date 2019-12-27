/*
 * Copyright 2019 Antonio Sorrentini
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package me.as.felijity.server.connector;


import io.undertow.server.Connectors;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.BlockingHandler;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;


/**
 * A non blocking handler to add a time delay before the next handler
 * is executed. If the exchange has already been dispatched this will
 * un-dispatch the exchange and re-dispatch it before next is called.
 */
public class DelayedExecutionHandler implements HttpHandler
{

 private final HttpHandler next;
 private final Function<HttpServerExchange, Duration> durationFunc;

 public DelayedExecutionHandler(HttpHandler next, Function<HttpServerExchange, Duration> durationFunc)
 {
  this.next=next;
  this.durationFunc=durationFunc;
 }


 public void handleRequest(HttpServerExchange exchange) throws Exception
 {
  Duration duration=durationFunc.apply(exchange);

  final HttpHandler delegate;
  if (exchange.isBlocking())
  {
   // We want to undispatch here so that we are not blocking
   // a worker thread. We will spin on the IO thread using the
   // built in executeAfter.
   exchange.unDispatch();
   delegate=new BlockingHandler(next);
  }
  else
  {
   delegate=next;
  }

  exchange.dispatch(exchange.getIoThread(),
   () -> exchange.getIoThread().executeAfter(
    () -> Connectors.executeRootHandler(delegate, exchange), duration.toMillis(), TimeUnit.MILLISECONDS));
 }
}