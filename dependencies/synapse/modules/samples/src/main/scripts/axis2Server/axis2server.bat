@echo off

REM  Copyright 2001,2004-2005 The Apache Software Foundation
REM
REM  Licensed under the Apache License, Version 2.0 (the "License");
REM  you may not use this file except in compliance with the License.
REM  You may obtain a copy of the License at
REM
REM      http://www.apache.org/licenses/LICENSE-2.0
REM
REM  Unless required by applicable law or agreed to in writing, software
REM  distributed under the License is distributed on an "AS IS" BASIS,
REM  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
REM  See the License for the specific language governing permissions and
REM  limitations under the License.

rem ---------------------------------------------------------------------------
rem Startup script for the Simple Axis Server (with default parameters)
rem
rem Environment Variable Prequisites
rem
rem   AXIS2_HOME      Must point at your AXIS2 directory
rem
rem   JAVA_HOME       Must point at your Java Development Kit installation.
rem
rem   JAVA_OPTS       (Optional) Java runtime options
rem ---------------------------------------------------------------------------

if "%OS%"=="Windows_NT" @setlocal
if "%OS%"=="WINNT" @setlocal

rem %~dp0 is expanded pathname of the current script under NT
if "%AXIS2_HOME%"=="" set AXIS2_HOME=%~dps0

rem find AXIS2_HOME if it does not exist due to either an invalid value passed
rem by the user or the %0 problem on Windows 9x

if exist "%AXIS2_HOME%\repository\conf\axis2.xml" goto checkJava

:noAxis2Home
echo AXIS2_HOME environment variable is set incorrectly or AXIS2 could not be located.
echo Please set the AXIS2_HOME variable appropriately
goto end

:checkJava
set _JAVACMD=%JAVACMD%
set _HTTPPORT=
set _HTTPSPORT=
set _SERVERNAME=

if "%JAVA_HOME%" == "" goto noJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD=%JAVA_HOME%\bin\java.exe

:setupArgs
if ""%1""=="""" goto runAxis2
if ""%1""==""-http"" goto httpport
if ""%1""==""-https"" goto httpsport
if ""%1""==""-name"" goto servername
if ""%1""==""-xdebug"" goto xdebug
shift
goto setupArgs

rem is a custom port specified
:httpport
shift
set _HTTPPORT="-Dhttp_port=%1"
shift
goto setupArgs

:httpsport
shift
set _HTTPSPORT="-Dhttps_port=%1"
shift
goto setupArgs

:servername
shift
set _SERVERNAME="-Dserver_name=%1"
shift
goto setupArgs

rem is there is a -xdebug in the options
:xdebug
set _XDEBUG="wrapper.java.additional.7=-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000"
shift
goto setupArgs

:noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD=java.exe
echo JAVA_HOME environment variable is set incorrectly or Java runtime could not be located.
echo Please set the JAVA_HOME variable appropriately
goto end


:runAxis2

cd %AXIS2_HOME%
echo "Starting Sample Axis2 Server ..."
echo Using AXIS2_HOME:        %AXIS2_HOME%
echo Using JAVA_HOME:       %JAVA_HOME%

rem Decide on the wrapper binary.
set _WRAPPER_BASE=wrapper
set _WRAPPER_DIR=%AXIS2_HOME%..\..\bin\native\
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
set _WRAPPER_CONF="%AXIS2_HOME%..\..\repository\conf\sample-server-wrapper.conf"

rem
rem Start the Wrapper
rem
:startup
"%_WRAPPER_EXE%" -c %_WRAPPER_CONF% wrapper.java.additional.1=%_HTTPPORT% wrapper.java.additional.2=%_HTTPSPORT% wrapper.java.additional.3=%_SERVERNAME% %_XDEBUG%

if not errorlevel 1 goto :eof
pause

:end
set _JAVACMD=
set AXIS2_CMD_LINE_ARGS=

if "%OS%"=="Windows_NT" @endlocal
if "%OS%"=="WINNT" @endlocal

:mainEnd
