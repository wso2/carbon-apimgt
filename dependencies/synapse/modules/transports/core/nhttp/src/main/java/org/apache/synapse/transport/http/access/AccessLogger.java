/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.synapse.transport.http.access;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.commons.util.MiscellaneousUtil;

/**
 * Class that logs the Http Accesses to the access log files. Code segment borrowed from
 * Apache Tomcat's org.apache.catalina.valves.AccessLogValve with thanks.
 */
public class AccessLogger {

    /** The name of the system property used to specify/override the nhttp properties location */
    public static final String NHTTP_PROPERTIES = "nhttp.properties";

    //property name of nhttp log directory
    public static final String NHTTP_LOG_DIRECTORY = "nhttp.log.directory";

    public final static String ACCESS_LOG_ID = "org.apache.synapse.transport.nhttp.access";
    
    private static Log log = LogFactory.getLog(ACCESS_LOG_ID);

    public AccessLogger(final Log log) {
        super();
        this.initOpen();
        AccessLogger.log = log;
        buffered = true;
        checkExists = false;
    }

    /**
     * A date formatter to format a Date into a date in the given file format
     */
    protected SimpleDateFormat fileDateFormatter =
            new SimpleDateFormat(AccessConstants.FILE_FORMAT);

    /**
     * The PrintWriter to which we are currently logging, if any.
     */
    protected PrintWriter writer;

    /**
     * The as-of date for the currently open log file, or a zero-length
     * string if there is no open log file.
     */
    private volatile String dateStamp = "";

    /**
     * Instant when the log daily rotation was last checked.
     */
    private volatile long rotationLastChecked = 0L;

    /**
     * Buffered logging.
     */
    private boolean buffered = true;

    /**
     * Do we check for log file existence? Helpful if an external
     * agent renames the log file so we can automatically recreate it.
     */
    private boolean checkExists = false;

    /**
     * The current log file we are writing to. Helpful when checkExists
     * is true.
     */
    protected File currentLogFile = null;

    /**
     * Can the log file be rotated.
     */
    protected boolean isRotatable = true;


    /**
     * Log the specified message to the log file, switching files if the date
     * has changed since the previous log call.
     *
     * @param message Message to be logged
     */
    public void log(String message) {
        if (isRotatable) {
            // Only do a logfile switch check once a second, max.
            long systime = System.currentTimeMillis();
            if ((systime - rotationLastChecked) > 1000) {
                synchronized (this) {
                    if ((systime - rotationLastChecked) > 1000) {
                        rotationLastChecked = systime;

                        String tsDate;
                        // Check for a change of date
                        tsDate = fileDateFormatter.format(new Date(systime));

                        // If the date has changed, switch log files
                        if (!dateStamp.equals(tsDate)) {
                            close();
                            dateStamp = tsDate;
                            open();
                        }
                    }
                }
            }
        }

        /* In case something external rotated the file instead */
        if (checkExists) {
            synchronized (this) {
                if (currentLogFile != null && !currentLogFile.exists()) {
                    try {
                        close();
                    } catch (Throwable e) {
                        handleThrowable(e);
                        log.info("Access Log file Close failed");
                    }

                    /* Make sure date is correct */
                    dateStamp = fileDateFormatter.format(
                            new Date(System.currentTimeMillis()));

                    open();
                }
            }
        }

        // Log this message
        synchronized (this) {
            if (writer != null) {
                writer.println(message);

                if (!buffered) {
                    writer.flush();
                }
            }
        }
    }

    protected synchronized void initOpen() {
        /* Make sure date is correct */
        dateStamp = fileDateFormatter.format(
                new Date(System.currentTimeMillis()));
        this.open();
    }

    /**
     * Open the new log file for the date specified by <code>dateStamp</code>.
     */
    protected synchronized void open() {
        // Create the directory if necessary
        File dir;
        Properties synapseProps = MiscellaneousUtil.loadProperties(NHTTP_PROPERTIES);
        String nhttpLogDir =  synapseProps.getProperty(NHTTP_LOG_DIRECTORY);
        if (nhttpLogDir != null) {
            dir = new File(nhttpLogDir);
        } else {
            dir = new File(AccessConstants.DIRECTORY);
        }
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                log.error("Access Log Open Directory Failed");
            }
        }

        // Open the current log file
        try {
            String pathname;
            // If no rotate - no need for dateStamp in fileName
            if (isRotatable) {
                pathname = dir.getAbsolutePath() + File.separator + AccessConstants.PREFIX +
                           dateStamp + AccessConstants.SUFFIX;
            } else {
                pathname = dir.getAbsolutePath() + File.separator + AccessConstants.PREFIX +
                           AccessConstants.SUFFIX;
            }

            writer = new PrintWriter(new BufferedWriter(new FileWriter(
                    pathname, true), 128000), true);

            currentLogFile = new File(pathname);
        } catch (IOException e) {
            log.warn("Unable to open the print writer", e);
            writer = null;
            currentLogFile = null;
        }
    }

    /**
     * Close the currently open log file (if any)
     */
    synchronized void close() {
        if (writer == null) {
            return;
        }
        writer.flush();
        writer.close();
        writer = null;
        dateStamp = "";
        currentLogFile = null;
    }

    /**
     * Checks whether the supplied Throwable is one that needs to be
     * re-thrown and swallows all others.
     *
     * @param t the Throwable to check
     */
    public static void handleThrowable(Throwable t) {
        if (t instanceof ThreadDeath) {
            throw (ThreadDeath) t;
        }
        if (t instanceof VirtualMachineError) {
            throw (VirtualMachineError) t;
        }
        // All other instances of Throwable will be silently swallowed
    }
}
