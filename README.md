
jEdit/Eclipse Interface Tools
=============================

#### jEdit/Eclipse Interface Tools ####

This repo contains the tools (windows based) that are used to build an interface between jEdit and Eclipse.

The rational for this is as follows:
  * Eclipse is the best ever compiler with fantastic dependency checking and incremental compile facilities. jEdit will never catch up.
  * jEdit is a very flexible editor built by programmers for programmers. I have
already added several customizations such as hotJump that have saved plenty of time.
Its hypersearch capability is the best find interface I have ever used, especially for program code files. Eclipse will never catch up.
  * I use an option in jEdit that does a save on task switch. (It is added as a bean shell interface in the configuration).
  * I task switch to Eclipse, do a compile, task switch to my PowerShell window and run the test (using the up arrow).
  * A few seconds and I have done a code change, compile and test (The systems I write are too complex to use Debug, I use log statements).
  * I realized there had to be a better way to get from the compile errors back to the line to be fixed in the editor than using the goto line and typing the line number.

####The Solution####
Eclipse provides the capability to listen to the compile errors.  The UDPMarkerListener provided in this repo does just that.
It compresses the compile error message(s) into a single package and broadcasts it via a UDP port.

The other part, the jEdit plugin called UDPErrors listens for the broadcast and populates the Error window provided by the ErrorList plugin.

Besides being pleasant to use, my productivity since implementing this interface has dramtically increased.

The result is that I am able to benefit from the best-of-breed qualities of two components required for speedy program development.

####Possible Improvements####
Time constraints prevent me from doing work to tidy up this package.
I am willing to assist anybody willing to step up to the plate and tie a bow around it. I can be reached at public&#064;rexcel.com


####Other Tools in this Directory####
Thirty+ years ago I started using an Editor called Brief.  It was later emulated under Windows by Multi-Edit.  The feature I used most, besides its save on task switch capability,
was the use of the Num-Pad + and - keys to copy and Cut respectively. Accompanied by using the INSERT key as the paste function, these key combinations
enabled single-key Copy/Cut/Paste operations. I find this capability is a must have for any editor I use on an extended basis.

jEdit had no built in capability to do this but I was able to implement it into jEdit 5.3 with a set of minor changes to several input handlers points found
in the jpatches directory.  These are installed by replaciing them in the jEdit.jar. The Num-Pad - and + are translated into F23, F24 keys respectively and then processed by
the SmartCut/SmartCopy macros.

These macros, along with others, are provided in the srp-macros folder.

#### Directory Structure ####
```
ext-lib             Required External jar files
jedit-plug          jEdit plug in
jpatches            Patches for single key Cut/Copy/Paste
sample-macros       Sample macros used as reference
srp-macros          srp developed macros
tools               scripts used to generate above (4NT compatible)
UDPMarkerListener   Eclipse plugin
```

#### Instalation Notes ####

  * Drop the plugin folder into the Eclipse plugins folder and restart.  Note that this was developed for Eclipse Luna (4.4)

  * Drop the UDPErrors.jar file into the jEdit jars folder.  This was developed for jEdit5.3
