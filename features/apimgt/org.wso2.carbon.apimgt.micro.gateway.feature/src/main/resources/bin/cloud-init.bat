@echo off
REM Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
REM
REM WSO2 Inc. licenses this file to you under the Apache License,
REM Version 2.0 (the "License"); you may not use this file except
REM in compliance with the License.
REM You may obtain a copy of the License at
REM
REM   http://www.apache.org/licenses/LICENSE-2.0
REM
REM Unless required by applicable law or agreed to in writing,
REM software distributed under the License is distributed on an
REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
REM KIND, either express or implied.  See the License for the
REM specific language governing permissions and limitations
REM under the License.

rem ---------------------------------------------------------------------------
rem This script replaces the necessary configs in the API Manager configurations with the values provided in
rem on-premise-gateway.properties file located in CARBON_HOME
rem ---------------------------------------------------------------------------

set CURRENT_DIR=%cd%

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
rem check the CARBON_HOME environment variable
setlocal EnableDelayedExpansion
set CURRENT_DIR=%cd%
if not "%CARBON_HOME%" == "" goto gotHome
set CARBON_HOME=%CURRENT_DIR%
if exist "%CARBON_HOME%\bin\cloud-init.bat" goto okHome

rem guess the home. Jump one directory up to check if that is the home
cd ..
set CARBON_HOME=%cd%
cd %CARBON_HOME%

:gotHome
if exist "%CARBON_HOME%\bin\cloud-init.bat" goto okHome

rem set CARBON_HOME=%~sdp0..
set CARBON_HOME=%~sdp0..
if exist "%CARBON_HOME%\bin\cloud-init.bat" goto okHome

echo The CARBON_HOME environment variable is not defined correctly
echo This environment variable is needed to run this program
goto end

:okHome

copy "%CARBON_HOME%\resources\wso2-cloud\cloud-on-premise-gateway.properties" "%CARBON_HOME%\repository\conf\on-premise-gateway.properties" options > nul
echo Your credentials will be required for accessing the services in API Cloud which are required for the functionality of the On Premise Gateway.

set /p ORG_KEY= Please enter your Organization Key used in WSO2 API Cloud:
set /p EMAIL= Please enter your email used for login to WSO2 API Cloud:
set /p PASSWORD= Please enter your password for %EMAIL%:

rem set the classes
cd "%CARBON_HOME%"
set CARBON_CLASSPATH=.\
set CARBON_CLASSPATH=!CARBON_CLASSPATH!;".\lib\*"
set CARBON_CLASSPATH=!CARBON_CLASSPATH!;".\repository\components\plugins\*"


rem ----- Execute The Requested Command ---------------------------------------
echo Using CARBON_HOME:   %CARBON_HOME%
echo Using JAVA_HOME:    %JAVA_HOME%
set _RUNJAVA="%JAVA_HOME%\bin\java"

%_RUNJAVA% %JAVA_OPTS% -Dcarbon.home="%CARBON_HOME%" -cp "%CARBON_CLASSPATH%" org.wso2.carbon.apimgt.micro.gateway.configurator.Configurator %EMAIL% %ORG_KEY% %PASSWORD%

echo.

if errorlevel 1 (
	echo Something went wrong while configuring the On Premise Gateway.
	echo Please check your credentials and re-try.
	echo If the problem persists please contact: cloud@wso2.com.
) else (
	echo Your On Premise Gateway has been configured successfully.
	echo You can start WSO2 On Premise API Gateway by going to the %CARBON_HOME% directory using the command-line,
	echo and then executing wso2server.bat"
)

echo.

endlocal
:end
