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
import me.as.lib.core.StillUnimplemented;
import me.as.lib.core.lang.ClassExtras;
import me.as.lib.core.lang.StringExtras;

import java.util.List;
import java.util.function.Consumer;

import static me.as.lib.core.system.FileSystemExtras.getDirAndFilename;
import static me.as.lib.core.system.FileSystemExtras.listTheTree;
import static me.as.lib.core.system.FileSystemExtras.mergePath;


public abstract class AbstractBuilder<C extends BuilderConfiguration> implements Builder<C>
{
 protected C configuration;
 protected BuildManager manager;


 public void setup(BuildManager manager, C configuration)
 {
  this.manager=manager;
  this.configuration=configuration;
 }

 public C getConfiguration()
 {
  return configuration;
 }

 public C getDefaultConfiguration()
 {
  throw new StillUnimplemented();
 }


 public List<String> getRootDirsToWatch()
 {
  return configuration.getInvolvedDirectories();
 }



 protected void searchFiles(List<String> yetMadeFiles, String dir, String fileNameGlobPattern, Consumer<String> worker)
 {
  Website web=manager.getWebsite();
  final String webDir=web.getWebsiteRoot();
  final int webDirSkip=StringExtras.length(webDir)+1;
  String buildRoot=mergePath(webDir, dir);

  List<String> files=listTheTree(buildRoot);

  files.forEach(
  file ->
  {
   String wholeF=mergePath(buildRoot, file);
   String nameF=getDirAndFilename(wholeF)[1];

   if (StringExtras.doTheyMatch(nameF, fileNameGlobPattern))
   {
    String fName=wholeF.substring(webDirSkip);

    if (!yetMadeFiles.contains(fName))
    {
     worker.accept(wholeF);
     yetMadeFiles.add(fName);
    }
   }
  });
 }





 public void build()
 {
  StillUnimplemented.warning(ClassExtras.getVerySpeedType(getClass())+".build()");
 }




}
