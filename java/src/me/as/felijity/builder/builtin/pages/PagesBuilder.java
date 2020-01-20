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

package me.as.felijity.builder.builtin.pages;


import me.as.felijity.app.Constants;
import me.as.felijity.app.Website;
import me.as.felijity.builder.AbstractBuilder;
import me.as.lib.core.extra.Box;
import me.as.lib.core.lang.StringExtras;
import me.as.lib.markdown.MarkdownExtras;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.as.lib.core.lang.ArrayExtras.toList;
import static me.as.lib.core.lang.ExceptionExtras.getDeepCauseStackTrace;
import static me.as.lib.core.lang.StringExtras.betterTrimNl;
import static me.as.lib.core.lang.StringExtras.isNotBlank;
import static me.as.lib.core.lang.StringExtras.purgeComments;
import static me.as.lib.core.lang.StringExtras.stringOrEmpty;
import static me.as.lib.core.log.LogEngine.logOut;
import static me.as.lib.core.system.FileSystemExtras.asUnixPath;
import static me.as.lib.core.system.FileSystemExtras.getDirAndFilenameAndExtension;
import static me.as.lib.core.system.FileSystemExtras.isFile;
import static me.as.lib.core.system.FileSystemExtras.lastModified;
import static me.as.lib.core.system.FileSystemExtras.loadTextFromFile;
import static me.as.lib.core.system.FileSystemExtras.mergePath;
import static me.as.lib.core.system.FileSystemExtras.saveInFile;
import static me.as.lib.core.system.FileSystemExtras.setLastModified;
import static me.as.lib.markdown.MarkdownExtras.toMapOfStrings;


public class PagesBuilder extends AbstractBuilder<PagesBuilderConfiguration>
{
 // YAML front matter keys
 public static final String yfmk_enabled              = "enabled";
 public static final String yfmk_pageTemplate         = "pageTemplate";
 public static final String yfmk_pageTitle            = "pageTitle";
 public static final String yfmk_languageId           = "languageId";
 public static final String yfmk_bodyClass            = "bodyClass";
 public static final String yfmk_canonicalUrl         = "canonicalUrl";
 public static final String yfmk_absoluteUrl          = "absoluteUrl";
 public static final String yfmk_pageDescription      = "pageDescription";
 public static final String yfmk_mainImage            = "mainImage";
 public static final String yfmk_mainImageDescription = "mainImageDescription";


 // END - YAML front matter keys

 // Other internal keys
 public static final String oik_body = "body";

 // END - Other internal keys



 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


 public PagesBuilderConfiguration getDefaultConfiguration()
 {
  PagesBuilderConfiguration res=new PagesBuilderConfiguration();

  res.targetDirectories=new String[]{"pages"};

  res.templateDirs=new String[]{"templates"};
  res.defaultTemplate="templates/pages/default.html";
  res.defaultPageTitle=Constants.programName+" static website page without title :(";
  res.defaultLanguageId="en";
  res.defaultBodyClass="center-mode small-footer";

  return res;
 }


 public List<String> getRootDirsToWatch()
 {
  List<String> res=super.getRootDirsToWatch();
  res.addAll(Arrays.asList(configuration.templateDirs));
  return res;
 }


 public void build()
 {
  final List<String> yetMadeFiles=new ArrayList<>();
  toList(configuration.targetDirectories).forEach(dir -> searchFiles(yetMadeFiles, dir, "*.md", f -> buildPage(dir, f)));
 }



 private Map<String, String> grantValue(Map<String, String> map, String key, String defaultValue)
 {
  if (map==null)
   map=new HashMap<>();

  map.putIfAbsent(key, defaultValue);

  return map;
 }


 protected Map<String, String> grantValues(Map<String, String> map)
 {
  map=grantValue(map, yfmk_enabled, "true");
  grantValue(map, yfmk_pageTemplate, configuration.defaultTemplate);
  grantValue(map, yfmk_pageTitle, configuration.defaultPageTitle);
  grantValue(map, yfmk_languageId, configuration.defaultLanguageId);
  grantValue(map, yfmk_bodyClass, configuration.defaultBodyClass);
  grantValue(map, yfmk_absoluteUrl, "");
  grantValue(map, yfmk_mainImage, "");
  grantValue(map, yfmk_mainImageDescription, "");
  grantValue(map, yfmk_pageDescription, "");

  return map;
 }


 protected String getDeployFilePath(String rootDir, String sourceFilePath, String absoluteUrl)
 {
  String res;
  Website web=manager.getWebsite();
  String webDir=web.getWebsiteRoot();
  int webDirSkip=StringExtras.length(mergePath(webDir, rootDir));
  String dne[]=getDirAndFilenameAndExtension(sourceFilePath);

  if (isNotBlank(absoluteUrl))
  {
   res=mergePath(web.getDeployRoot(), web.getDocumentRoot(), dne[0].substring(webDirSkip), absoluteUrl);
  }
  else
  {
   if ("index".equals(dne[1]))
    res=mergePath(web.getDeployRoot(), web.getDocumentRoot(), dne[0].substring(webDirSkip), "index.html");
   else
    res=mergePath(web.getDeployRoot(), web.getDocumentRoot(), dne[0].substring(webDirSkip), dne[1], "index.html");
  }

  return res;
 }


 private void buildPage(String rootDir, String file)
 {
  try
  {
   _i_buildPage(rootDir, file);
  }
  catch (Throwable tr)
  {
   System.err.println(getDeepCauseStackTrace(tr));
  }
 }


 private void _i_buildPage(String rootDir, String file)
 {
  Box<Map<String, String>> values=new Box<>();
  String code=loadTextFromFile(file);

  code=MarkdownExtras.parse(code, yamlFrontMatterBlock -> values.element=toMapOfStrings(yamlFrontMatterBlock));

  values.element=grantValues(values.element);

  boolean enabled=StringExtras.toBoolean(values.element.get(yfmk_enabled));
  if (!enabled)
   return;

  String absoluteUrl=values.element.get(yfmk_absoluteUrl);
  String output=getDeployFilePath(rootDir, file, absoluteUrl);
  Website web=manager.getWebsite();
  String canonicalUrl=web.getProductionServingRootUrl();

  if (isNotBlank(absoluteUrl))
   canonicalUrl+=absoluteUrl.substring(1);
  else
  {
   String remove=mergePath(web.getDeployRoot(), web.getDocumentRoot());
   String uri=output.substring(remove.length());

   if (output.endsWith("index.html"))
    uri=uri.substring(0, uri.length()-10);

   canonicalUrl+=asUnixPath(uri).substring(1);
  }

  values.element.put(yfmk_canonicalUrl, canonicalUrl);

  String templatePath=values.element.get(yfmk_pageTemplate);

  if (isNotBlank(code))
   code=manager.cedillaAdjust(file, code, values.element);

  String template;
  String templateFilePath;

  try
  {
   templateFilePath=mergePath(manager.getWebsite().getWebsiteRoot(), templatePath);
   if (isFile(templateFilePath))
    template=loadTextFromFile(templateFilePath);
   else
    throw new RuntimeException();
  }
  catch (Throwable tr)
  {
   manager.getProblems().addShowStopper("template '"+templatePath+"' does not exist or can't be read!", true);
   return;
  }

  values.element.put(oik_body, stringOrEmpty(code));
  template=manager.cedillaAdjust(templateFilePath, template, values.element);
  template=purgeComments(template, null, "<!---", "--->", null, null);
  template=betterTrimNl(template)+"\n";

  saveInFile(output, stringOrEmpty(template));
  setLastModified(output, lastModified(file));
 }


}
