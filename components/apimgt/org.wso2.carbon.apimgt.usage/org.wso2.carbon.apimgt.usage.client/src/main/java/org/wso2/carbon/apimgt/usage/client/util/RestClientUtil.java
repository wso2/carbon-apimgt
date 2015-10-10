package org.wso2.carbon.apimgt.usage.client.util;

import org.apache.commons.codec.binary.Base64;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by rukshan on 9/28/15.
 */
public class RestClientUtil {
    public static long dateToLong(String date) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date dt =df.parse(date);
        Long l = dt.getTime();
        return l;
    }

    public static long getCeilingDateAsLong(String date) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date d =df.parse(date);
        d.setHours(0);
        d.setMinutes(0);
        d.setSeconds(0);
        d.setDate(d.getDate()+1);
        return d.getTime();
    }

    public static long getFloorDateAsLong(String date) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date d =df.parse(date);
        d.setHours(0);
        d.setMinutes(0);
        d.setSeconds(0);
        d.setDate(d.getDate());
        return d.getTime();
    }

    public static String encodeCredintials(String user,String pass){
        String cred=user+":"+pass;
        byte[] encodedBytes = Base64.encodeBase64(cred.getBytes());
        return new String(encodedBytes);
    }

//    public static APIUsageStatisticsClient getStatisticClient(String name){
//
//        APIUsageStatisticsClient client= null;
//        try {
//            client = new APIUsageStatisticsRestClientImpl(name);
//        } catch (APIMgtUsageQueryServiceClientException e) {
//            e.printStackTrace();
//        }
//        return client;
//    }
}
