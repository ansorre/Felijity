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

package me.as.felijity.app;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import me.as.felijity.builder.AbstractBuilder;
import me.as.felijity.builder.BuildManager;
import me.as.felijity.builder.BuilderConfiguration;
import me.as.felijity.server.Server;
import me.as.felijity.server.StaticContentHandler;
import me.as.felijity.server.connector.IPConfiguration;
import me.as.lib.core.extra.Box;
import me.as.lib.core.extra.BoxFor2;
import me.as.lib.core.io.util.MemBytesRoom;
import me.as.lib.core.lang.ArrayExtras;
import me.as.lib.core.lang.ClassExtras;
import me.as.lib.format.json.JsonExtras;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.as.lib.core.lang.ByteExtras.unZipInMemory;
import static me.as.lib.core.lang.ObjectExtras.assign;
import static me.as.lib.core.lang.ResourceExtras.loadPackagedBytesNE;
import static me.as.lib.core.system.FileSystemExtras.*;


public class Website
{
 public static final String defaultBuilders[]=new String[]{"styles", "assets", "pages", "articles"};

 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

 private WebsiteConfig common;
 private WebsiteConfig production;
 private WebsiteConfig development;


 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

 transient Project owner;
 transient String productionUrl;
 transient String websiteFile;
 transient String websiteRoot;
 transient String deployRoot;
 transient StaticContentHandler staticContentHandler;
 transient Server server;


 Website(Project owner, String websiteFile)
 {
  this.owner=owner;
  this.websiteFile=websiteFile;
  websiteRoot=getDirAndFilename(websiteFile)[0];
 }


 Website(Project owner, String websiteFile, String production_url, String development_url)
 {
  this(owner, websiteFile);
  this.productionUrl=production_url;

  if (isFile(websiteFile))
   owner.owner.getProblems().addShowStopper("Website file '"+websiteFile+"' already exists, cannot create new!", true);
  else
   createNewWebsite(production_url, development_url);

  setupTheRest();
 }


 public Project getOwner()
 {
  return owner;
 }


 private void setupAsANewWebsite()
 {
  common=new WebsiteConfig();
  production=new WebsiteConfig();
  development=new WebsiteConfig();

  common.documentRoot="-www";
 }

 private void setupTheRest()
 {
  deployRoot=mergePath(owner.getFullDeployRoot(), productionUrl);
 }


 private void createNewWebsite(String production_url, String development_url)
 {
  setupAsANewWebsite();

  production.listens=new IPConfiguration[]{new IPConfiguration(production_url, 80, "https")};
  development.listens=new IPConfiguration[]{new IPConfiguration(development_url, 80, "http")};

  common.productionServingRootUrl="https://"+production_url+"/";

  mkdirs(mergePath(websiteRoot, common.documentRoot));

  installDefaultBuilders();

  MemBytesRoom zipBytes=loadPackagedBytesNE("/me/as/felijity/resource/felijity-template-website.zip");
  HashMap<String, byte[]> files=unZipInMemory(zipBytes.getContent());

  for (String fname : files.keySet())
  {
   String wholeFile=mergePath(websiteRoot, fname);
   saveInFile(wholeFile, files.get(fname));
  }

  save();
 }


 private void installDefaultBuilders()
 {
  int t, len=ArrayExtras.length(defaultBuilders);
  common.builders=new BuilderConfiguration[len];

  for (t=0;t<len;t++)
  {
   String name=defaultBuilders[t];
   String className=
    "me.as.felijity.builder.builtin."+name+"."+
     Character.toUpperCase(name.charAt(0))+
     name.substring(1)+"Builder";

   AbstractBuilder builder=ClassExtras.newInstanceByClassName(className);
   common.builders[t]=builder.getDefaultConfiguration();

   List<String> dirs=common.builders[t].getInvolvedDirectories();
   int d, dLen=ArrayExtras.length(dirs);

   for (d=0;d<dLen;d++)
    mkdirs(mergePath(websiteRoot, dirs.get(d)));

  }
 }


 void load()
 {
  try
  {
   setupAsANewWebsite();
   productionUrl=getDirAndFilename(getDirAndFilename(websiteFile)[0])[1];
   setupTheRest();

   Box<Gson> igs=new Box<>();

   JsonDeserializer deserializer=
   (jsonElement, type, jsonDeserializationContext) ->
   {
    String cfgClassName=jsonElement.getAsJsonObject().get("Class").getAsString()+"Configuration";
    return igs.element.fromJson(jsonElement, ClassExtras.classFromName_NE(cfgClassName));
   };

   Gson gson=igs.element=new GsonBuilder().
    registerTypeAdapter(BuilderConfiguration.class, deserializer).
    create();

   Website tmpW=gson.fromJson(loadTextFromFile(websiteFile), Website.class);
   assign(this, tmpW);
   assign(common, tmpW.common);
   assign(production, tmpW.production);
   assign(development, tmpW.development);
  }
  catch (Throwable tr)
  {
   owner.owner.getProblems().addShowStopper(tr.getMessage(), true);
  }
 }


 void save()
 {
  saveInFile(websiteFile, JsonExtras.toString(this, true));
 }


 private BoxFor2<WebsiteConfig, WebsiteConfig> _i_getConfigs_cache=null;
 private BoxFor2<WebsiteConfig, WebsiteConfig> getConfigs()
 {
  if (_i_getConfigs_cache==null)
  {
   _i_getConfigs_cache=new BoxFor2<>();

   if (owner.isProduction())
    _i_getConfigs_cache.set(production, common);
   else
    _i_getConfigs_cache.set(development, common);
  }

  return _i_getConfigs_cache;
 }



 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


 public String getDeployRoot()
 {
  return deployRoot;
 }

 public String getDocumentRoot()
 {
  BoxFor2<WebsiteConfig, WebsiteConfig> c=getConfigs();
  if (c.element1.documentRoot!=null)
   return c.element1.documentRoot;
  else
   return c.element2.documentRoot;
 }


 public String getWebsiteRoot()
 {
  return websiteRoot;
 }

 public String getProductionUrl()
 {
  return productionUrl;
 }

 public String getProductionServingRootUrl()
 {
  return common.productionServingRootUrl;
 }


 public List<IPConfiguration> getListens()
 {
  List<IPConfiguration> res=new ArrayList<>();
  BoxFor2<WebsiteConfig, WebsiteConfig> c=getConfigs();
  IPConfiguration listens[];

  for (int t=0;t<2;t++)
  {
   listens=(t==0 ? c.element1.listens : c.element2.listens);

   if (ArrayExtras.length(listens)>0)
    res.addAll(Arrays.asList(listens));
  }

  return res;
 }


 public Map<String, String> getCustomMap()
 {
  Map<String, String> customMap;
  BoxFor2<WebsiteConfig, WebsiteConfig> c=getConfigs();

  for (int t=0;t<2;t++)
  {
   customMap=(t==0 ? c.element1.customMap : c.element2.customMap);

   if (ArrayExtras.length(customMap)>0)
    return customMap;
  }

  return null;
 }



 public StaticContentHandler getStaticContentHandler()
 {
  return staticContentHandler;
 }

 public void setStaticContentHandler(StaticContentHandler staticContentHandler)
 {
  this.staticContentHandler=staticContentHandler;
 }


 public void sourcesChanged()
 {
  if (server!=null)
   server.refreshBrowser();
 }


 public void setTheServer(Server server)
 {
  this.server=server;
 }


 public void build()
 {
  build(false);
 }

 public void build(boolean watch)
 {
  List<BuilderConfiguration> buildersCfgs=new ArrayList<>();
  BoxFor2<WebsiteConfig, WebsiteConfig> c=getConfigs();
  BuilderConfiguration builders[];

  for (int t=0;t<2;t++)
  {
   builders=(t==0 ? c.element1.builders : c.element2.builders);

   if (ArrayExtras.length(builders)>0)
    buildersCfgs.addAll(Arrays.asList(builders));
  }

  new BuildManager().setup(this, buildersCfgs, watch).build();
 }


}
