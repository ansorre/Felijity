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

package me.as.felijity.builder.builtin.assets;


import me.as.felijity.app.Website;
import me.as.felijity.builder.AbstractBuilder;
import me.as.lib.core.lang.StringExtras;

import java.util.ArrayList;
import java.util.List;

import static me.as.lib.core.lang.ArrayExtras.toList;
import static me.as.lib.core.lang.ExceptionExtras.getDeepCauseStackTrace;
import static me.as.lib.core.system.FileSystemExtras.*;


public class AssetsBuilder extends AbstractBuilder<AssetsBuilderConfiguration>
{


 public AssetsBuilderConfiguration getDefaultConfiguration()
 {
  AssetsBuilderConfiguration res=new AssetsBuilderConfiguration();
  res.deployDestination="-www";
  res.targetDirectories=new String[]{res.deployDestination};

  return res;
 }


 public void build()
 {
  final List<String> yetMadeFiles=new ArrayList<>();
  toList(configuration.targetDirectories).forEach(dir -> searchFiles(yetMadeFiles, dir, "*", f -> synchFile(dir, f)));
 }



 private void synchFile(String rootDir, String sourceFilePath)
 {
  try
  {
   _i_synchFile(rootDir, sourceFilePath);
  }
  catch (Throwable tr)
  {
   System.err.println(getDeepCauseStackTrace(tr));
  }
 }



 private void _i_synchFile(String rootDir, String sourceFilePath)
 {
  String res;
  Website web=manager.getWebsite();
  String webDir=web.getWebsiteRoot();
  int webDirSkip=StringExtras.length(mergePath(webDir, rootDir));
  String dne[]=getDirAndFilename(sourceFilePath);
  String dest=mergePath(web.getDeployRoot(), configuration.deployDestination, dne[0].substring(webDirSkip), dne[1]);

  copyIfDifferent(sourceFilePath, dest);
//  logOut.println(sourceFilePath+"\n"+dest+"\n\n");
 }


}
