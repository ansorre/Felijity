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


import me.as.felijity.command.Command;
import me.as.felijity.command.CommandsCentral;
import me.as.lib.buildtools.tool.AbstractBuildTool;
import me.as.lib.core.lang.ArrayExtras;
import me.as.lib.core.report.Problems;

import java.util.List;

import static me.as.felijity.command.CommandsCentral.*;
import static me.as.lib.core.lang.ClassExtras.newInstanceByClass;
import static me.as.lib.core.log.DefaultTraceLevels.FATAL_ERROR;
import static me.as.lib.core.log.LogEngine.logOut;


public class Felijity extends AbstractBuildTool<Felijity>
{

 private List<String> commandsAndArguments=null;


 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
 // runtimes

// private Watcher watcher;
 Project project=null;


 public Felijity()
 {
  canRunWithoutArgsAndWithoutOptions=true;
 }


 public String getDefaultConfigFile()
 {
  return null;
 }


 public synchronized Project getProject()
 {
  if (project==null)
   project=new Project(this);

  return project;
 }





 public List<String> getCommandsAndArguments()
 {
  return commandsAndArguments;
 }

 public Felijity setCommandsAndArguments(List<String> commandsAndArguments)
 {
  this.commandsAndArguments=commandsAndArguments;
  return this;
 }


 private void runInteractiveHelpAndExit()
 {
  logOut.println(Constants.versionLine);

  logOut.println("\nto get help for the command line options and arguments run:");
  logOut.println("felijity --help");

  logOut.println("\nto get help for the commands available run:");
  logOut.println("felijity help");
 }


 private void showTheErrorsAndExit()
 {
  logOut.println(FATAL_ERROR, FATAL_ERROR+": '"+commandsAndArguments.get(0)+"' command is unknown or the number of arguments is incorrect!?!?");
  logOut.println("Please run 'felijity help "+commandsAndArguments.get(0)+"' to get help");
 }


 protected void adjustParameters()
 {
  super.adjustParameters();

 }


 public void run(Problems problems)
 {
  this.problems=problems;
  adjustParameters();

  if (!problems.areThereShowStoppers())
  {
   no_else_please:
   {
    if (ArrayExtras.length(commandsAndArguments)==0)
    {
     runInteractiveHelpAndExit();
     break no_else_please;
    }

    List<Class<Command>> classes=CommandsCentral.getCommandClasses();

    for (Class<Command> clas : classes)
    {
     Command cmd=newInstanceByClass(clas);

     if (doesMatch(commandsAndArguments, cmd.getArgumentsPattern()))
     {
      cmd.setup(this).run();
      break no_else_please;
     }
    }

    showTheErrorsAndExit();
   }
  }
 }



}
