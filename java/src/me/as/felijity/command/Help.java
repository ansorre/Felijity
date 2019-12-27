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


import me.as.lib.core.collection.RamTable;
import me.as.lib.core.lang.ArrayExtras;

import java.util.List;

import static me.as.felijity.command.CommandsCentral.*;
import static me.as.lib.core.lang.ClassExtras.newInstanceByClass;
import static me.as.lib.core.lang.ObjectExtras.areEqual;
import static me.as.lib.core.log.DefaultTraceLevels.FATAL_ERROR;
import static me.as.lib.core.log.LogEngine.logOut;


public class Help extends AbstractCommand
{

 public String getName()
 {
  return "help";
 }

 public String getHelp()
 {
  return "gives help for <command> or lists all avaliable commands if <command> is not passed.";
 }


 public String[] getArgumentsPattern()
 {
  return new String[]{"help", zeroOrOne};
 }

 public String[] getArgumentsNames()
 {
  return new String[]{"command"};
 }

 public void run()
 {
  List<Class<Command>> all=getCommandClasses();
  List<String> cAas=owner.getCommandsAndArguments();
  int t, len=ArrayExtras.length(cAas);

  if (len==1)
   showHelpForAll(all);
  else
   showHelpFor(all, cAas.get(1));
 }



 private StringBuilder addShortHelp(StringBuilder sb, String args, String help)
 {
  sb.append(args).append("\n");
  sb.append("    ").append(help).append("\n\n");

  return sb;
 }

 private void showHelpFor(List<Class<Command>> all, String command)
 {
  for (Class<Command> clas : all)
  {
   Command cmd=newInstanceByClass(clas);

   if (cmd.getName().equals(command))
   {
    StringBuilder sb=new StringBuilder();

    addShortHelp(sb, formatArguments(cmd), cmd.getHelp()).
     append("Details:\n").append(cmd.setup(owner).getDetailedHelp()).append("\n");

    logOut.println(sb.toString());
    return;
   }
  }

  logOut.println(FATAL_ERROR, FATAL_ERROR+": command '"+command+"' does not exist!");
  logOut.println("Run 'felijity help' to list available commands.");
 }


 private String formatArguments(Command cmd)
 {
  String args[]=cmd.getArgumentsPattern();
  StringBuilder sb=new StringBuilder();
  String argNames[];
  int t, len=ArrayExtras.length(args);

  for (t=0;t<len;t++)
  {
   if (t>0)
    sb.append(" ");

   argNames=cmd.getArgumentsNames();

   switch (args[t])
   {
    case one       :sb.append("<").append(argNames[0]).append(">");break;
    case oneOrTwo  :sb.append("<").append(argNames[0]).append("> [<").append(argNames[1]).append(">]");break;
    case oneOrMore :sb.append("<").append(argNames[0]).append("> [<").append(argNames[1]).append("> [...]]");break;
    case zeroOrOne :sb.append("[<").append(argNames[0]).append(">]");break;
    case zeroOrMore:sb.append("[<").append(argNames[0]).append("> [...]]");break;

    default:
    {
     sb.append(args[t]);
    }
   }
  }

  return sb.toString();
 }


 private void showHelpForAll(List<Class<Command>> all)
 {
  RamTable rt=new RamTable();

  all.forEach(
  clas ->
  {
   if (!areEqual(clas, Help.class))
   {
    int r=rt.getRowsCount();
    Command cmd=newInstanceByClass(clas);

    rt.setString(0, r, cmd.getName());
    rt.setString(1, r, formatArguments(cmd));
    rt.setString(2, r, cmd.setup(owner).getHelp());
   }
  });

  rt.sortByString(0);

  rt.insertRows(0, 1); // <-- this is me (help) on the top!
  rt.setString(0, 0, getName());
  rt.setString(1, 0, formatArguments(this));
  rt.setString(2, 0, getHelp());


  StringBuilder sb=new StringBuilder("Available commands:\n");
  sb.append("\n");

  int t, len=rt.getRowsCount();

  for (t=0;t<len;t++)
  {
   addShortHelp(sb, rt.getString(1, t), rt.getString(2, t));
  }

  logOut.println(sb);
 }


}



















