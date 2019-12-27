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


import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.predicate.Predicates;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.encoding.ContentEncodingRepository;
import io.undertow.server.handlers.encoding.DeflateEncodingProvider;
import io.undertow.server.handlers.encoding.EncodingHandler;
import io.undertow.server.handlers.encoding.GzipEncodingProvider;
import io.undertow.util.Headers;
import me.as.felijity.server.Server;
import me.as.lib.core.lang.ArrayExtras;

import javax.net.ssl.SSLContext;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import static me.as.lib.core.lang.ExceptionExtras.getDeepCauseStackTrace;
import static me.as.lib.core.lang.ExceptionExtras.systemErrDeepCauseStackTrace;


public class Connector implements HttpHandler
{
 private Undertow undertow;
 private ArrayList<ConnectorHandler> connectorHandlers=new ArrayList<>();
 private Boolean developmentInstance=null;
 private Server owner;
 private List<IPConfiguration> listens;


 public void setup(Server owner, List<IPConfiguration> listens)
 {
  this.owner=owner;
  this.listens=listens;
 }


 private int addListener(Undertow.Builder uBuilder, int port, String host, List<String> yetAdded)
 {
  String key=""+port+"|"+host;

  if (!yetAdded.contains(key))
  {
   uBuilder.addHttpListener(port, host);
   yetAdded.add(key);
   return 1;
  }

  return 0;
 }


 public void start()
 {
  SSLContext sslContext=null;
/*
  String serverInstance=getGlobalSetting(Globals.instance);

  if (serverInstance.equals(Globals.development))
  {
   sslContext=getTest_sslContext();
  }
*/

  // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

  int listeningCount=0;

  List<String> yetAddedListeners=new ArrayList<>();
  Undertow.Builder uBuilder=Undertow.builder();
  int t, len=ArrayExtras.length(listens);

  for (t=0;t<len;t++)
  {
   IPConfiguration ipc=listens.get(t);

   if ("*".equals(ipc.host))
   {
    try
    {
     Enumeration<NetworkInterface> nets=NetworkInterface.getNetworkInterfaces();

     for (NetworkInterface netint : Collections.list(nets))
     {
      Enumeration<InetAddress> inetAddresses=netint.getInetAddresses();

      for (InetAddress inetAddress : Collections.list(inetAddresses))
      {
       listeningCount+=addListener(uBuilder, ipc.port, inetAddress.getHostAddress(), yetAddedListeners);

/*
       if (sslContext!=null)
       {
        // ssl hackery in development mode just to test facebook api grrrrrrrrrrrrrrrrrrrrrrrrrrrr

        uBuilder.addH_adjust!_ttpsListener(ipc.port+363, inetAddress.getHostAddress(), sslContext);
        listeningCount++;
       }
*/
      }
     }
    }
    catch (Throwable tr)
    {
     System.out.println("WARNING: "+getDeepCauseStackTrace(tr));
    }
   }
   else
   {
    listeningCount+=addListener(uBuilder, ipc.port, ipc.host, yetAddedListeners);
   }
  }

  if (listeningCount==0)
  {
   throw new RuntimeException("ERROR: this connector does not listen to any host:port!");
  }


  // with compression
  EncodingHandler compressingHandler=new EncodingHandler(new ContentEncodingRepository()
   .addEncodingHandler("gzip", new GzipEncodingProvider(), 50, Predicates.maxContentSize(1024))
   .addEncodingHandler("deflate", new DeflateEncodingProvider(), 50, Predicates.maxContentSize(1024)))
   .setNext(this);
  HttpHandler handler=compressingHandler;
  // END - with compression

  // without compression
//  HttpHandler handler=this;
  // END - without compression

  uBuilder.setServerOption(UndertowOptions.URL_CHARSET, "UTF8");
//  uBuilder.setServerOption(UndertowOptions.ENABLE_HTTP2, true);  what is ALPN ???
  undertow=uBuilder.setHandler(handler).build();
  undertow.start();


  /*

  EncodingHandler handler = new EncodingHandler(new ContentEncodingRepository()
  .addEncodingHandler("gzip", new GzipEncodingProvider(), 100, Predicates.maxContentSize(5)))
  .addEncodingHandler("deflate", new DeflateEncodingProvider(), 50, Predicates.maxContentSize(5)))
  .setNext(new HttpHandler() {
    @Override
    public void handleRequest(final HttpServerExchange exchange) throws Exception {
      // whatever the request is, say "Hello World"
      exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
      exchange.getResponseSender().send("Hello World");
    }
  });
  server.setHandler(handler);

   */

 }


 public void stop()
 {

  undertow.stop();

 }


 public void handleRequest(HttpServerExchange exchange)
 {
  try
  {
   _i_handleRequest(exchange);
  }
  catch (Throwable tr)
  {
   systemErrDeepCauseStackTrace(tr);
   throw new RuntimeException(tr);
  }
 }


 private void _i_handleRequest(HttpServerExchange exchange) throws Exception
 {
//  debugRequestHeaders(exchange);
  boolean runNotHandled=true;

  for (ConnectorHandler ch : connectorHandlers)
  {
   if (!ch.handleRequest(this, exchange))
   {
    runNotHandled=false;
    break;
   }
  }

  if (runNotHandled)
  {
   sendHTTPError(exchange, 403, "403 - Forbidden", "Uh!?!");
  }
 }


 public void sendHTTPError(HttpServerExchange exchange, int httpErrorCode, String errorText, String content)
 {
  try
  {
   exchange.setStatusCode(httpErrorCode);
  }
  catch (Throwable tr)
  {
   throw new RuntimeException(tr);
  }

  exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html");
  exchange.getResponseSender().send(content);
 }


 public void addConnectorHandler(ConnectorHandler handler)
 {
  connectorHandlers.add(handler);
 }

 public void removeConnectorHandler(ConnectorHandler handler)
 {
  connectorHandlers.remove(handler);
 }



}
