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
import io.undertow.server.handlers.resource.CachingResourceManager;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.server.handlers.resource.Resource;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.server.handlers.resource.ResourceManager;
import io.undertow.util.DateUtils;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import io.undertow.util.MimeMappings;
import me.as.felijity.server.connector.Connector;
import me.as.felijity.server.connector.ConnectorHandler;
import me.as.lib.core.lang.CalendarExtras;
import me.as.lib.core.system.FileSystemExtras;


import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static me.as.lib.core.lang.StringExtras.isNotBlank;
import static me.as.lib.core.lang.StringExtras.replace;
import static me.as.lib.core.system.FileSystemExtras.lastModified;


public class StaticContentHandler implements ConnectorHandler
{
 public static final int defaultMaxAge=(int)(CalendarExtras.oneYear/1000);

 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

 protected ResourceHandler resourceHandler;
 protected ResourceManager resourceManager;
 private List<String> welcomeFileList;
 private String autorefresherInjection;



 public StaticContentHandler(String staticRoot, String autorefresherInjection)
 {
  this.autorefresherInjection=autorefresherInjection;

  //noinspection PointlessArithmeticExpression
  resourceManager=
   new CachingResourceManager(1000, 1L, null,
    new FileResourceManager(new File(staticRoot), 1*1024*1024), 250);

  MimeMappings.Builder mmBuilder=MimeMappings.builder(true)
   .addMapping("html", "text/html; charset=utf-8")
   .addMapping("css", "text/css; charset=utf-8")
   .addMapping("js", "application/javascript; charset=utf-8")
   .addMapping("json", "application/json; charset=utf-8");

  String[] welcomeFiles=new String[]{"index.html", "index.htm", "default.html", "default.htm"};
  welcomeFileList=new CopyOnWriteArrayList<>(welcomeFiles);

  resourceHandler=new ResourceHandler(resourceManager)
   .setWelcomeFiles(welcomeFiles)
   .setDirectoryListingEnabled(false)
   .setMimeMappings(mmBuilder.build());
 }


 protected Resource getIndexFiles(ResourceManager resourceManager, final String base, List<String> possible) throws IOException
 {
  String realBase;

  if (base.endsWith("/"))
  {
   realBase=base;
  }
  else
  {
   realBase=base+"/";
  }

  for (String possibility : possible)
  {
   Resource index=resourceManager.getResource(realBase+possibility);
   if (index!=null)
   {
    return index;
   }
  }

  return null;
 }


 public String injectAutorefresher(String html)
 {
  return replace(html, "</body>", autorefresherInjection, true);
 }


 public boolean handleRequest(Connector connector, HttpServerExchange exchange)
 {
  boolean res=true;
  HttpString method=exchange.getRequestMethod();

  switch (method.toString().toUpperCase())
  {
   case "GET":
   {
    try
    {
     long lastModified;
     boolean noCachePlease=false;
     boolean isHtml=false;
     Resource resource;

     try
     {
      String rp=exchange.getRelativePath();
      resource=resourceManager.getResource(rp);

      if (resource==null)
      {
       return true;
      }

      if (resource.isDirectory())
      {
       Resource indexResource=getIndexFiles(resourceManager, resource.getPath(), welcomeFileList);

       if (indexResource==null)
        return true;
       else
        resource=indexResource;
      }

      lastModified=lastModified(resource.getFile().getAbsolutePath());
     }
     catch (IOException ex)
     {
      return true;
     }

     setDefaultStaticContentResponseHeaders(exchange, lastModified);

     isHtml=resource.getName().endsWith(".html");

     if (isHtml || resource.getName().endsWith(".css"))
      setNotCachableResponse(exchange);

     if (isHtml)
     {
      String html=FileSystemExtras.loadTextFromFile(resource.getFile().getCanonicalPath());
      html=injectAutorefresher(html);

      exchange.getResponseHeaders().add(Headers.CONTENT_TYPE, "text/html");
      exchange.getResponseSender().send(html);
     }
     else
      resourceHandler.handleRequest(exchange);

     res=false;
    }
    catch (Throwable tr)
    {
     throw new RuntimeException(tr);
    }

   } break;
  }

  return res;
 }


 public static void setNotCachableResponse(HttpServerExchange exchange)
 {
  HeaderMap responseHeaders=exchange.getResponseHeaders();

  responseHeaders.put(Headers.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
  responseHeaders.put(Headers.PRAGMA, "no-cache");
  responseHeaders.put(Headers.EXPIRES, "0");
 }

 public static void setDefaultStaticContentResponseHeaders(HttpServerExchange exchange, long lastModified)
 {
  setDefaultStaticContentResponseHeaders(exchange, lastModified, null, null, null);
 }


 public static void setDefaultStaticContentResponseHeaders(HttpServerExchange exchange, long lastModified, String contentType, Long contentLength, String eTag)
 {
  setDefaultStaticContentResponseHeaders(exchange, lastModified, contentType, contentLength, eTag, true);
 }


 public static void setDefaultStaticContentResponseHeaders(HttpServerExchange exchange, long lastModified, String contentType, Long contentLength, String eTag, boolean noCache)
 {
  try
  {
   HeaderMap responseHeaders=exchange.getResponseHeaders();

   String nc=((noCache)?"no-cache, max-age=":"max-age=");
   responseHeaders.put(Headers.CACHE_CONTROL, nc+defaultMaxAge);

   Calendar cal=CalendarExtras.newInstance(lastModified);
   cal.add(Calendar.SECOND, defaultMaxAge);
   String expires=DateUtils.toDateString(cal.getTime());
   responseHeaders.put(Headers.EXPIRES, expires);

   if (isNotBlank(eTag))
   {
    responseHeaders.put(Headers.ETAG, eTag);
   }

   if (isNotBlank(contentType))
   {
    responseHeaders.put(Headers.CONTENT_TYPE, contentType);
   }

   if (contentLength!=null)
   {
    responseHeaders.put(Headers.CONTENT_LENGTH, (long)contentLength);
   }
  }
  catch (Throwable tr)
  {
   throw new RuntimeException(tr);
  }
 }


}
