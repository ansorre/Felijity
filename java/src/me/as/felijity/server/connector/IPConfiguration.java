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

package me.as.felijity.server.connector;


import me.as.lib.core.lang.ArrayExtras;


public class IPConfiguration
{
 public String host;
 public int port;
 public String protocol;


 public IPConfiguration()
 {

 }

 public IPConfiguration(String host, int port)
 {
  this.host=host;
  this.port=port;
 }

 public IPConfiguration(String host, int port, String protocol)
 {
  this(host, port);
  this.protocol=protocol;
 }


 public static IPConfiguration[] build(int port, String... hosts)
 {
  int t, len=ArrayExtras.length(hosts);
  IPConfiguration res[]=new IPConfiguration[len];

  for (t=0;t<len;t++)
  {
   res[t]=new IPConfiguration(hosts[t], port);
  }

  return res;
 }


 public static IPConfiguration[] merge(IPConfiguration[]... arrays)
 {
  IPConfiguration res[]=null;

  int t, len=ArrayExtras.length(arrays);

  for (t=0;t<len;t++)
  {
   res=(IPConfiguration[])ArrayExtras.appendAll(res, arrays[t]);
  }

  return res;
 }

 public static IPConfiguration[] all(int port)
 {
  return new IPConfiguration[]{new IPConfiguration("*", port)};
 }

}
