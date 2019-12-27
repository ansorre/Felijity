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

package me.as.felijity.builder.builtin.styles;


import io.bit3.jsass.CompilationException;
import io.bit3.jsass.Compiler;
import io.bit3.jsass.Options;
import io.bit3.jsass.Output;
import io.bit3.jsass.OutputStyle;
import me.as.felijity.builder.AbstractBuilder;
import me.as.lib.markdown.MarkdownExtras;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static me.as.lib.core.lang.ArrayExtras.toList;
import static me.as.lib.core.lang.ExceptionExtras.getDeepCauseStackTrace;
import static me.as.lib.core.lang.StringExtras.isNotBlank;
import static me.as.lib.core.system.FileSystemExtras.*;
import static me.as.lib.markdown.MarkdownExtras.toMapOfStrings;


public class StylesBuilder extends AbstractBuilder<StylesBuilderConfiguration>
{

 public StylesBuilderConfiguration getDefaultConfiguration()
 {
  StylesBuilderConfiguration res=new StylesBuilderConfiguration();
  res.targetDirectories=new String[]{"styles"};
  res.mainScsses=new String[]{"main.scss"};
  return res;
 }


 public void build()
 {
  final List<String> yetMadeFiles=new ArrayList<>();

  toList(configuration.targetDirectories).forEach(
   dir -> toList(configuration.mainScsses).forEach(
    fileNameGlobPattern -> searchFiles(yetMadeFiles, dir, fileNameGlobPattern, this::buildScss)
   ));
 }


 private void buildScss(String file)
 {
  try
  {
   _i_buildScss(file);
  }
  catch (Throwable tr)
  {
   System.err.println(getDeepCauseStackTrace(tr));
  }
 }


 private void _i_buildScss(String file)
 {
  String code=loadTextFromFile(file);
  int i=code.indexOf("/*---");
  int e=code.indexOf("---*/");

  if (i==0 && e>0)
  {
   String yamlFM=code.substring(i+2, e+3)+"\n\n# fake markdown";

   MarkdownExtras.parse(yamlFM,
   yamlFrontMatterBlock ->
   {
    Map<String, String> yfm=toMapOfStrings(yamlFrontMatterBlock);
    String output=yfm.get("output");

    if (isNotBlank(output))
    {
     output=adjustPath(manager.cedillaAdjust(output));
     String nAd[]=getDirAndFilename(output);

     if (grantDirectory(nAd[0]))
     {
      Compiler compiler=new Compiler();
      Options options=new Options();
      options.setOutputStyle(OutputStyle.COMPRESSED);

      try
      {
       Output css=compiler.compileFile(new File(file).toURI(), null, options);
       saveInFile(output, css.getCss());
       setLastModified(output, lastModified(file));
      }
      catch (CompilationException ce)
      {
       manager.getProblems().addShowStopper("Error compiling '"+file+"'\n"+ce.getErrorText(), true);
      }
     }
     else
      manager.getProblems().addShowStopper("Cannot create directory '"+nAd[0]+"'", true);
    }
    else
     manager.getProblems().addShowStopper("Main scss file misses 'output' value in the YAML Front Matter encapsulated in /**/ ("+file+")", true);
   });
  }
  else
   manager.getProblems().addShowStopper("Main scss file misses the YAML Front Matter encapsulated in /**/ ("+file+")", true);
 }


}
