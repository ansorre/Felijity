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


import me.as.lib.core.extra.BoxFor3;
import me.as.lib.core.lang.ArrayExtras;
import me.as.lib.core.system.FileSystemExtras;
import me.as.lib.format.json.JsonExtras;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.as.felijity.app.Constants.defaultProjectFileName;
import static me.as.felijity.app.Constants.defaultWebsiteFileName;
import static me.as.lib.core.lang.ArrayExtras.append;
import static me.as.lib.core.lang.ObjectExtras.assign;
import static me.as.lib.core.lang.StringExtras.isBlank;
import static me.as.lib.core.system.FileSystemExtras.*;
import static me.as.lib.format.json.JsonExtras.fromString;


public class Project
{

 private String deployRoot;
 private String sourcesRoot;

 public int version[];

 public String websites[];


 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

 transient Felijity owner;
 transient String projectFile;
 transient String fullDeployRoot;
 transient String fullSourcesRoot;
 transient final Map<String, Website> websiteInstances=new HashMap<>();


 public Project()
 {

 }

 public Project(Felijity owner)
 {
  this(owner, getCanonicalPath(mergePath(".", defaultProjectFileName)), false);
 }


 public Project(Felijity owner, String projectFile, boolean createIfNotExistent)
 {
  this.owner=owner;
  this.projectFile=projectFile;

  if (isFile(projectFile))
   load();
  else
  {
   if (createIfNotExistent)
    createNewProject();
   else
    owner.getProblems().addShowStopper("Project file '"+projectFile+"' does not exist!", true);
  }
 }



 private void grantDirectory(String fullPath, String dirType)
 {
  if (!isDirectory(fullPath))
  {
   mkdirs(fullPath);

   if (!isDirectory(fullPath))
   {
    owner.getProblems().addShowStopper("Cannot create "+dirType+" directory '"+fullPath+"'!", true);
   }
  }
 }



 private void createNewProject()
 {
  setupAsANewProject();
  save();

  grantDirectory(getFullDeployRoot(), "deployRoot");
  grantDirectory(getFullSourcesRoot(), "sourceRoot");
 }


 public Felijity getOwner()
 {
  return owner;
 }

 public String getDeployRoot()
 {
  return deployRoot;
 }

 public String getSourcesRoot()
 {
  return sourcesRoot;
 }


 public String _i_getFullRoot(String cached, String relative)
 {
  if (cached!=null)
   return cached;
  else
  {
   if (relative.startsWith("."))
   {
    String dir=FileSystemExtras.getDirAndFilename(projectFile)[0];
    return getCanonicalPath(mergePath(dir, relative));
   }
   else
    return relative;
  }
 }


 public String getFullDeployRoot()
 {
  return _i_getFullRoot(fullDeployRoot, deployRoot);
 }


 public String getFullSourcesRoot()
 {
  return _i_getFullRoot(fullSourcesRoot, sourcesRoot);
 }


 private void setupAsANewProject()
 {
  version=Constants.version;
  deployRoot="./deploy";
  sourcesRoot="./sources";
 }


 private void load()
 {
  try
  {
   setupAsANewProject();
   assign(this, fromString(Project.class, loadTextFromFile(projectFile)));
  }
  catch (Throwable tr)
  {
   owner.getProblems().addShowStopper(tr.getMessage(), true);
  }
 }


 private void save()
 {
  saveInFile(projectFile, JsonExtras.toString(this, true));
 }


 public List<String> getWebsitesProductionUrls()
 {
  return ArrayExtras.length(websites)>0 ? Arrays.asList(websites) : new ArrayList<>();
 }


 private String _i_getWebsiteFile(String production_url)
 {
  return mergePath(getFullSourcesRoot(), production_url, defaultWebsiteFileName);
 }


 public synchronized Website getWebsiteByProductionUrl(String production_url)
 {
  Website res=websiteInstances.get(production_url);

  if (res==null)
  {
   if (ArrayExtras.indexOf(websites, 0, production_url)>=0)
   {
    res=new Website(this, _i_getWebsiteFile(production_url));
    res.load();
    websiteInstances.put(production_url, res);
   }
   else
    owner.getProblems().addShowStopper("There is no website with production url '"+production_url+"'", true);
  }

  return res;
 }



 public BoxFor3<Website, String, String> createNewWebsite(String production_url)
 {
  return createNewWebsite(production_url, null);
 }


 public void removeWebsite(Website website)
 {
  websites=ArrayExtras.remove(websites, website.productionUrl);
  save();
 }



 public BoxFor3<Website, String, String> createNewWebsite(String production_url, String development_url)
 {
  BoxFor3<Website, String, String> res=new BoxFor3<>();

  if (ArrayExtras.indexOf(websites, 0, production_url)>=0)
  {
   owner.getProblems().addShowStopper("A website with production url '"+production_url+"' already exists in this project", true);
  }
  else
  {
   if (isBlank(development_url))
    development_url="dev-"+production_url;

   if (FileSystemExtras.getFileSystemCompatibleFileName(production_url).equals(production_url))
   {
    res.element1=new Website(this, _i_getWebsiteFile(production_url), production_url, development_url);

    websites=append(websites, production_url);

    save();

    res.element2=production_url;
    res.element3=development_url;
   }
   else
    owner.getProblems().addShowStopper("Invalid production_url '"+production_url+"' cannot create website with that url", true);
  }

  return res;
 }


 public boolean isProduction()
 {
  return false;
 }



}
