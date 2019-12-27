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

package me.as.felijity.server;


import io.undertow.server.HttpServerExchange;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import me.as.felijity.app.Felijity;
import me.as.felijity.app.Website;
import me.as.felijity.server.connector.Connector;
import me.as.felijity.server.connector.ConnectorHandler;
import me.as.felijity.server.connector.IPConfiguration;
import me.as.lib.cedilla.Cedilla;
import me.as.lib.core.concurrent.ThreadExtras;
import me.as.lib.core.lang.ResourceExtras;
import me.as.lib.core.lang.StringExtras;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.undertow.Handlers.websocket;
import static me.as.lib.core.lang.ExceptionExtras.getDeepCauseStackTrace;
import static me.as.lib.core.lang.StringExtras.quickMap;
import static me.as.lib.core.log.LogEngine.logOut;
import static me.as.lib.core.system.FileSystemExtras.isDirectory;
import static me.as.lib.core.system.FileSystemExtras.loadTextFromFile;
import static me.as.lib.core.system.FileSystemExtras.mergePath;


public class Server implements ConnectorHandler
{
 private Felijity owner;
 private final List<Website> websites=new ArrayList<>();

 private String websocketSecretURI;
 private WebSocketChannel channel;



 public Server(Felijity owner)
 {
  this.owner=owner;
 }


 public void addWebsite(Website web)
 {
  if (!websites.contains(web))
   websites.add(web);
 }

 public void addAllWebsites(List<Website> webs)
 {
  webs.forEach(this::addWebsite);
 }


 final Map<String, Website> websitesByHosPort=new HashMap<>();


 public void refreshBrowser()
 {
  try
  {
   WebSockets.sendText("refresh", channel, null);
  }
  catch (Throwable ignore)
  {
  }
 }

 public void serve()
 {
  websocketSecretURI="/"+StringExtras.generateRandomString(120);
  String autorefresherInjection="<script>\n"+ResourceExtras.loadPackagedText("/me/as/felijity/server/resource/autorefresher.js")+"\n</script>\n</body>";
  final List<IPConfiguration> allListens=new ArrayList<>();

  websites.forEach(
  web ->
  {
   web.build(true);
   web.getListens().forEach(
   ipc ->
   {
    String key=ipc.host+":"+ipc.port;
    Website wbhps=websitesByHosPort.get(key);
    if (wbhps!=null)
     throw new RuntimeException("Another website is already serving on '"+key+"'");

    String staticRoot=mergePath(web.getDeployRoot(), web.getDocumentRoot());

    int slept=0;
    while (slept<20 && !isDirectory(staticRoot)) // can be done better, for now I just wait for the build process to create that dir
    {
     ThreadExtras.sleep(250);
     slept++;
    }

    String injection=Cedilla.render(autorefresherInjection,
     quickMap("hostPortUri", ipc.host+(ipc.port!=80 ? ":"+ipc.port : "")+websocketSecretURI));

    web.setStaticContentHandler(new StaticContentHandler(staticRoot, injection));
    websitesByHosPort.put(key, web);
    allListens.add(new IPConfiguration("*", ipc.port));

    logOut.println("Starting to serve on '"+ipc.host+":"+ipc.port+"'...");
   });

   web.setTheServer(this);
  });

  try
  {
   Connector connector=new Connector();
   connector.setup(this, allListens);
   connector.addConnectorHandler(this);
   connector.start();
  }
  catch (Throwable tr)
  {
   System.err.println(getDeepCauseStackTrace(tr));
   throw tr;
  }
 }



 public boolean handleWebSocketForAutorefresh(Connector connector, HttpServerExchange exchange)
 {
  try
  {
   String rp=exchange.getRelativePath();

   if (websocketSecretURI.endsWith(rp))
   {
    websocket(
     (exchange1, channel) ->
     {
      channel.getReceiveSetter().set(new AbstractReceiveListener()
      {
       protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message)
       {
        Server.this.channel=channel;
       }
      });

      channel.resumeReceives();
     }).handleRequest(exchange);

    return false;
   }
  }
  catch (Throwable tr)
  {
   throw new RuntimeException(tr);
  }

  return true;
 }




 public boolean handleRequest(Connector connector, HttpServerExchange exchange)
 {
  boolean notHandled=true;

  String hostAndPort;

  try
  {
   hostAndPort=exchange.getHostAndPort();

   if (!hostAndPort.contains(":"))
    hostAndPort+=":80";
  }
  catch (Throwable tr)
  {
   System.err.println("Host is 'unknown' in request: "+exchange.getRequestURL());
   hostAndPort="unknown";
  }

  Website web=websitesByHosPort.get(hostAndPort);

  if (web!=null)
  {
   notHandled=handleWebSocketForAutorefresh(connector, exchange);

   if (notHandled)
   {
    notHandled=false;
    StaticContentHandler sch=web.getStaticContentHandler();

    if (sch.handleRequest(connector, exchange)) // not hanfled
    {
     String html;

     try
     {
      html=loadTextFromFile(mergePath(web.getDeployRoot(), web.getDocumentRoot(), "404.html"));
      html=sch.injectAutorefresher(html);
     }
     catch (Throwable tr)
     {
      html="404 - Not found error!";
     }

     connector.sendHTTPError(exchange, 404, "404 - Not found error", html);
    }
   }
  }

  return notHandled;
 }


}
