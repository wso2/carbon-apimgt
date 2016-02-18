/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*
*/
package org.wso2.carbon.apimgt.usage.client.util;

import org.apache.commons.codec.binary.Base64;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Class contain the util method used by the rest client impl
 */
public class RestClientUtil {

    private final static String DATE_PATTERN = "yyyy-MM-dd";

    /**
     * conversion from date to long value
     *
     * @param date date in the yyyy-MM-dd format
     * @return long value of the date
     * @throws ParseException throw when error in parsing date
     */
    public static long dateToLong(String date) throws ParseException {
        final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);
        Date fDate = dateFormat.parse(date);
        Long lDate = fDate.getTime();
        return lDate;
    }

    /**
     * return the ceiling time of the time and convert to long
     *
     * @param date date in the yyyy-MM-dd format
     * @return long ceiling value of date
     * @throws ParseException throw when error in parsing date
     */
    public static long getCeilingDateAsLong(String date) throws ParseException {
        final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);
        Date fDate = dateFormat.parse(date);
        Calendar calender = Calendar.getInstance();
        calender.setTime(fDate);
        calender.set(Calendar.HOUR, 0);
        calender.set(Calendar.MINUTE, 0);
        calender.set(Calendar.SECOND, 0);
        calender.set(Calendar.DATE, calender.get(Calendar.DATE) + 1);
        return calender.getTimeInMillis();
    }

    /**
     * return the floor time of the time and convert to long
     *
     * @param date date in the yyyy-MM-dd format
     * @return long floor value of date
     * @throws ParseException throw when error in parsing date
     */
    public static long getFloorDateAsLong(String date) throws ParseException {
        final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);
        Date fDate = dateFormat.parse(date);
        Calendar calender = Calendar.getInstance();
        calender.setTime(fDate);
        calender.set(Calendar.HOUR, 0);
        calender.set(Calendar.MINUTE, 0);
        calender.set(Calendar.SECOND, 0);
        calender.set(Calendar.DATE, calender.get(Calendar.DATE));
        return calender.getTimeInMillis();
    }

    /**
     * get the base 64 encoded username and password
     *
     * @param user username
     * @param pass password
     * @return encoded basic auth, as string
     */
    public static String encodeCredentials(String user, char[] pass) {
        StringBuilder builder = new StringBuilder(user).append(':').append(pass);
        String cred = builder.toString();
        byte[] encodedBytes = Base64.encodeBase64(cred.getBytes());
        return new String(encodedBytes);
    }

    /**
     * Parsing long value to the Date format
     *
     * @param time time in long
     * @return
     */
    public static String longToDate(long time) {
        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return format.format(date);
    }
}
