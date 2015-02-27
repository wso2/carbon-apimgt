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

if "%OS%"=="Windows_NT" @setlocal
if "%OS%"=="WINNT" @setlocal

rem %~dp0 is expanded pathname of the current script under NT
set SYNAPSE_HOME=%~dps0..

set _SYNAPSE_XML="%SYNAPSE_HOME%\repository\conf\synapse-config"
set _XDEBUG=
set _SERVER_NAME=

rem Slurp the command line arguments. This loop allows for an unlimited number
rem of arguments (up to the command line limit, anyway).

:setupArgs
if ""%1""=="""" goto doneStart
if ""%1""==""-sample"" goto SYNAPSESample
if ""%1""==""-serverName"" goto serverName
if ""%1""==""-xdebug"" goto xdebug
shift
goto setupArgs

rem is there is a -xdebug in the options
:xdebug

set _XDEBUG="wrapper.java.additional.4=-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000"
shift
goto setupArgs

:SYNAPSESample
shift
set _SYNAPSE_XML="%SYNAPSE_HOME%\repository\conf\sample\synapse_sample_%1.xml"
shift
goto setupArgs

:serverName
shift
set _SERVER_NAME=%1
shift
goto setupArgs

:doneStart
rem find SYNAPSE_HOME if it does not exist due to either an invalid value passed
rem by the user or the %0 problem on Windows 9x
if exist "%SYNAPSE_HOME%\README.TXT" goto checkJava

:noSYNAPSEHome
echo SYNAPSE_HOME is set incorrectly or SYNAPSE could not be located. Please set SYNAPSE_HOME.
goto end

:checkJava
set _JAVACMD=%JAVACMD%

if "%JAVA_HOME%" == "" goto noJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD="%JAVA_HOME%\bin\java.exe"
goto runServer

:noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD=java.exe
echo JAVA_HOME variable not defined or incorrect. Please set JAVA_HOME.

:runServer
@rem @echo on
cd %SYNAPSE_HOME%
echo "Starting Synapse/Java ..."
echo Using SYNAPSE_HOME:        %SYNAPSE_HOME%
echo Using JAVA_HOME:       %JAVA_HOME%

rem Decide on the wrapper binary.
set _WRAPPER_BASE=wrapper
set _WRAPPER_DIR=%SYNAPSE_HOME%\bin\native\
set _WRAPPER_EXE=%_WRAPPER_DIR%%_WRAPPER_BASE%-windows-x86-32.exe
if exist "%_WRAPPER_EXE%" goto conf
set _WRAPPER_EXE=%_WRAPPER_DIR%%_WRAPPER_BASE%-windows-x86-64.exe
if exist "%_WRAPPER_EXE%" goto conf
set _WRAPPER_EXE=%_WRAPPER_DIR%%_WRAPPER_BASE%.exe
if exist "%_WRAPPER_EXE%" goto conf
echo Unable to locate a Wrapper executable using any of the following names:
echo %_WRAPPER_DIR%%_WRAPPER_BASE%-windows-x86-32.exe
echo %_WRAPPER_DIR%%_WRAPPER_BASE%-windows-x86-64.exe
echo %_WRAPPER_DIR%%_WRAPPER_BASE%.exe
pause
goto :eof

rem
rem Find the wrapper.conf
rem
:conf
set _WRAPPER_CONF="%SYNAPSE_HOME%\repository\conf\wrapper.conf"

rem
rem Start the Wrapper
rem
:startup
"%_WRAPPER_EXE%" -c %_WRAPPER_CONF% wrapper.app.parameter.5=%_SYNAPSE_XML% wrapper.app.parameter.8=%_SERVER_NAME% %_XDEBUG%

if not errorlevel 1 goto :eof
pause


:end
set _JAVACMD=
set SYNAPSE_CMD_LINE_ARGS=

if "%OS%"=="Windows_NT" @endlocal
if "%OS%"=="WINNT" @endlocal

:mainEnd
