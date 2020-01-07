# Felijity

Felijity is a very easy, lightweight static website builder for blazing fast static websites.  

## Key points

 * CLI: command line based 
 * easy: just run 'felijity help' and the list of available commands is presented
 * auto-reload: just run 'felijity serve' and you don't even need to hit refresh in the browser everytime you change a file  
 * many websites: with just one project and one 'felijity serve' you can create, manage and update many websites at the same time. 
 * ... many many more to follow...

## How to run 

* Download the [most recent release](https://github.com/ansorre/Felijity/releases/download/0.1.0/Felijity-release-0.1.0.zip).
* unzip in a folder of your choice, let's assume in &lt;felijity-install-dir&gt; 
* go in a folder under which you want to create a new project (every project can have many websites)
* on Unix-like systems (on Windows systems replace .sh with .cmd) execute:
``` 
  <felijity-install-dir>/felijity.sh new project all-my-sites
```

* enter the new created folder **all-my-sites**: 
``` 
  cd all-my-sites
```                                            

* on Unix-like systems (on Windows systems replace .sh with .cmd) create a new website:
``` 
  <felijity-install-dir>/felijity.sh new website firstsite.com
```                                             

* add dev-firstsite.com to your system "hosts" file ([how to change hosts fle on Windows, Linux and Mac?](https://www.hostinger.com/tutorials/how-to-emulate-edit-windows-hosts-file)), 
this is necessary because felijity allows you to work on more websites in the same project
```
 127.0.0.1 dev-firstsite.com
``` 
              
* on Unix-like systems (on Windows systems replace .sh with .cmd) execute:
``` 
  <felijity-install-dir>/felijity.sh serve
```

* point your browser to http://dev-firstsite.com and enjoy your first felijity website!

* change the files under "firstsite.com" with your favourite editor. Everytime you save them your browser will automatically reaload your website pages.   


## How to build
Instructions will be added...

## Feel like sponsoring this project?  
**Compliments, it's a very good idea. 🤗**    
You can do so right here: [Sponsor @ansorre on GitHub sponsors](https://github.com/sponsors/ansorre)  
Why it's a good idea? Read [here](https://ansorre.github.io/sponsor/).    

## Quick links

 * [Project website](https://ansorre.github.io/felijity/)
 * [Github project](https://github.com/ansorre/Felijity)
