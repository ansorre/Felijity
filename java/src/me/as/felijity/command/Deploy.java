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


import static me.as.felijity.command.CommandsCentral.zeroOrMore;
import static me.as.lib.core.log.LogEngine.logOut;


public class Deploy extends AbstractCommand
{

 public String getName()
 {
  return "deploy";
 }

 public String getHelp()
 {
  return "builds the specified websites or all the websites in the project if no one is specified, then deploys them to the remote servers, then terminates.";
 }


 public String[] getArgumentsNames()
 {
  return new String[]{"website-production-url", "website-production-url"};
 }


 public String[] getArgumentsPattern()
 {
  return new String[]{"deploy", zeroOrMore};
 }


 public void run()
 {

  logOut.println("this command is StillUnimplemented!");

 }

}
