#!/bin/bash
# ----------------------------------------------------------------------------
# Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
#
# WSO2 Inc. licenses this file to you under the Apache License,
# Version 2.0 (the "License"); you may not use this file except
# in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
# ----------------------------------------------------------------------------

# This script replaces the necessary configs in the API Manager configurations with the values provided in
# on-premise-gateway.properties file located in CARBON_HOME

# ----------------------------------------------------------------------------

# You can set the following values directly in the script of enter them interactively while the script is executed
ORG_KEY="${WSO2_CLOUD_ORG_KEY}"
EMAIL="${WSO2_CLOUD_EMAIL}"
PASSWORD="${WSO2_CLOUD_PASSWORD}"
AUTOSTART="${WSO2_CLOUD_AUTOSTART:-"false"}"

# if JAVA_HOME is not set we're not happy
if [ -z "$JAVA_HOME" ]; then
  echo "You must set the JAVA_HOME variable before running chpasswd."
  exit 1
fi

# OS specific support.  $var _must_ be set to either true or false.
cygwin=false
os400=false
case "`uname`" in
CYGWIN*) cygwin=true;;
OS400*) os400=true;;
esac

# resolve links - $0 may be a softlink
PRG="$0"

while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '.*/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

# Get standard environment variables
PRGDIR=`dirname "$PRG"`

# Only set CARBON_HOME if not already set
[ -z "$CARBON_HOME" ] && CARBON_HOME=`cd "$PRGDIR/.." ; pwd`

# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin; then
  [ -n "$JAVA_HOME" ] && JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
  [ -n "$CARBON_HOME" ] && CARBON_HOME=`cygpath --unix "$CARBON_HOME"`
  [ -n "$CLASSPATH" ] && CLASSPATH=`cygpath --path --unix "$CLASSPATH"`
fi

# For OS400
if $os400; then
  # Set job priority to standard for interactive (interactive - 6) by using
  # the interactive priority - 6, the helper threads that respond to requests
  # will be running at the same priority as interactive jobs.
  COMMAND='chgjob job('$JOBNAME') runpty(6)'
  system $COMMAND

  # Enable multi threading
  QIBM_MULTI_THREADED=Y
  export QIBM_MULTI_THREADED
fi

if [ -z "$JAVACMD" ] ; then
  if [ -n "$JAVA_HOME"  ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
      # IBM's JDK on AIX uses strange locations for the executables
      JAVACMD="$JAVA_HOME/jre/sh/java"
    else
      JAVACMD="$JAVA_HOME/bin/java"
    fi
  else
    JAVACMD=java
  fi
fi

if [ ! -x "$JAVACMD" ] ; then
  echo "Error: JAVA_HOME is not defined correctly."
  echo " CARBON cannot execute $JAVACMD"
  exit 1
fi

echo "JAVA_HOME Set to ${JAVA_HOME}"
echo "CARBON_HOME Set to ${CARBON_HOME}"

# ----- Execute The Requested Command -----------------------------------------

## Check whether the configure script has been run previously
if [ -f $CARBON_HOME/configure.lck ]; then
    echo "Your Gateway is already configured. If you want to reconfigure, please remove 'configure.lck' file found in ${CARBON_HOME} and run the startup script"
    echo "\nYou can start WSO2 On Premise API Gateway by going to the ${CARBON_HOME}/bin directory using the command-line,\nand then executing wso2server.sh (for Linux.) or wso2server.bat (for Windows)"
    exit
fi

if [ -z "$ORG_KEY" ] || [ -z "$EMAIL" ] || [ -z "$PASSWORD" ]; then
echo "Your credentials will be required for accessing the services in API Cloud which are required for the functionality of the On Premise Gateway."
fi

cp $CARBON_HOME/resources/cloud-on-premise-gateway.properties $CARBON_HOME/repository/conf/on-premise-gateway.properties

if [ -z "$ORG_KEY" ]; then
    echo "Please enter your Organization Key used in WSO2 API Cloud"
    echo "Organization Key: "
    read ORG_KEY
fi

if [ -z "$EMAIL" ]; then
    echo "Please enter your email used for login to WSO2 API Cloud"
    echo "Email: "
    read EMAIL
fi

if [ -z "$PASSWORD" ]; then
    echo "Please enter your password for ${EMAIL}: "
    read -s PASSWORD
    echo
fi


${JAVACMD} -Dcarbon.home="$CARBON_HOME" -jar ${CARBON_HOME}/lib/org.wso2.onpremise.gateway.configurator-1.0.0.jar ${EMAIL}  ${ORG_KEY} ${PASSWORD}
OUT=$?
if [ $OUT -eq 0 ];then
  echo "Your On Premise Gateway has been configured successfully."
  if [ "$AUTOSTART" = "true" ]; then
    ${CARBON_HOME}/bin/wso2server.sh
  else
    echo "You can start WSO2 On Premise API Gateway by going to the ${CARBON_HOME}/bin directory using the command-line,"
    echo "and then executing wso2server.sh (for Linux.) or wso2server.bat (for Windows)"
  fi
else
   echo "Something went wrong while configuring the On Premise Gateway. Please check your credentials and re-try. If the problem persists please contact: cloud@wso2.com."
fi

