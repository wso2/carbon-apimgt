@echo off
REM Licensed to the Apache Software Foundation (ASF) under one
REM or more contributor license agreements.  See the NOTICE file
REM distributed with this work for additional information
REM regarding copyright ownership.  The ASF licenses this file
REM to you under the Apache License, Version 2.0 (the
REM "License"); you may not use this file except in compliance
REM with the License.  You may obtain a copy of the License at
REM
REM    http://www.apache.org/licenses/LICENSE-2.0
REM
REM Unless required by applicable law or agreed to in writing,
REM software distributed under the License is distributed on an
REM  # "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
REM KIND, either express or implied.  See the License for the
REM specific language governing permissions and limitations
REM under the License.

rem ---------------------------------------------------------------------------
rem Startup script for the ciphertool
rem
rem Environment Variable Prerequisites
rem
rem   SYNAPSE_HOME      Must point at your SYNAPSE directory
rem
rem   JAVA_HOME       Must point at your Java Development Kit installation.
rem
rem   JAVA_OPTS       (Optional) Java runtime options
rem ---------------------------------------------------------------------------
set CURRENT_DIR=%cd%
set _XDEBUG="-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000"

rem Make sure prerequisite environment variables are set
if not "%JAVA_HOME%" == "" goto gotJavaHome
echo The JAVA_HOME environment variable is not defined
echo This environment variable is needed to run this program
goto end
:gotJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto noJavaHome
goto okJavaHome
:noJavaHome
echo The JAVA_HOME environment variable is not defined correctly
echo This environment variable is needed to run this program
echo NB: JAVA_HOME should point to a JDK/JRE
goto end
:okJavaHome

rem check the SYNAPSE_HOME environment variable
if not "%SYNAPSE_HOME%" == "" goto gotHome
set SYNAPSE_HOME=%CURRENT_DIR%
if exist "%SYNAPSE_HOME%\bin\ciphertool.bat" goto okHome

rem guess the home. Jump one directory up to check if that is the home
cd ..
set SYNAPSE_HOME=%cd%
cd %SYNAPSE_HOME%

:gotHome
if exist "%SYNAPSE_HOME%\bin\ciphertool.bat" goto okHome

rem set SYNAPSE_HOME=%~dp0..
if exist "%SYNAPSE_HOME%\bin\ciphertool.bat" goto okHome

echo The SYNAPSE_HOME environment variable is not defined correctly
echo This environment variable is needed to run this program
goto end

:okHome
rem set the classes
setlocal EnableDelayedExpansion
rem loop through the libs and add them to the class path
cd "%SYNAPSE_HOME%"
set SYNAPSE_CLASSPATH=.\conf
FOR %%C in ("%SYNAPSE_HOME%\lib\synapse-*.jar") DO set SYNAPSE_CLASSPATH=!SYNAPSE_CLASSPATH!;".\lib\%%~nC%%~xC"
FOR %%C in ("%SYNAPSE_HOME%\lib\commons-*.jar") DO set SYNAPSE_CLASSPATH=!SYNAPSE_CLASSPATH!;".\lib\%%~nC%%~xC"



rem ----- Execute The Requested Command ---------------------------------------
echo Using SYNAPSE_HOME:   %SYNAPSE_HOME%
echo Using JAVA_HOME:    %JAVA_HOME%
set _RUNJAVA="%JAVA_HOME%\bin\java"

set JAVA_ENDORSED=".\lib\endorsed";"%JAVA_HOME%\jre\lib\endorsed";"%JAVA_HOME%\lib\endorsed"

%_RUNJAVA% %JAVA_OPTS% -cp "%SYNAPSE_CLASSPATH%"  %_XDEBUG% -Djava.endorsed.dirs=%JAVA_ENDORSED% org.apache.synapse.securevault.tool.CipherTool %*
endlocal
:end

