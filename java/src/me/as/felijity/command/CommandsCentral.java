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



import me.as.lib.core.lang.ArrayExtras;
import me.as.lib.core.lang.ClassExtras;

import java.util.List;

import static me.as.lib.core.lang.ClassExtras.classFromNameNoException;
import static me.as.lib.core.lang.ClassExtras.getClassPackagePath;
import static me.as.lib.core.lang.ClassExtras.getClassesInPackageInstancing;


public class CommandsCentral
{
 // singleton
 private CommandsCentral(){}

 // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

 public static final String one = "-1";
 public static final String oneOrTwo = "-1|2";
 public static final String oneOrMore = "-1|*";
 public static final String zeroOrOne = "-0|1";
 public static final String zeroOrMore = "-0|*";


 public static boolean doesMatch(List<String> cAas, String... pattern)
 {
  boolean res=true;
  int t, cLen=cAas.size(), len=ArrayExtras.length(pattern);

  for (t=0;t<len && res;t++)
  {
   switch (pattern[t])
   {
    case one: return (cLen==len);
    case oneOrTwo:return (cLen==len || cLen==len+1);
    case oneOrMore:return (cLen>=len);
    case zeroOrOne:return (cLen==len || cLen==len-1);
    case zeroOrMore:return (cLen>=len-1);

    default:
    {
     try
     {
      String str=cAas.get(t);
      res=cAas.get(t).equals(pattern[t]);
     }
     catch (Throwable tr)
     {
      return false;
     }
    }
   }
  }

  return res;
 }



 public static List<Class<Command>> getCommandClasses()
 {
  return getClassesInPackageInstancing(getClassPackagePath(CommandsCentral.class), Command.class);
 }

}
