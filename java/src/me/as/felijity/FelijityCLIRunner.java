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

package me.as.felijity;


import me.as.felijity.app.Constants;
import me.as.felijity.app.Felijity;
import me.as.lib.minicli.CLIArgument;
import me.as.lib.minicli.CLIOption;
import me.as.lib.minicli.CommandLineHandler;
import me.as.lib.minicli.HelpCLIOption;
import me.as.lib.minicli.NoOperand;
import me.as.lib.minicli.VersionCLIOption;
import me.as.lib.core.report.Problems;

import java.util.List;

import static me.as.lib.core.log.DefaultTraceLevels.DEFAULT;
import static me.as.lib.core.system.FileSystemExtras.getCanonicalPath;


public class FelijityCLIRunner
{

 @VersionCLIOption NoOperand<String> version=new NoOperand<>(){{customContent=
  Constants.programName+" \""+Constants.version[0]+"."+Constants.version[1]+"."+Constants.version[2]+"\" "+Constants.date;}};

 @HelpCLIOption NoOperand help;


 @CLIOption
 (
  name= "-verbosity",
  aliases={".--verbosity"},
  operand="<COMMA_SEPARATED_LEVELS>",
  usage="Verbosity levels. Possible values are:\n" +
   "    *           - every message is printed out\n"+
   "    OFF         - no message is printed out\n"+
   "    SEVERE      - SEVERE messages are printed out\n"+
   "    FATAL_ERROR - FATAL_ERROR messages are printed out\n"+
   "    ERROR       - ERROR messages are printed out\n"+
   "    WARNING     - WARNING messages are printed out\n"+
   "    DEBUG       - DEBUG messages are printed out\n"+
   "    INFO        - INFO messages are printed out\n"+
   "    CONFIG      - CONFIG messages are printed out\n"+
   "    FINE        - FINE messages are printed out\n"+
   "    FINER       - FINER messages are printed out\n"+
   "    FINEST      - FINEST messages are printed out\n"+
   "Defaults is FINE,WARNING,ERROR,FATAL_ERROR.",
  helpOrder=11
 ) String verbosity=DEFAULT;



 @CLIArgument
 (
  usage="commands and options.",
  separator=""
 ) List<String> commandsAndArguments=null;


 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

 CommandLineHandler commandLineHandler;

 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

 private void start(Problems problems)
 {
  Felijity felijity=new Felijity().
    setCommandsAndArguments(commandsAndArguments).
    setVerbosity(verbosity).
    setWorkingDirectory(getCanonicalPath(".")).
    setCommandLineHandler(commandLineHandler);

  felijity.run(problems);
 }


 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .


 public static void main(String args[])
 {
  Problems problems=new Problems();
  FelijityCLIRunner runner=CommandLineHandler.prepare(FelijityCLIRunner.class, args, problems);
  if (runner!=null) runner.start(problems);
  problems.printIfTheCase();
 }


}
