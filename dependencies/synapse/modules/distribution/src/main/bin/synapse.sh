#!/bin/sh

#   Licensed to the Apache Software Foundation (ASF) under one
#   or more contributor license agreements.  See the NOTICE file
#   distributed with this work for additional information
#   regarding copyright ownership.  The ASF licenses this file
#   to you under the Apache License, Version 2.0 (the
#   "License"); you may not use this file except in compliance
#   with the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing,
#   software distributed under the License is distributed on an
#    #  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
#   KIND, either express or implied.  See the License for the
#   specific language governing permissions and limitations
#   under the License.

# -----------------------------------------------------------------------------
#
# Environment Variable Prerequisites
#
#   SYNAPSE_HOME   Home of Synapse installation. If not set will use the parent directory
#
#   JAVA_HOME      Must point at your Java Development Kit installation.
#
# NOTE: Borrowed generously from Apache Tomcat startup scripts.

# if JAVA_HOME is not set we're not happy
if [ -z "$JAVA_HOME" ]; then
  echo "You must set the JAVA_HOME variable before running Synapse."
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

# Only set SYNAPSE_HOME if not already set
[ -z "$SYNAPSE_HOME" ] && SYNAPSE_HOME=`cd "$PRGDIR/.." ; pwd`



# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin; then
  [ -n "$JAVA_HOME" ] && JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
  [ -n "$SYNAPSE_HOME" ] && SYNAPSE_HOME=`cygpath --unix "$SYNAPSE_HOME"`
  [ -n "$AXIS2_HOME" ] && TUNGSTEN_HOME=`cygpath --unix "$SYNAPSE_HOME"`
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
  export QIBM_MULTI_THREADED=Y
fi

# update classpath - add any patches first
SYNAPSE_CLASSPATH="$SYNAPSE_HOME/lib/patches"
for f in $SYNAPSE_HOME/lib/patches/*.jar
do
  SYNAPSE_CLASSPATH=$SYNAPSE_CLASSPATH:$f
done

SYNAPSE_CLASSPATH=$SYNAPSE_CLASSPATH:"$SYNAPSE_HOME/lib"
for f in $SYNAPSE_HOME/lib/*.jar
do
  SYNAPSE_CLASSPATH=$SYNAPSE_CLASSPATH:$f
done
SYNAPSE_CLASSPATH=$SYNAPSE_HOME/repository/conf:$JAVA_HOME/lib/tools.jar:$SYNAPSE_CLASSPATH:$CLASSPATH

# use proper bouncy castle version for the JDK
jdk_15=`$JAVA_HOME/bin/java -version 2>&1 | grep 1.5`

if [ "$jdk_15" ]; then
    echo " Using Bouncy castle JAR for Java 1.5"
    for f in $SYNAPSE_HOME/lib/bcprov-jdk15*.jar
    do
      SYNAPSE_CLASSPATH=$f:$SYNAPSE_CLASSPATH
    done
else
    echo " [Warn] Synapse is tested only with Java 5"
fi

# For Cygwin, switch paths to Windows format before running java
if $cygwin; then
  JAVA_HOME=`cygpath --absolute --windows "$JAVA_HOME"`
  SYNAPSE_HOME=`cygpath --absolute --windows "$SYNAPSE_HOME"`
  AXIS2_HOME=`cygpath --absolute --windows "$SYNAPSE_HOME"`
  CLASSPATH=`cygpath --path --windows "$CLASSPATH"`
  JAVA_ENDORSED_DIRS=`cygpath --path --windows "$JAVA_ENDORSED_DIRS"`
fi
# endorsed dir
SYNAPSE_ENDORSED=$SYNAPSE_HOME/lib/endorsed

# synapse config
SYNAPSE_XML=$SYNAPSE_HOME/repository/conf/synapse-config

# server name
SERVER_NAME=

# ----- Uncomment the following line to enalbe the SSL debug options ----------
# TEMP_PROPS="-Djavax.net.debug=all"

while [ $# -ge 1 ]; do

if [ "$1" = "-xdebug" ]; then
    XDEBUG="-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,server=y,address=8000"
    shift

  elif [ "$1" = "-sample" ]; then
    SYNAPSE_XML=$SYNAPSE_HOME/repository/conf/sample/synapse_sample_$2.xml
    shift 2 # -sample and sample number

  elif [ "$1" = "-serverName" ]; then
    SERVER_NAME=$2
    shift 2 # -serverName and actual name

elif [ "$1" = "-h" ]; then
    echo "Usage: synapse.sh ( commands ... )"
    echo "commands:"
    echo "  -xdebug            Start Synapse under JPDA debugger"
    echo "  -sample (number)   Start with sample Synapse configuration of given number"
    echo "  -serverName <name> Name of the Synapse server instance"
    shift
    exit 0

  else
    echo "Error: unknown command:$1"
    echo "For help: synapse.sh -h"
    shift
    exit 1
  fi

done

# ----- Execute The Requested Command -----------------------------------------

cd $SYNAPSE_HOME
echo "Starting Synapse/Java ..."
echo "Using SYNAPSE_HOME:    $SYNAPSE_HOME"
echo "Using JAVA_HOME:       $JAVA_HOME"
echo "Using SYNAPSE_XML:     $SYNAPSE_XML"

$JAVA_HOME/bin/java -server -Xms128M -Xmx128M \
    $XDEBUG \
    $TEMP_PROPS \
    -Dorg.apache.xerces.xni.parser.XMLParserConfiguration=org.apache.xerces.parsers.XMLGrammarCachingConfiguration \
    -Djava.endorsed.dirs=$SYNAPSE_ENDORSED \
    -Djava.io.tmpdir=$SYNAPSE_HOME/work/temp/synapse \
    -classpath $SYNAPSE_CLASSPATH \
    org.apache.synapse.SynapseServer \
        $SYNAPSE_HOME/repository \
        $SYNAPSE_HOME/repository/conf/axis2.xml \
        $SYNAPSE_HOME \
        $SYNAPSE_XML \
        $SYNAPSE_HOME/repository \
        $SERVER_NAME