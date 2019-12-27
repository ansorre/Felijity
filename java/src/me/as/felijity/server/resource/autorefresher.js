(function autorefresher(serverUrl)
{
 var ws=new WebSocket(serverUrl);
 ws.onopen=function(event)
 {
  ws.onmessage=function(event)
  {
   console.log("reloading!");
   window.location.reload(true);
  };

  ws.send("page ready");
 };

})("ws://§hostPortUri");
