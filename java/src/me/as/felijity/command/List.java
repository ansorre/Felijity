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


import me.as.felijity.app.Constants;
import me.as.felijity.app.Project;

import static me.as.lib.core.log.LogEngine.logOut;


public class List extends AbstractCommand
{

 public String getName()
 {
  return "list";
 }

 public String getHelp()
 {
  return "lists the websites in this project.";
 }


 public String[] getArgumentsPattern()
 {
  return new String[]{"list"};
 }

 public String[] getArgumentsNames()
 {
  return null;
 }


 public void run()
 {
  try
  {
   Project project=owner.getProject();

   java.util.List<String> wss=project.getWebsitesProductionUrls();
   int len=wss.size();

   if (len>0)
   {
    logOut.println(""+len+" website"+(len>1?"s are":" is")+" present in this project:\n");

    wss.forEach(logOut::println);
   }
   else
    logOut.println("Still no website has been created in this project\nCreate a new website with '"+
     Constants.programName.toLowerCase()+" new website <website-production-url> [<website-development-url>]'\n");

  }
  catch (Throwable ignore)
  {

  }
 }



}
