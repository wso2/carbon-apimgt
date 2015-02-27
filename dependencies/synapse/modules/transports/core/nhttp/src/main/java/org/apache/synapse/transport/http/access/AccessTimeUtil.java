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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Utility methods related to the Access Time
 */
public class AccessTimeUtil {

    private static String calculateTimeZoneOffset(long offset) {
        StringBuilder tz = new StringBuilder();
        if ((offset < 0)) {
            tz.append("-");
            offset = -offset;
        } else {
            tz.append("+");
        }

        long hourOffset = offset / (1000 * 60 * 60);
        long minuteOffset = (offset / (1000 * 60)) % 60;

        if (hourOffset < 10) {
            tz.append("0");
        }
        tz.append(hourOffset);

        if (minuteOffset < 10) {
            tz.append("0");
        }
        tz.append(minuteOffset);

        return tz.toString();
    }

    /**
     * gets the timezone +/-{****}}, offset from GMT,
     * @return String time zone (eg, +0530, -0600).
     */
    public static String getTimeZone() {
        try {
            int offset = TimeZone.getDefault().getRawOffset();
            return calculateTimeZoneOffset(offset);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * AccessDateStruct Class
     */
    private static class AccessDateStruct {
        private Date currentDate = new Date();
        private String currentDateString = null;
        private SimpleDateFormat dayFormatter = new SimpleDateFormat("dd");
        private SimpleDateFormat monthFormatter = new SimpleDateFormat("MM");
        private SimpleDateFormat yearFormatter = new SimpleDateFormat("yyyy");

        private SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");

        public AccessDateStruct() {
            TimeZone tz = TimeZone.getDefault();
            dayFormatter.setTimeZone(tz);
            monthFormatter.setTimeZone(tz);
            yearFormatter.setTimeZone(tz);
            timeFormatter.setTimeZone(tz);
        }
    }

    /**
     * The system time when we last updated the Date, that is used for log lines.
     */
    private static final ThreadLocal<AccessDateStruct> currentDateStruct =
            new ThreadLocal<AccessDateStruct>() {
                @Override
                protected AccessDateStruct initialValue() {
                    return new AccessDateStruct();
                }
            };


    /**
     * This method returns a Date object that is accurate to within one second.
     * If a thread calls this method to get a Date and it's been less than 1
     * second since a new Date was created, this method simply gives out the
     * same Date again so that the system doesn't spend time creating Date
     * objects unnecessarily.
     *
     * @return Date
     */
    public static Date getDate() {
        // Only create a new Date once per second, max.
        long systime = System.currentTimeMillis();
        AccessDateStruct struct = currentDateStruct.get();
        if ((systime - struct.currentDate.getTime()) > 1000) {
            struct.currentDate.setTime(systime);
            struct.currentDateString = null;
        }
        return struct.currentDate;
    }

    private static AccessDateStruct getAccessDateStruct(Date date) {
        AccessDateStruct struct = currentDateStruct.get();
        if (struct.currentDateString == null) {
            StringBuilder current = new StringBuilder(32);
            current.append('[');
            current.append(struct.dayFormatter.format(date));
            current.append('/');
            current.append(lookup(struct.monthFormatter.format(date)));
            current.append('/');
            current.append(struct.yearFormatter.format(date));
            current.append(':');
            current.append(struct.timeFormatter.format(date));
            current.append(' ');
            current.append(AccessTimeUtil.getTimeZone());
            current.append(']');
            struct.currentDateString = current.toString();
        }
        return struct;
    }

    /**
     * Return the month abbreviation for the specified month, which must
     * be a two-digit String.
     *
     * @param month Month number ("01" .. "12").
     * @return - the month
     */
    private static String lookup(String month) {
        int index;
        try {
            index = Integer.parseInt(month) - 1;
        } catch (Throwable t) {
            handleThrowable(t);
            index = 0;  // Can not happen, in theory
        }
        return (AccessConstants.MONTHS[index]);
    }

    public static String getAccessDate(Date date) {
         AccessDateStruct struct = getAccessDateStruct(date);
        return struct.currentDateString;
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