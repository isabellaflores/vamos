set JAVA_HOME=C:\Users\iflores\.apps\graalvm-ce-java11-22.0.0.2
set GRAALVM_HOME=%JAVA_HOME%
set PATH=%JAVA_HOME%\bin;c:\windows;c:\windows\system32;C:\Users\iflores\.apps\apache-maven-3.8.4\bin
call "C:\Program Files\Microsoft Visual Studio\2022\Community\VC\Auxiliary\Build\vcvars64.bat"
call mvn -e clean
if errorlevel 1 goto :EOF
call mvn -e gluonfx:build -f pom.xml
if errorlevel 1 goto :EOF
rcedit-x64 target\gluonfx\x86_64-windows\vamos-installer.exe --set-icon arrow-down.ico
if errorlevel 1 goto :EOF
@echo ***************
@echo BUILD COMPLETE!
@echo ***************
