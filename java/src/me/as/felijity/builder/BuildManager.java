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

package me.as.felijity.builder;


import me.as.felijity.app.Website;
import me.as.lib.buildtools.Watcher;
import me.as.lib.cedilla.Cedilla;
import me.as.lib.cedilla.CedillaHelper;
import me.as.lib.cedilla.Configuration;
import me.as.lib.cedilla.ValuesProvider;
import me.as.lib.core.concurrent.ThreadExtras;
import me.as.lib.core.lang.ArrayExtras;
import me.as.lib.core.lang.ExceptionExtras;
import me.as.lib.core.report.Problems;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.Consumer;

import static me.as.lib.core.lang.ArrayExtras.toArrayOfStrings;
import static me.as.lib.core.lang.ArrayExtras.toList;
import static me.as.lib.core.lang.StringExtras.isNotBlank;
import static me.as.lib.core.log.LogEngine.logErr;
import static me.as.lib.core.log.LogEngine.logOut;
import static me.as.lib.core.system.FileSystemExtras.*;


public class BuildManager implements ValuesProvider, CedillaHelper
{

 public static final String ck_deployRoot   = "deployRoot";
 public static final String ck_documentRoot = "documentRoot";

 private static final String cedillaKeys[]=new String[]{ck_deployRoot, ck_documentRoot};

 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

 private Website website;
 private Map<String, String> customMap;

 private boolean watch;
 private List<BuilderConfiguration> buildersCfgs;
 private List<Builder> builders;

 private String cv_deployRoot;
 private String cv_documentRoot;

 private final Stack<Map<String, String>> moreValues=new Stack<>();
 private final Stack<String> currentCedillaFile=new Stack<>();
 private String cachedKeys[];

 Watcher watcher=null;


 public BuildManager()
 {

 }


 public BuildManager setup(Website website, List<BuilderConfiguration> buildersCfgs, boolean watch)
 {
  this.website=website;
  this.watch=watch;
  this.buildersCfgs=buildersCfgs;
  if (watch) watcher=new Watcher();

  customMap=website.getCustomMap();
  if (ArrayExtras.length(customMap)==0) customMap=null;

  cv_deployRoot=website.getDeployRoot();
  cv_documentRoot=website.getDocumentRoot();

  return this;
 }


 public String getSourceFor(String path)
 {
  if (path.startsWith("/"))
   return loadTextFromFile(getCanonicalPath(mergePath(website.getWebsiteRoot(), path)));
  else
   return loadTextFromFile(getCanonicalPath(mergePath(getDirAndFilename(currentCedillaFile.peek())[0], path)));
 }


 public String[] getKeys()
 {
  int t, len=ArrayExtras.length(moreValues);

  if (len>0)
  {
   if (cachedKeys==null)
   {
    List<String> allKeys=new ArrayList<>();
    Consumer<String> adder=
    k ->
    {
     if (!allKeys.contains(k))
      allKeys.add(k);
    };

    moreValues.forEach(m -> m.keySet().forEach(adder));
    toList(cedillaKeys).forEach(adder);

    cachedKeys=toArrayOfStrings(allKeys);
   }

   return cachedKeys;
  }

  return cedillaKeys;
 }


 public Object getValueFor(String key)
 {
  Object res;
  int t, len=ArrayExtras.length(moreValues);

  for (t=len-1;t>=0;t--)
  {
   res=moreValues.get(t).get(key);
   if (res!=null) return res;
  }

  switch (key)
  {
   case ck_deployRoot:return cv_deployRoot;
   case ck_documentRoot:return cv_documentRoot;
   default:getProblems().addShowStopper("Variable named '"+key+"' is unknown!", true);
  }

  return null;
 }



 private void startWatching(Builder builder)
 {
  String sourcesRoot=website.getWebsiteRoot();
  List<String> dirs=builder.getRootDirsToWatch();
  int t, len=ArrayExtras.length(dirs);

  for (t=0;t<len;t++)
  {
   String root=mergePath(sourcesRoot, dirs.get(t));
   watcher.addToWatching(root);

   listTheTree(root,
   b3 ->
   {
    if (isDirectory(b3.element1))
     watcher.addToWatching(b3.element1);
   });
  }
 }



 private void showBuilding()
 {
  logOut.println("Building '"+website.getProductionUrl()+"'...");
 }


 public void build()
 {
  synchronized (this)
  {
   if (builders==null)
   {
    builders=new ArrayList<>();

    buildersCfgs.forEach(
     bc ->
     {
      try
      {
       Builder builder=bc.newBuilder();
       builder.setup(BuildManager.this, bc);
       builders.add(builder);
      }
      catch (Throwable tr)
      {
       logErr.println(ExceptionExtras.getDeepCauseStackTrace(tr));
      }
     });
   }
  }

  if (watcher==null)
  {
   showBuilding();
   builders.forEach(Builder::build);
  }
  else
  {
   ThreadExtras.executeOnAnotherThread(()->
   {
    int rounds=0;

    do
    {
     showBuilding();

     builders.forEach(
     builder ->
     {
      startWatching(builder);
      builder.build();
     });

     if (rounds>0)
      website.sourcesChanged();

     watcher.waitChanges();
     rounds++;
    } while (true);
   });
  }
 }


 public Website getWebsite()
 {
  return website;
 }


 public Problems getProblems()
 {
  return getWebsite().getOwner().getOwner().getProblems();
 }



 private void pushMoreValues(Map<String, String> additionalValues)
 {
  moreValues.push(additionalValues);
  cachedKeys=null;
 }

 private void popMoreValues()
 {
  moreValues.pop();
  cachedKeys=null;
 }


 public String cedillaAdjust(String text)
 {
  return cedillaAdjust(null, text, null);
 }


 public String cedillaAdjust(String sourcefilePath, String text, Map<String, String> additionalValues)
 {
  boolean hasFile=isNotBlank(sourcefilePath);
  boolean hasValues=(ArrayExtras.length(additionalValues)>0);

  if (customMap!=null && moreValues.size()==0)
   pushMoreValues(customMap);

  if (hasValues) pushMoreValues(additionalValues);
  if (hasFile) currentCedillaFile.push(sourcefilePath);

  try
  {
   return Cedilla.render(Configuration.defaultConfiguration, text, this, this);
  }
  catch (Throwable tr)
  {
   getProblems().addShowStopper(tr, true);
  }
  finally
  {
   if (hasValues) popMoreValues();
   if (hasFile) currentCedillaFile.pop();

   if (customMap!=null && moreValues.size()==1)
    popMoreValues();
  }

  return null;
 }


}
