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
  echo "You must set the JAVA_HOME variable before running Synapse Configuration Migrator."
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

MIGRATING_CONFIG=$SYNAPSE_HOME/repository/conf/synapse.xml
if $1; then
  MIGRATING_CONFIG=$1
fi


# ----- Execute The Requested Command -----------------------------------------

cd $SYNAPSE_HOME
echo "Starting Synapse Configuration Migration ..."
echo "Using SYNAPSE_HOME :    $SYNAPSE_HOME"
echo "Using JAVA_HOME :       $JAVA_HOME"
echo "Migrating configuration :     $MIGRATING_CONFIG"

mv $MIGRATING_CONFIG $MIGRATING_CONFIG.back

$JAVA_HOME/bin/java -server -Xms128M -Xmx128M \
    $TEMP_PROPS \
    -Dorg.apache.xerces.xni.parser.XMLParserConfiguration=org.apache.xerces.parsers.XMLGrammarCachingConfiguration \
    -Djava.endorsed.dirs=$SYNAPSE_ENDORSED \
    -Djava.io.tmpdir=$SYNAPSE_HOME/work/temp/synapse \
    -classpath $SYNAPSE_CLASSPATH \
    org.apache.synapse.migrator.ConfigurationMigrator \
        $MIGRATING_CONFIG.back \
        $SYNAPSE_HOME/repository/conf/migrated-synapse.xml \
        $SYNAPSE_HOME/resources/synapse-configuration-migrator.xslt \
