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

import static me.as.felijity.app.Constants.defaultProjectFileName;
import static me.as.felijity.command.CommandsCentral.one;
import static me.as.lib.core.log.LogEngine.logOut;
import static me.as.lib.core.system.FileSystemExtras.*;


public class NewProject extends AbstractCommand
{

 public String getName()
 {
  return "new";
 }

 public String getHelp()
 {
  return "creates a new folder under the current one and initialize it with a new project.";
 }


 public String[] getArgumentsPattern()
 {
  return new String[]{"new", "project", one};
 }

 public String[] getArgumentsNames()
 {
  return new String[]{"project-folder-name"};
 }


 public void run()
 {
  String projectFolder=owner.getCommandsAndArguments().get(2);
  String dir=getCanonicalPath(mergePath(".", projectFolder));
  String projectFile=mergePath(dir, defaultProjectFileName);

  if (!isFile(projectFile))
  {
   try
   {
    new Project(owner, projectFile, true);
    logOut.println("Project succesfully created.\nNow enter the folder '"+projectFolder+"' to work with the project.");
   }
   catch (Throwable ignore)
   {
   }
  }
  else
   owner.getProblems().addShowStopper("The specified project '"+projectFolder+"' already exists!");
 }

}



