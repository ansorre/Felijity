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
import me.as.felijity.app.Website;
import me.as.lib.core.concurrent.ThreadExtras;
import me.as.lib.core.lang.RandomExtras;
import me.as.lib.core.lang.StringExtras;
import me.as.lib.core.system.OSExtras;

import java.util.List;

import static me.as.felijity.command.CommandsCentral.oneOrMore;
import static me.as.lib.core.log.LogEngine.logOut;
import static me.as.lib.core.system.FileSystemExtras.*;


public class Delete extends AbstractCommand
{

 public String getName()
 {
  return "delete";
 }

 public String getHelp()
 {
  return "DANGER: removes a website forever, no undo possibile.";
 }



 public String[] getArgumentsPattern()
 {
  return new String[]{"delete", "website", oneOrMore};
 }


 public String[] getArgumentsNames()
 {
  return new String[]{"website-production-url", "pass frase for unattend exection"};
 }



 private boolean saidYes(String question)
 {
  boolean uppercase=(RandomExtras.random()>0.5);

  logOut.println("\nPlease if you want to anwser yes to the following question type it in "+(uppercase?"UPPERCASE":"lowercase"));
  logOut.print(question+" ");

  String response=OSExtras.readConsoleLine();
  return StringExtras.areEqual(response, (uppercase?"YES":"yes"));
 }


 private void destroyDir(String root)
 {
  if (isDirectory(root))
  {
   deleteTheTree(root);
   deleteFile(root);
  }
 }


 public void run()
 {
  List<String> cAa=owner.getCommandsAndArguments();
  String productionUrl=cAa.get(2);

  try
  {
   final Project project=owner.getProject();
   Website web=project.getWebsiteByProductionUrl(productionUrl);
   boolean confirmed;

   try
   {
    confirmed=
     (
      "really".equals(cAa.get(3)) &&
      "delete".equals(cAa.get(4)) &&
      "it".equals(cAa.get(5)) &&
      "now".equals(cAa.get(6))
     );
   }
   catch (Throwable tr)
   {
    confirmed=false;
   }

   if (!confirmed)
   {
    logOut.println("Deleting the website '"+productionUrl+"' is an action that cannot be undone at all.");
    logOut.println("For this reason it is strongly suggested to type Ctrl-C right now and give up doing this not undoable deletion.");
    logOut.println("If instead you really want to permanently delete ( -> in a way that cannot be undone!!!) the website '"+productionUrl+"' then you need to answer affermatively 3 times to the following questions:");

    confirmed=
     saidYes("Do you really want to delete this website (undo not possible!)?") &&
     saidYes("Are you sure I should permanently delete the website '"+productionUrl+"'?") &&
     saidYes("Delete '"+productionUrl+"' with no recovery possibile?");

    if (confirmed)
    {
     logOut.println("\nI'm going to permanently delete the website '"+productionUrl+"' in ---------> 10 seconds.");
     logOut.println("In the meantime you can hit Ctrl-C and abort the operation should you change you mind.");
     logOut.println("On the other side, next time, if you want to avoid all this hassle and want just delete a website with an unattended command, invoke this command:");
     logOut.println(Constants.programName+" delete website "+productionUrl+" really delete it now\n");

     ThreadExtras.sleep(5000);
     logOut.println("Permanently deleting website '"+productionUrl+"' in ---> 5 seconds");

     ThreadExtras.sleep(1000);
     logOut.println("... 4 ...");

     ThreadExtras.sleep(1000);
     logOut.println("Now you have only 3 seconds to hit Ctrl-C and cancel the destruction of the website '"+productionUrl+"'");

     ThreadExtras.sleep(1000);
     logOut.println("... 2 ...");

     ThreadExtras.sleep(1000);
     logOut.println("... 1 ...");

     ThreadExtras.sleep(1000);
    }
    else
    {
     logOut.println("\nDeletion wisely aborted.");
    }
   }

   if (confirmed)
   {
    logOut.println("Deleting website '"+productionUrl+"'...");

    destroyDir(web.getDeployRoot());
    destroyDir(web.getWebsiteRoot());
    project.removeWebsite(web);

    logOut.println("Website '"+productionUrl+" deleted!");
   }
  }
  catch (Throwable ignore){}
 }

}
