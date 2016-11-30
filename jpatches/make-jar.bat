echo making replacement jedit jar
set prevRUN=make-jar.bat
copy ..\ext-lib\jedit.jar
cd cls
C:\c-pgms\jdk8u45x\bin\jar ufv ..\jedit.jar org\gjt\sp\jedit\gui org\gjt\sp\jedit\input
cd ..
rem C:\c-pgms\jdk8u45x\bin\jar ufv jedit.jar -C ..\jedit-plug\cls com
rem C:\c-pgms\jdk8u45x\bin\jar ufv jedit.jar -C .\ErrorList errorlist
copy jedit.jar C:\c-pgms\jedit5.3
