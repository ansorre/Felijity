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


import me.as.felijity.app.Project;
import me.as.felijity.app.Website;
import me.as.lib.core.extra.BoxFor3;

import java.util.List;

import static me.as.felijity.command.CommandsCentral.oneOrTwo;
import static me.as.lib.core.log.LogEngine.logOut;


public class NewWebsite extends AbstractCommand
{

 public String getName()
 {
  return "new";
 }

 public String getHelp()
 {
  return "creates a new website that will be served on specified urls in production and in development.";
 }


 public String[] getArgumentsPattern()
 {
  return new String[]{"new", "website", oneOrTwo};
 }

 public String[] getArgumentsNames()
 {
  return new String[]{"website-production-url", "website-development-url"};
 }



 public void run()
 {
  try
  {
   Project project=owner.getProject();
   List<String> cAas=owner.getCommandsAndArguments();
   String production_url=cAas.get(2);
   String development_url=(cAas.size()>3 ? cAas.get(3) : null);

   BoxFor3<Website, String, String> b3=project.createNewWebsite(production_url, development_url);

   logOut.println("Website with production url '"+b3.element2+"' and development url '"+b3.element3+"' succesfully created!");
  }
  catch (Throwable ignore)
  {

  }
 }

}
