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

package me.as.felijity.command;


import me.as.felijity.app.Felijity;
import me.as.felijity.app.Project;
import me.as.felijity.app.Website;
import me.as.lib.core.lang.ArrayExtras;

import java.util.ArrayList;
import java.util.List;


public abstract class AbstractCommand implements Command
{
 public static final String undoc="<undocumented> (NOTE: you can contribute to Felijity project on https://github.com/ansorre/Felijity)";

 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

 protected Felijity owner;


 public <ME extends Command> ME setup(Felijity owner)
 {
  this.owner=owner;
  return (ME)this;
 }

 public String getHelp()
 {
  return undoc;
 }

 public String getDetailedHelp()
 {
  return undoc;
 }


 protected java.util.List<Website> getWebsitesFromCLI()
 {
  final Project project=owner.getProject();
  java.util.List<String> websiteUrls;
  java.util.List<String> cAas=owner.getCommandsAndArguments();
  int t, len=ArrayExtras.length(cAas);

  if (len==1)
   websiteUrls=project.getWebsitesProductionUrls();
  else
   websiteUrls=cAas.subList(1, cAas.size());

  if (ArrayExtras.length(websiteUrls)==0)
  {
   owner.getProblems().addShowStopper("no websites are present in the project");
  }
  else
  {
   List<Website> webs=new ArrayList<>();
   websiteUrls.forEach(wsu -> {try { webs.add(project.getWebsiteByProductionUrl(wsu)); } catch (Throwable ignore) {}});

   if (webs.size()==websiteUrls.size())
   {
    return webs;
   } // else there has been an error
  }

  return null;
 }



}
