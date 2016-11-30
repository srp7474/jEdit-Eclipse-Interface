echo making UDPErrors jedit plugin
set prevRUN=make-jedit-plug.bat
rem copy ..\ext-lib\jedit.jar
rem cd cls
rem C:\c-pgms\jdk8u45x\bin\jar ufv ..\jedit.jar org\gjt\sp\jedit\gui org\gjt\sp\jedit\input
C:\c-pgms\jdk8u45x\bin\jar cfv UDPErrors.jar -C ..\UDPErrors . -C ..\cls udperrors
copy UDPErrors.jar C:\c-pgms\jedit5.3\jars
