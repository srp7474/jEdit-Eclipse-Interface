echo making eclipse MarkerNotifyUDP plugin
set prevRUN=make-eclip-plug.bat
copy ..\..\UDPMarkerListener\cls\com\rexcel\eclipse\plugin\UDPMarkerListener*.class temp\com\rexcel\eclipse\plugin
copy ..\..\tools\cls\com\rexcel\jedit\tools\UDPMarkerMessage*.class temp\com\rexcel\jedit\tools
C:\c-pgms\jdk8u45x\bin\jar cfv MarkerNotifyUDP.jar -C temp com
copy MarkerNotifyUDP.jar d:\1\plugins\plugins\UDPMarkerListener_1.0.0.201609080635
copy MarkerNotifyUDP.jar C:\c-pgms\eclipse4.4\plugins\UDPMarkerListener_1.0.0.201609080635
quit
